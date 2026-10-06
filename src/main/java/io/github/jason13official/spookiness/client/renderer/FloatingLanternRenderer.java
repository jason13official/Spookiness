package io.github.jason13official.spookiness.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jason13official.spookiness.client.renderer.state.FloatingLanternRenderState;
import io.github.jason13official.spookiness.entity.FloatingLantern;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

public class FloatingLanternRenderer extends EntityRenderer<FloatingLantern, FloatingLanternRenderState> {

  private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

  private final BlockModelResolver blockModelResolver;

  public FloatingLanternRenderer(Context context) {
    super(context);
    this.blockModelResolver = context.getBlockModelResolver();
    this.shadowRadius = 0.2F;
  }

  @Override
  public FloatingLanternRenderState createRenderState() {

    return new FloatingLanternRenderState();
  }

  @Override
  public void extractRenderState(FloatingLantern entity, FloatingLanternRenderState state, float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);

    this.blockModelResolver.update(state.lantern, entity.getLanternState(), BLOCK_DISPLAY_CONTEXT);
  }

  @Override
  public void submit(FloatingLanternRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {

    poseStack.pushPose();
    poseStack.translate(0.0F, 0.9F, 0.0F);
    poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.sin(state.ageInTicks * 0.07F) * 8.0F));
    poseStack.mulPose(Axis.XP.rotationDegrees(Mth.cos(state.ageInTicks * 0.05F) * 6.0F));
    poseStack.translate(-0.5F, -1.0F, -0.5F);
    state.lantern.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
    poseStack.popPose();

    super.submit(state, poseStack, submitNodeCollector, camera);
  }
}
