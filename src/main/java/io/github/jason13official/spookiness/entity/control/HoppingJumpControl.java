package io.github.jason13official.spookiness.entity.control;

import io.github.jason13official.spookiness.entity.JumpingPathfinderMob;
import net.minecraft.world.entity.ai.control.JumpControl;

public class HoppingJumpControl extends JumpControl {

  private final JumpingPathfinderMob mob;
  private boolean canJump;

  public HoppingJumpControl(JumpingPathfinderMob mob) {
    super(mob);
    this.mob = mob;
  }

  public boolean wantJump() {
    return this.jump;
  }

  public boolean canJump() {
    return this.canJump;
  }

  public void setCanJump(boolean canJump) {
    this.canJump = canJump;
  }

  public void tick() {
    if (this.jump) {
      this.mob.startJumping();
      this.jump = false;
    }

  }
}
