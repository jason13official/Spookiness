package io.github.jason13official.spookiness.entity;

import io.github.jason13official.spookiness.lighting.LivingLights;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class JackOMimic extends JumpingPathfinderMob {

  private static final int LIGHT_EMISSION = 15;

  public final AnimationState yapAnimationState = new AnimationState();

  public JackOMimic(EntityType<? extends JackOMimic> type, Level level) {
    super(type, level);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, 3.0F).add(Attributes.MOVEMENT_SPEED, 0.3F).add(Attributes.ATTACK_DAMAGE, 3.0F);
  }

  @Override
  protected void registerGoals() {

    int goalPriority = 1;

    this.goalSelector.addGoal(goalPriority++, new FloatGoal(this));
    this.goalSelector.addGoal(goalPriority++, new MeleeAttackGoal(this, 1.4, true));
    this.goalSelector.addGoal(goalPriority++, new WaterAvoidingRandomStrollGoal(this, 0.6));
    this.goalSelector.addGoal(goalPriority++, new LookAtPlayerGoal(this, Player.class, 10.0F));

    int targetPriority = 1;

    this.targetSelector.addGoal(targetPriority++, new HurtByTargetGoal(this).setAlertOthers());
    this.targetSelector.addGoal(targetPriority++, new NearestAttackableTargetGoal<>(this, Player.class, true));
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

  @Override
  protected SoundEvent getJumpSound() {
    return SoundEvents.RABBIT_JUMP;
  }

  @Override
  protected SoundEvent getAmbientSound() {
    return SoundEvents.RABBIT_AMBIENT;
  }

  @Override
  protected SoundEvent getHurtSound(DamageSource source) {
    return SoundEvents.RABBIT_HURT;
  }

  @Override
  protected SoundEvent getDeathSound() {
    return SoundEvents.RABBIT_DEATH;
  }
}
