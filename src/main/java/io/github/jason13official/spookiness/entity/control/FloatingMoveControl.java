package io.github.jason13official.spookiness.entity.control;

import io.github.jason13official.spookiness.util.SpookyMath;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class FloatingMoveControl extends MoveControl {

  private static final double CLIMB_FACTOR = 0.6;

  private final double steering;
  private final float maxTurn;

  public FloatingMoveControl(Mob mob, double steering, float maxTurn) {
    super(mob);
    this.steering = steering;
    this.maxTurn = maxTurn;
  }

  @Override
  public void tick() {

    this.mob.setSpeed(0.0F);
    this.mob.setYya(0.0F);

    if (this.operation != Operation.MOVE_TO) {
      return;
    }

    Vec3 delta = new Vec3(this.wantedX - this.mob.getX(), this.wantedY - this.mob.getY(), this.wantedZ - this.mob.getZ());
    double distance = delta.length();
    if (distance < 0.25) {
      this.operation = Operation.WAIT;
      return;
    }

    double maxSpeed = this.speedModifier * this.mob.getAttributeValue(Attributes.FLYING_SPEED);
    Vec3 velocity = this.mob.getDeltaMovement();
    Vec3 desired = delta.scale(maxSpeed / distance);
    Vec3 steered = velocity.add(desired.subtract(velocity).scale(this.steering));
    if (this.mob.horizontalCollision) {
      steered = new Vec3(steered.x, Math.max(steered.y, maxSpeed * CLIMB_FACTOR), steered.z);
    }
    this.mob.setDeltaMovement(steered);

    if (delta.horizontalDistanceSqr() > 1.0E-4) {
      float yRot = SpookyMath.yawToward(delta);
      this.mob.setYRot(this.rotlerp(this.mob.getYRot(), yRot, this.maxTurn));
      this.mob.yBodyRot = this.mob.getYRot();
    }
  }
}
