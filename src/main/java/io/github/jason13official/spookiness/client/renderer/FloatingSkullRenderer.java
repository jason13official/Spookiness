package io.github.jason13official.spookiness.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jason13official.spookiness.client.renderer.state.FloatingSkullRenderState;
import io.github.jason13official.spookiness.entity.FloatingSkull;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.skull.SkullModel;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.SkullBlock;

public class FloatingSkullRenderer extends EntityRenderer<FloatingSkull, FloatingSkullRenderState> {

  private static final float CHATTER_DEGREES = 20.0F;

  private final SkullModelBase model;
  private final RenderType renderType;

  public FloatingSkullRenderer(Context context) {
    super(context);
    this.model = new SkullModel(context.bakeLayer(ModelLayers.SKELETON_SKULL));
    this.renderType = SkullBlockRenderer.getSkullRenderType(SkullBlock.Types.SKELETON, null);
    this.shadowRadius = 0.25F;
  }

  @Override
  public FloatingSkullRenderState createRenderState() {

    return new FloatingSkullRenderState();
  }

  @Override
  public void extractRenderState(FloatingSkull entity, FloatingSkullRenderState state, float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);

    state.yRot = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
    state.xRot = entity.getXRot(partialTicks);
    state.chatter = entity.getChatter(partialTicks);
  }

  @Override
  public void submit(FloatingSkullRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {

    poseStack.pushPose();
    poseStack.translate(0.0F, Mth.sin(state.ageInTicks * 0.1F) * 0.06F, 0.0F);
    poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.yRot));
    poseStack.scale(-1.0F, -1.0F, 1.0F);

    SkullModelBase.State modelState = new SkullModelBase.State();
    modelState.xRot = state.xRot + state.chatter * CHATTER_DEGREES;
    submitNodeCollector.submitModel(this.model, modelState, poseStack, this.renderType, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor, null);
    poseStack.popPose();

    super.submit(state, poseStack, submitNodeCollector, camera);
  }
}
