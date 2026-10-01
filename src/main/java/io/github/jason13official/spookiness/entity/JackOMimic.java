package io.github.jason13official.spookiness.entity;

import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class JackOMimic extends LivingEntity {

  public final AnimationState yapAnimationState = new AnimationState();

  public JackOMimic(EntityType<? extends JackOMimic> type, Level level) {
    super(type, level);
  }

  @Override
  public HumanoidArm getMainArm() {

    return HumanoidArm.RIGHT;
  }

  @Override
  public void tick() {
    super.tick();

    this.yapAnimationState.startIfStopped(this.tickCount);
  }
}
