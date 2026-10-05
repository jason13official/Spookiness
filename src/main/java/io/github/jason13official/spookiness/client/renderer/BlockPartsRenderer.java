package io.github.jason13official.spookiness.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jason13official.spookiness.client.renderer.state.BlockPartsRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BlockPartsRenderer<T extends Entity> extends EntityRenderer<T, BlockPartsRenderState> {

  private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

  private final BlockModelResolver blockModelResolver;

  protected BlockPartsRenderer(Context context, float shadowRadius) {
    super(context);
    this.blockModelResolver = context.getBlockModelResolver();
    this.shadowRadius = shadowRadius;
  }

  @Override
  public BlockPartsRenderState createRenderState() {
    return new BlockPartsRenderState();
  }

  @Override
  public void extractRenderState(T entity, BlockPartsRenderState state, float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);
    state.used = 0;
    state.hurt = entity instanceof LivingEntity living && (living.hurtTime > 0 || living.deathTime > 0);
    this.collectParts(entity, state, partialTicks);
  }

  protected abstract void collectParts(T entity, BlockPartsRenderState state, float partialTicks);

  protected void part(BlockPartsRenderState state, BlockState block, double x, double y, double z, float scale, float yRot, float xRot) {

    BlockPartsRenderState.Part part = state.next();
    this.blockModelResolver.update(part.model, block, BLOCK_DISPLAY_CONTEXT);
    part.x = (float) x;
    part.y = (float) y;
    part.z = (float) z;
    part.scale = scale;
    part.yRot = yRot;
    part.xRot = xRot;
  }

  @Override
  public void submit(BlockPartsRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {

    int overlay = OverlayTexture.pack(0.0F, state.hurt);
    for (int i = 0; i < state.used; i++) {
      BlockPartsRenderState.Part part = state.parts.get(i);
      poseStack.pushPose();
      poseStack.translate(part.x, part.y, part.z);
      poseStack.mulPose(Axis.YP.rotationDegrees(-part.yRot));
      poseStack.mulPose(Axis.XP.rotationDegrees(part.xRot));
      poseStack.scale(part.scale, part.scale, part.scale);
      poseStack.translate(-0.5F, 0.0F, -0.5F);
      part.model.submit(poseStack, submitNodeCollector, state.lightCoords, overlay, state.outlineColor);
      poseStack.popPose();
    }
    super.submit(state, poseStack, submitNodeCollector, camera);
  }
}
