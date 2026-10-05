package io.github.jason13official.spookiness.entity.control;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.Vec3;

public class FloatingMoveControl extends MoveControl {

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
    this.mob.setDeltaMovement(velocity.add(desired.subtract(velocity).scale(this.steering)));

    if (delta.horizontalDistanceSqr() > 1.0E-4) {
      float yRot = (float) (Mth.atan2(delta.z, delta.x) * Mth.RAD_TO_DEG) - 90.0F;
      this.mob.setYRot(this.rotlerp(this.mob.getYRot(), yRot, this.maxTurn));
      this.mob.yBodyRot = this.mob.getYRot();
    }
  }
}
