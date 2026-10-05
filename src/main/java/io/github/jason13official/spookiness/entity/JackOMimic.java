package io.github.jason13official.spookiness.entity;

import io.github.jason13official.spookiness.effect.SoulBurst;
import io.github.jason13official.spookiness.lighting.LivingLights;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;

public class JackOMimic extends JumpingPathfinderMob {

  private static final int LIGHT_EMISSION = 15;
  private static final int SOUL_BURST_PARTICLES = 64;
  private static final double SOUL_BURST_SPEED = 0.15;

  public final AnimationState yapAnimationState = new AnimationState();

  public JackOMimic(EntityType<? extends JackOMimic> type, Level level) {
    super(type, level);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 3.0F).add(Attributes.MOVEMENT_SPEED, 0.3F).add(Attributes.ATTACK_DAMAGE, 3.0F);
  }

  public static boolean checkJackOMimicSpawnRules(EntityType<JackOMimic> type, ServerLevelAccessor level, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {

    // TODO more custom?
    return Mob.checkMobSpawnRules(type, level, spawnReason, pos, random);
  }

  @Override
  protected void registerGoals() {

    int goalPriority = 1;

    this.goalSelector.addGoal(goalPriority++, new FloatGoal(this));
    this.goalSelector.addGoal(goalPriority++, new MeleeAttackGoal(this, 1.4, true));
    this.goalSelector.addGoal(goalPriority++, new WaterAvoidingRandomStrollGoal(this, 0.6));
    this.goalSelector.addGoal(goalPriority++, new LookAtPlayerGoal(this, Player.class, 10.0F));

    int targetPriority = 1;

    // these goals originally use forCombat targeting which is gated by PEACEFUL difficulty; override canAttack to simplify,
    // and allow targeting in peaceful
    this.targetSelector.addGoal(targetPriority++, new HurtByTargetGoal(this) {

      @Override
      protected boolean canAttack(@Nullable LivingEntity target, TargetingConditions targetConditions) {

        // return super.canAttack(target, targetConditions);
        return target != null && target.canBeSeenAsEnemy();
      }
    }.setAlertOthers());
    this.targetSelector.addGoal(targetPriority++, new NearestAttackableTargetGoal<>(this, Player.class, true) {
      @Override
      protected boolean canAttack(@Nullable LivingEntity target, TargetingConditions targetConditions) {

        // return super.canAttack(target, targetConditions);
        return target != null && target.canBeSeenAsEnemy();
      }
    });
  }

  @Override
  public boolean doHurtTarget(ServerLevel level, Entity target) {
    return super.doHurtTarget(level, target);
  }

  //  @Override
//  public void die(DamageSource source) {
//    super.die(source);
//  }

  public void spawnSoulBurst(ServerLevel level) {
    SoulBurst.spawn(level, this.getBoundingBox().getCenter(), SOUL_BURST_PARTICLES, 0.5, SOUL_BURST_SPEED);
  }

  @Override
  public boolean shouldShowName() {

    return false;
  }

  @Override
  public boolean canBeLeashed() {

    return false;
  }

  @Override
  public void baseTick() {
    super.baseTick();
    if (this.level().isClientSide()) {
      this.setupAnimationStates();
    }
  }

  @Override
  public void onAddedToLevel() {
    super.onAddedToLevel();
    LivingLights.add(this, LIGHT_EMISSION);
  }

  @Override
  public void onRemovedFromLevel() {
    super.onRemovedFromLevel();
    LivingLights.remove(this);
  }

  @Override
  public void tick() {
    super.tick();
    LivingLights.move(this);
  }

  private void setupAnimationStates() {
    if (this.isMidJump()) {
      this.yapAnimationState.startIfStopped(this.tickCount);
    } else {
      this.yapAnimationState.stop();
    }
  }

  @Override
  public boolean canSpawnSprintParticle() {

    return false;
  }

  // region sounds

  @Override
  protected float getSoundVolume() {
    return 0.5f;
  }

  @Override
  public float getVoicePitch() {

    return 0.8f;
  }

  @Override
  protected SoundEvent getJumpSound() {

    return SoundEvents.SLIME_JUMP_SMALL;
  }

  @Override
  protected SoundEvent getAmbientSound() {

    return SoundEvents.BOGGED_AMBIENT;
  }

  @Override
  protected SoundEvent getHurtSound(DamageSource source) {

    return SoundEvents.POLAR_BEAR_HURT;
  }

  @Override
  protected SoundEvent getDeathSound() {

    return SoundEvents.SNOW_GOLEM_DEATH;
  }

  // endregion sounds
}
