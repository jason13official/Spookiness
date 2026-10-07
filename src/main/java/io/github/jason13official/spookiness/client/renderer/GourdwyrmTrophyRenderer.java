package io.github.jason13official.spookiness.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jason13official.spookiness.block.entity.GourdwyrmTrophyBlockEntity;
import io.github.jason13official.spookiness.client.renderer.state.BlockPartsRenderState;
import io.github.jason13official.spookiness.client.renderer.state.GourdwyrmTrophyRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class GourdwyrmTrophyRenderer implements BlockEntityRenderer<GourdwyrmTrophyBlockEntity, GourdwyrmTrophyRenderState> {

  private static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

  private static final int SEGMENTS = 8;
  private static final float SPEED = 0.06F;
  private static final float SPACING = 0.45F;
  private static final float RADIUS = 0.26F;
  private static final float HEIGHT = 0.5F;
  private static final float BOB = 0.12F;
  private static final float HEAD_SCALE = 0.22F;
  private static final float SEGMENT_SCALE = 0.16F;
  private static final float TAIL_SCALE = 0.09F;
  private static final float LIT_SPIN = 12.0F;

  private final BlockModelResolver blockModelResolver;

  public GourdwyrmTrophyRenderer(BlockEntityRendererProvider.Context context) {
    this.blockModelResolver = context.blockModelResolver();
  }

  @Override
  public GourdwyrmTrophyRenderState createRenderState() {
    return new GourdwyrmTrophyRenderState();
  }

  private static Vec3 pathAt(float t) {
    return new Vec3(0.5 + Mth.cos(t) * RADIUS, HEIGHT + Mth.sin(t * 2.0F) * BOB, 0.5 + Mth.sin(t) * RADIUS);
  }

  private static boolean isLit(int index) {
    return index == 2 || index == 5;
  }

  @Override
  public void extractRenderState(GourdwyrmTrophyBlockEntity blockEntity, GourdwyrmTrophyRenderState state, float partialTicks, Vec3 cameraPosition,
      ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
    BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
    state.used = 0;

    long gameTime = blockEntity.getLevel() == null ? 0L : blockEntity.getLevel().getGameTime();
    float ticks = (gameTime % 24000L) + partialTicks;
    float head = ticks * SPEED + blockEntity.getBlockPos().hashCode() * 0.37F;

    for (int i = 0; i <= SEGMENTS; i++) {
      float t = head - i * SPACING;
      Vec3 pos = pathAt(t);
      Vec3 ahead = pathAt(t + 0.05F);
      float yaw = (float) (Mth.atan2(ahead.z - pos.z, ahead.x - pos.x) * Mth.RAD_TO_DEG) - 90.0F;
      float pitch = (float) (Mth.atan2(ahead.y - pos.y, ahead.subtract(pos).horizontalDistance()) * Mth.RAD_TO_DEG);

      BlockState block;
      float scale;
      float yRot = yaw + 180.0F;
      if (i == 0) {
        block = Blocks.JACK_O_LANTERN.defaultBlockState();
        scale = HEAD_SCALE;
      } else if (isLit(i)) {
        block = Blocks.JACK_O_LANTERN.defaultBlockState();
        scale = SEGMENT_SCALE;
        yRot = ticks * LIT_SPIN + i * 40.0F;
        pitch = 0.0F;
      } else {
        block = Blocks.CARVED_PUMPKIN.defaultBlockState();
        scale = Mth.lerp((float) i / SEGMENTS, SEGMENT_SCALE, TAIL_SCALE);
      }

      BlockPartsRenderState.Part part = state.next();
      this.blockModelResolver.update(part.model, block, BLOCK_DISPLAY_CONTEXT);
      part.x = (float) pos.x;
      part.y = (float) pos.y;
      part.z = (float) pos.z;
      part.scale = scale;
      part.yRot = yRot;
      part.xRot = pitch;
      part.zRot = 0.0F;
    }
  }

  @Override
  public void submit(GourdwyrmTrophyRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {

    for (int i = 0; i < state.used; i++) {
      BlockPartsRenderState.Part part = state.parts.get(i);
      poseStack.pushPose();
      poseStack.translate(part.x, part.y, part.z);
      poseStack.mulPose(Axis.YP.rotationDegrees(-part.yRot));
      poseStack.mulPose(Axis.XP.rotationDegrees(part.xRot));
      poseStack.scale(part.scale, part.scale, part.scale);
      poseStack.translate(-0.5F, -0.5F, -0.5F);
      part.model.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
      poseStack.popPose();
    }
  }
}
