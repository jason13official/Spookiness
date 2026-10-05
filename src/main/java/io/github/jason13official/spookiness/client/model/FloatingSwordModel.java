package io.github.jason13official.spookiness.client.model;

import io.github.jason13official.spookiness.client.renderer.state.FloatingSwordRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.illager.AbstractIllager;

public class FloatingSwordModel extends IllagerModel<FloatingSwordRenderState> {

  private final ModelPart head;
  private final ModelPart body;
  private final ModelPart rightArm;
  private final ModelPart leftArm;
  private final ModelPart rightLeg;
  private final ModelPart leftLeg;

  public FloatingSwordModel(ModelPart root) {
    super(root);
    this.head = root.getChild("head");
    this.body = root.getChild("body");
    this.rightArm = root.getChild("right_arm");
    this.leftArm = root.getChild("left_arm");
    this.rightLeg = root.getChild("right_leg");
    this.leftLeg = root.getChild("left_leg");
  }

  @Override
  public void setupAnim(FloatingSwordRenderState state) {
    super.setupAnim(state);

    float t = state.ageInTicks;
    float drift = Mth.sin(t * 0.05F);

    this.body.zRot = drift * 0.04F;
    this.head.zRot = Mth.sin(t * 0.05F + 1.0F) * 0.08F;

    if (state.armPose != AbstractIllager.IllagerArmPose.ATTACKING) {
      this.rightArm.xRot += Mth.sin(t * 0.067F) * 0.18F - 0.15F;
      this.rightArm.zRot += Mth.cos(t * 0.09F) * 0.12F + 0.12F;
    }
    this.leftArm.xRot += Mth.sin(t * 0.067F + 1.7F) * 0.18F - 0.15F;
    this.leftArm.zRot -= Mth.cos(t * 0.09F + 1.7F) * 0.12F + 0.12F;

    this.rightLeg.xRot = 0.35F + Mth.sin(t * 0.08F) * 0.15F;
    this.leftLeg.xRot = 0.3F + Mth.sin(t * 0.08F + 2.1F) * 0.15F;
    this.rightLeg.zRot = 0.06F + drift * 0.05F;
    this.leftLeg.zRot = -0.06F + drift * 0.05F;
  }
}
