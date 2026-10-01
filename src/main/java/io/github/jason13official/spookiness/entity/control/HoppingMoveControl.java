package io.github.jason13official.spookiness.entity.control;

import io.github.jason13official.spookiness.entity.JumpingPathfinderMob;
import net.minecraft.world.entity.ai.control.MoveControl;

public class HoppingMoveControl extends MoveControl {

  private final JumpingPathfinderMob mob;
  private double nextJumpSpeed;

  public HoppingMoveControl(JumpingPathfinderMob mob) {
    super(mob);
    this.mob = mob;
  }

  public void tick() {
    if (this.mob.onGround() && !this.mob.isJumping() && !this.mob.getHoppingJumpControl().wantJump()) {
      this.mob.setSpeedModifier(0.0F);
    } else if (this.hasWanted() || this.operation == MoveControl.Operation.JUMPING) {
      this.mob.setSpeedModifier(this.nextJumpSpeed);
    }

    super.tick();
  }

  public void setWantedPosition(double x, double y, double z, double speedModifier) {
    if (this.mob.isInWater()) {
      speedModifier = this.mob.getWaterSpeedModifier();
    }

    super.setWantedPosition(x, y, z, speedModifier);
    if (speedModifier > (double) 0.0F) {
      this.nextJumpSpeed = speedModifier;
    }

  }
}
