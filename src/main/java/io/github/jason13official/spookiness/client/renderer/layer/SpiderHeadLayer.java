package io.github.jason13official.spookiness.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class SpiderHeadLayer extends RenderLayer<LivingEntityRenderState, SpiderModel> {

  private final ModelPart head;

  public SpiderHeadLayer(RenderLayerParent<LivingEntityRenderState, SpiderModel> renderer) {
    super(renderer);
    this.head = renderer.getModel().root().getChild("head");
  }

  @Override
  public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, LivingEntityRenderState state, float yRot, float xRot) {

    if (state.headItem.isEmpty()) {
      return;
    }

    poseStack.pushPose();
    this.getParentModel().root().translateAndRotate(poseStack);
    this.head.translateAndRotate(poseStack);
    poseStack.translate(0.0F, 0.0F, -0.25F);
    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
    poseStack.scale(0.625F, -0.625F, -0.625F);
    state.headItem.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
    poseStack.popPose();
  }
}
