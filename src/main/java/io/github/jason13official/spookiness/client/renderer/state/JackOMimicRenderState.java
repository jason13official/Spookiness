package io.github.jason13official.spookiness.client.renderer.state;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class JackOMimicRenderState extends LivingEntityRenderState {

  public float jumpCompletion;
  public final AnimationState yapAnimationState = new AnimationState();
}
