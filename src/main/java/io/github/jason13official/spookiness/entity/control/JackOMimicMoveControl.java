package io.github.jason13official.spookiness.entity.control;

import io.github.jason13official.spookiness.entity.JackOMimic;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.animal.rabbit.Rabbit.RabbitJumpControl;

public class JackOMimicMoveControl extends MoveControl {
  private final JackOMimic mimic;
  private double nextJumpSpeed;

  public JackOMimicMoveControl(JackOMimic mimic) {
    super(mimic);
    this.mimic = mimic;
  }

  public void tick() {
    if (this.mimic.onGround() && !this.mimic.isJumping() && !((JackOMimicJumpControl)this.mimic.getJumpControl()).wantJump()) {
      this.mimic.setSpeedModifier((double)0.0F);
    } else if (this.hasWanted() || this.operation == net.minecraft.world.entity.ai.control.MoveControl.Operation.JUMPING) {
      this.mimic.setSpeedModifier(this.nextJumpSpeed);
    }

    super.tick();
  }

  public void setWantedPosition(double x, double y, double z, double speedModifier) {
    if (this.mimic.isInWater()) {
      speedModifier = (double)1.5F;
    }

    super.setWantedPosition(x, y, z, speedModifier);
    if (speedModifier > (double)0.0F) {
      this.nextJumpSpeed = speedModifier;
    }

  }
}
