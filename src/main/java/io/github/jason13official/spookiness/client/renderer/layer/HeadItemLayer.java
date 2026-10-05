package io.github.jason13official.spookiness.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class HeadItemLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {

  private final float headCenterZ;

  public HeadItemLayer(RenderLayerParent<S, M> renderer, float headCenterZ) {
    super(renderer);
    this.headCenterZ = headCenterZ;
  }

  @Override
  public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, S state, float yRot, float xRot) {

    if (state.headItem.isEmpty() || state.isBaby) {
      return;
    }

    M model = this.getParentModel();
    poseStack.pushPose();
    model.root().translateAndRotate(poseStack);
    model.root().getChild("head").translateAndRotate(poseStack);
    poseStack.translate(0.0F, 0.0F, this.headCenterZ);
    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
    poseStack.scale(0.625F, -0.625F, -0.625F);
    state.headItem.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
    poseStack.popPose();
  }
}
