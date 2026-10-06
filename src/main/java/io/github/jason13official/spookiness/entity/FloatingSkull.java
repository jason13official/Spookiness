package io.github.jason13official.spookiness.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class FloatingSkull extends FloatingPathfinderMob implements Enemy {

  private static final byte CHATTER_EVENT = 4;
  private static final int CHATTER_TICKS = 12;
  private static final double TRAIL_SPEED_SQR = 1.0E-4;

  private boolean fromBlock;
  private int chatterTicks;

  public FloatingSkull(EntityType<? extends FloatingSkull> type, Level level) {
    super(type, level);
    this.xpReward = 3;
  }

  public static AttributeSupplier.Builder createAttributes() {
    return createFloatingAttributes().add(Attributes.MAX_HEALTH, 6.0F).add(Attributes.ATTACK_DAMAGE, 2.0F).add(Attributes.FOLLOW_RANGE, 16.0F).add(Attributes.FLYING_SPEED, 0.25F);
  }

  @Override
  protected void registerGoals() {

    int goalPriority = 1;

    this.goalSelector.addGoal(goalPriority++, new MeleeAttackGoal(this, 1.2, false));
    this.goalSelector.addGoal(goalPriority++, new WaterAvoidingRandomFlyingGoal(this, 0.8));
    this.goalSelector.addGoal(goalPriority++, new LookAtPlayerGoal(this, Player.class, 8.0F));
    this.goalSelector.addGoal(goalPriority++, new RandomLookAroundGoal(this));

    int targetPriority = 1;

    this.targetSelector.addGoal(targetPriority++, new HurtByTargetGoal(this).setAlertOthers());
    this.targetSelector.addGoal(targetPriority++, new NearestAttackableTargetGoal<>(this, Player.class, true));
  }

  public void setFromBlock(boolean fromBlock) {
    this.fromBlock = fromBlock;
  }

  @Override
  protected void addAdditionalSaveData(ValueOutput output) {
    super.addAdditionalSaveData(output);
    output.putBoolean("from_block", this.fromBlock);
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    this.fromBlock = input.getBooleanOr("from_block", false);
  }

  @Override
  public void playAmbientSound() {
    super.playAmbientSound();
    if (this.level() instanceof ServerLevel level) {
      level.broadcastEntityEvent(this, CHATTER_EVENT);
    }
  }

  @Override
  public void handleEntityEvent(byte id) {
    if (id == CHATTER_EVENT) {
      this.chatterTicks = CHATTER_TICKS;
    } else {
      super.handleEntityEvent(id);
    }
  }

  @Override
  public void tick() {
    super.tick();
    if (!this.level().isClientSide()) {
      return;
    }
    if (this.chatterTicks > 0) {
      this.chatterTicks--;
    }
    Vec3 motion = new Vec3(this.getX() - this.xo, this.getY() - this.yo, this.getZ() - this.zo);
    if (motion.lengthSqr() > TRAIL_SPEED_SQR || this.random.nextInt(8) == 0) {
      this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, this.xo, this.yo + 0.15, this.zo, 0.0, 0.01, 0.0);
    }
  }

  public float getChatter(float partialTicks) {
    float ticks = this.chatterTicks - partialTicks;
    return ticks <= 0.0F ? 0.0F : Mth.sin(ticks * 2.4F) * ticks / CHATTER_TICKS;
  }

  @Override
  public float getVoicePitch() {
    return 1.6F + this.random.nextFloat() * 0.4F;
  }

  @Override
  protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
    super.dropCustomDeathLoot(level, source, killedByPlayer);
    this.spawnAtLocation(level, new ItemStack(Items.BONE_MEAL, Mth.nextInt(this.random, 1, 3)));
    if (this.fromBlock) {
      this.spawnAtLocation(level, new ItemStack(Items.SKELETON_SKULL));
    }
  }

  @Override
  protected SoundEvent getAmbientSound() {
    return SoundEvents.SKELETON_AMBIENT;
  }

  @Override
  protected SoundEvent getHurtSound(DamageSource source) {
    return SoundEvents.SKELETON_HURT;
  }

  @Override
  protected SoundEvent getDeathSound() {
    return SoundEvents.SKELETON_DEATH;
  }
}
