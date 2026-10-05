package io.github.jason13official.spookiness.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jason13official.spookiness.client.renderer.state.FloatingCandlesRenderState;
import io.github.jason13official.spookiness.entity.FloatingCandles;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

public class FloatingCandlesRenderer extends EntityRenderer<FloatingCandles, FloatingCandlesRenderState> {

  private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

  private final BlockModelResolver blockModelResolver;

  public FloatingCandlesRenderer(Context context) {
    super(context);
    this.blockModelResolver = context.getBlockModelResolver();
    this.shadowRadius = 0.25F;
  }

  @Override
  public FloatingCandlesRenderState createRenderState() {

    return new FloatingCandlesRenderState();
  }

  @Override
  public void extractRenderState(FloatingCandles entity, FloatingCandlesRenderState state, float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);

    this.blockModelResolver.update(state.candles, entity.getCandleState(), BLOCK_DISPLAY_CONTEXT);
    state.deathTime = entity.deathTime > 0 ? entity.deathTime + partialTicks : 0.0F;
  }

  @Override
  public void submit(FloatingCandlesRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {

    poseStack.pushPose();
    if (state.deathTime > 0.0F) {
      float fall = Math.min(Mth.sqrt((state.deathTime - 1.0F) / 20.0F * 1.6F), 1.0F);
      poseStack.mulPose(Axis.ZP.rotationDegrees(fall * 90.0F));
    }
    poseStack.translate(-0.5F, 0.0F, -0.5F);
    state.candles.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
    poseStack.popPose();

    super.submit(state, poseStack, submitNodeCollector, camera);
  }
}
