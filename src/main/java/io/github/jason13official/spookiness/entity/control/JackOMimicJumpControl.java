package io.github.jason13official.spookiness.entity.control;

import io.github.jason13official.spookiness.entity.JackOMimic;
import net.minecraft.world.entity.ai.control.JumpControl;

public class JackOMimicJumpControl extends JumpControl {
  private final JackOMimic mimic;
  private boolean canJump;

  public JackOMimicJumpControl(JackOMimic mimic) {
    super(mimic);
    this.mimic = mimic;
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
      this.mimic.startJumping();
      this.jump = false;
    }

  }
}
