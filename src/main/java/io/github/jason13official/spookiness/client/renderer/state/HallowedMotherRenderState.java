package io.github.jason13official.spookiness.client.renderer.state;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.AnimationState;

public class HallowedMotherRenderState extends LivingEntityRenderState {

  public final AnimationState spitAnimationState = new AnimationState();
  public float breathe;
}
