package io.github.jason13official.spookiness.client.model;

import io.github.jason13official.spookiness.client.anim.HallowedMotherAnimations;
import io.github.jason13official.spookiness.client.renderer.state.HallowedMotherRenderState;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

public class HallowedMotherModel extends EntityModel<HallowedMotherRenderState> {

  private static final float GAPE_ANGLE = 65.0F * Mth.DEG_TO_RAD;

  private final ModelPart head;
  private final ModelPart cranium;
  private final KeyframeAnimation spitAnimation;

  public HallowedMotherModel(ModelPart root) {
    super(root);
    this.head = root.getChild("head");
    this.cranium = this.head.getChild("cranium");
    this.spitAnimation = HallowedMotherAnimations.SPIT.bake(root);
  }

  @Override
  public void setupAnim(HallowedMotherRenderState state) {

    super.setupAnim(state);
    this.head.xScale *= state.breathe;
    this.head.yScale *= state.breathe;
    this.head.zScale *= state.breathe;
    this.spitAnimation.apply(state.spitAnimationState, state.ageInTicks);
    this.cranium.xRot = Math.min(this.cranium.xRot, -state.gape * GAPE_ANGLE);
  }
}
