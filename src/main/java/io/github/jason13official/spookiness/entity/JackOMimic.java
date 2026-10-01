package io.github.jason13official.spookiness.entity;

import io.github.jason13official.spookiness.entity.control.JackOMimicJumpControl;
import io.github.jason13official.spookiness.entity.control.JackOMimicMoveControl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.rabbit.Rabbit;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public class JackOMimic extends PathfinderMob {

  private static final int JUMP_DELAY_TICKS = 10;
  private static final int JUMP_DURATION_IN_TICKS = 20;

  public final AnimationState yapAnimationState = new AnimationState();

  private int jumpTicks;
  private int jumpDuration;

  private boolean wasOnGround;
  private int jumpDelayTicks;

  public JackOMimic(EntityType<? extends JackOMimic> type, Level level) {
    super(type, level);
    this.jumpControl = new JackOMimicJumpControl(this);
    this.moveControl = new JackOMimicMoveControl(this);
    this.setSpeedModifier(0.0F);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Animal.createAnimalAttributes().add(Attributes.MAX_HEALTH, (double)3.0F).add(Attributes.MOVEMENT_SPEED, (double)0.3F).add(Attributes.ATTACK_DAMAGE, (double)3.0F);
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

  public void setSpeedModifier(double speed) {
    this.getNavigation().setSpeedModifier(speed);
    this.moveControl.setWantedPosition(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ(), speed);
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
  public void aiStep() {
    super.aiStep();
    if (this.jumpTicks != this.jumpDuration) {
      ++this.jumpTicks;
    } else if (this.jumpDuration != 0) {
      this.jumpTicks = 0;
      this.jumpDuration = 0;
      this.setJumping(false);
    }
  }

  @Override
  public void baseTick() {
    super.baseTick();
    if (this.level().isClientSide()) {
      this.setupAnimationStates();
    }
  }

  @Override
  public void tick() {
    super.tick();

    // this.yapAnimationState.startIfStopped(this.tickCount);
  }

  @Override
  public void handleEntityEvent(byte id) {
    if (id == EntityEvent.JUMP) {
      this.spawnSprintParticle();
      this.jumpDuration = JUMP_DURATION_IN_TICKS;
      this.jumpTicks = 0;
    } else {
      super.handleEntityEvent(id);
    }

  }

  @Override
  protected float getJumpPower() {
    float baseJumpPower = 0.3F;
    if (this.moveControl.getSpeedModifier() <= 0.6) {
      baseJumpPower = 0.2F;
    }

    Path path = this.navigation.getPath();
    if (path != null && !path.isDone()) {
      Vec3 currentPos = path.getNextEntityPos(this);
      if (currentPos.y > this.getY() + (double) 0.5F) {
        baseJumpPower = 0.5F;
      }
    }

    if (this.horizontalCollision || this.jumping && this.moveControl.getWantedY() > this.getY() + (double) 0.5F) {
      baseJumpPower = 0.5F;
    }

    return super.getJumpPower(baseJumpPower / 0.42F);
  }

  @Override
  public void jumpFromGround() {
    super.jumpFromGround();
    double speedModifier = this.moveControl.getSpeedModifier();
    if (speedModifier > (double) 0.0F) {
      double current = this.getDeltaMovement().horizontalDistanceSqr();
      if (current < 0.01) {
        this.moveRelative(0.1F, new Vec3(0.0F, this.isBaby() ? (double) 0.5F : (double) 1.5F, 1.0F));
      }
    }

    if (!this.level().isClientSide()) {
      this.level().broadcastEntityEvent(this, (byte) 1);
    }

  }

  public float getJumpCompletion(float a) {
    return this.jumpDuration == 0 ? 0.0F : ((float) this.jumpTicks + a) / (float) this.jumpDuration;
  }

  private void setupAnimationStates() {
    if (this.jumpTicks > 0) {
      this.yapAnimationState.startIfStopped(this.tickCount);
    } else {
      this.yapAnimationState.stop();
    }
  }

  public void startJumping() {
    this.setJumping(true);
    this.jumpDuration = JUMP_DURATION_IN_TICKS;
    this.jumpTicks = 0;
  }

  @Override
  public void customServerAiStep(ServerLevel level) {
    if (this.jumpDelayTicks > 0) {
      --this.jumpDelayTicks;
    }

    if (this.onGround()) {
      if (!this.wasOnGround) {
        this.setJumping(false);
        this.checkLandingDelay();
      }

      if (this.jumpDelayTicks == 0) {
        LivingEntity target = this.getTarget();
        if (target != null && this.distanceToSqr(target) < 16.0) {
          this.facePoint(target.getX(), target.getZ());
          this.moveControl.setWantedPosition(target.getX(), target.getY(), target.getZ(), this.moveControl.getSpeedModifier());
          this.startJumping();
          this.wasOnGround = true;
        }
      }

      JackOMimicJumpControl jumpControl = (JackOMimicJumpControl) this.jumpControl;
      if (!jumpControl.wantJump()) {
        if (this.moveControl.hasWanted() && this.jumpDelayTicks == 0) {
          Path path = this.navigation.getPath();
          Vec3 pos = new Vec3(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ());
          if (path != null && !path.isDone()) {
            pos = path.getNextEntityPos(this);
          }

          this.facePoint(pos.x, pos.z);
          this.startJumping();
        }
      } else if (!jumpControl.canJump()) {
        this.enableJumpControl();
      }
    }

    this.wasOnGround = this.onGround();
  }

  private void facePoint(double faceX, double faceZ) {
    this.setYRot((float) (Mth.atan2(faceZ - this.getZ(), faceX - this.getX()) * (double) 180.0F / (double) (float) Math.PI) - 90.0F);
  }

  private void enableJumpControl() {
    ((JackOMimicJumpControl) this.jumpControl).setCanJump(true);
  }

  private void disableJumpControl() {
    ((JackOMimicJumpControl) this.jumpControl).setCanJump(false);
  }

  private void setLandingDelay() {
    this.jumpDelayTicks = this.moveControl.getSpeedModifier() < 2.2 ? JUMP_DELAY_TICKS : 3;
  }

  private void checkLandingDelay() {
    this.setLandingDelay();
    this.disableJumpControl();
  }

  public boolean canSpawnSprintParticle() {
    return false;
  }

  protected SoundEvent getJumpSound() {
    return SoundEvents.RABBIT_JUMP;
  }

  protected SoundEvent getAmbientSound() {
    return SoundEvents.RABBIT_AMBIENT;
  }

  protected SoundEvent getHurtSound(DamageSource source) {
    return SoundEvents.RABBIT_HURT;
  }

  protected SoundEvent getDeathSound() {
    return SoundEvents.RABBIT_DEATH;
  }
}
