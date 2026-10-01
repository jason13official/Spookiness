package io.github.jason13official.spookiness.entity;

import io.github.jason13official.spookiness.entity.control.HoppingJumpControl;
import io.github.jason13official.spookiness.entity.control.HoppingMoveControl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

public abstract class JumpingPathfinderMob extends PathfinderMob {

  private int jumpTicks;
  private int jumpDuration;

  private boolean wasOnGround;
  private int jumpDelayTicks;

  protected JumpingPathfinderMob(EntityType<? extends JumpingPathfinderMob> type, Level level) {
    super(type, level);
    this.jumpControl = new HoppingJumpControl(this);
    this.moveControl = new HoppingMoveControl(this);
    this.setSpeedModifier(0.0F);
  }

  protected abstract SoundEvent getJumpSound();

  protected int getJumpDurationTicks() {
    return 20;
  }

  protected int getLandingDelayTicks() {
    return this.moveControl.getSpeedModifier() < 2.2 ? 10 : 3;
  }

  protected double getPounceRangeSqr() {
    return 16.0;
  }

  protected float getWalkJumpPower() {
    return 0.2F;
  }

  protected float getRunJumpPower() {
    return 0.3F;
  }

  protected float getClimbJumpPower() {
    return 0.5F;
  }

  public double getWaterSpeedModifier() {
    return 1.5;
  }

  public HoppingJumpControl getHoppingJumpControl() {
    return (HoppingJumpControl) this.jumpControl;
  }

  public void setSpeedModifier(double speed) {
    this.getNavigation().setSpeedModifier(speed);
    this.moveControl.setWantedPosition(this.moveControl.getWantedX(), this.moveControl.getWantedY(), this.moveControl.getWantedZ(), speed);
  }

  public boolean isMidJump() {
    return this.jumpTicks > 0;
  }

  public float getJumpCompletion(float a) {
    return this.jumpDuration == 0 ? 0.0F : ((float) this.jumpTicks + a) / (float) this.jumpDuration;
  }

  public void startJumping() {
    this.setJumping(true);
    this.jumpDuration = this.getJumpDurationTicks();
    this.jumpTicks = 0;
  }

  @Override
  public void setJumping(boolean jump) {
    super.setJumping(jump);
    if (jump) {
      this.playSound(this.getJumpSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F) * 0.8F);
    }
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
  public void handleEntityEvent(byte id) {
    if (id == EntityEvent.JUMP) {
      this.spawnSprintParticle();
      this.jumpDuration = this.getJumpDurationTicks();
      this.jumpTicks = 0;
    } else {
      super.handleEntityEvent(id);
    }

  }

  @Override
  protected float getJumpPower() {
    float baseJumpPower = this.moveControl.getSpeedModifier() <= 0.6 ? this.getWalkJumpPower() : this.getRunJumpPower();

    Path path = this.navigation.getPath();
    if (path != null && !path.isDone()) {
      Vec3 currentPos = path.getNextEntityPos(this);
      if (currentPos.y > this.getY() + (double) 0.5F) {
        baseJumpPower = this.getClimbJumpPower();
      }
    }

    if (this.horizontalCollision || this.jumping && this.moveControl.getWantedY() > this.getY() + (double) 0.5F) {
      baseJumpPower = this.getClimbJumpPower();
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
      this.level().broadcastEntityEvent(this, EntityEvent.JUMP);
    }

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
        if (target != null && this.distanceToSqr(target) < this.getPounceRangeSqr()) {
          this.facePoint(target.getX(), target.getZ());
          this.moveControl.setWantedPosition(target.getX(), target.getY(), target.getZ(), this.moveControl.getSpeedModifier());
          this.startJumping();
          this.wasOnGround = true;
        }
      }

      HoppingJumpControl jumpControl = this.getHoppingJumpControl();
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
        jumpControl.setCanJump(true);
      }
    }

    this.wasOnGround = this.onGround();
  }

  protected void facePoint(double faceX, double faceZ) {
    this.setYRot((float) (Mth.atan2(faceZ - this.getZ(), faceX - this.getX()) * (double) 180.0F / (double) (float) Math.PI) - 90.0F);
  }

  private void checkLandingDelay() {
    this.jumpDelayTicks = this.getLandingDelayTicks();
    this.getHoppingJumpControl().setCanJump(false);
  }
}
