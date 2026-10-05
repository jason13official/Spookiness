package io.github.jason13official.spookiness.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jason13official.spookiness.block.SoullessJackOMimicBlock;
import io.github.jason13official.spookiness.block.entity.SoullessJackOMimicBlockEntity;
import io.github.jason13official.spookiness.client.model.JackOMimicModel;
import io.github.jason13official.spookiness.client.model.SoullessJackOMimicModel;
import io.github.jason13official.spookiness.client.renderer.state.SoullessJackOMimicRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SoullessJackOMimicRenderer implements BlockEntityRenderer<SoullessJackOMimicBlockEntity, SoullessJackOMimicRenderState> {

  private static final float MODEL_OFFSET = 1.5F;
  private static final float ITEM_HEIGHT = 0.28F;
  private static final float ITEM_FORWARD = 0.05F;
  private static final float ITEM_SCALE = 0.45F;

  private final SoullessJackOMimicModel model;
  private final ItemModelResolver itemModelResolver;

  public SoullessJackOMimicRenderer(BlockEntityRendererProvider.Context context) {
    this.model = new SoullessJackOMimicModel(context.bakeLayer(JackOMimicModel.LAYER_LOCATION));
    this.itemModelResolver = context.itemModelResolver();
  }

  @Override
  public SoullessJackOMimicRenderState createRenderState() {
    return new SoullessJackOMimicRenderState();
  }

  @Override
  public void extractRenderState(SoullessJackOMimicBlockEntity blockEntity, SoullessJackOMimicRenderState state, float partialTicks, Vec3 cameraPosition,
      ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
    BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
    state.facing = blockEntity.getBlockState().getValue(SoullessJackOMimicBlock.FACING);
    state.openness = blockEntity.getOpenness(partialTicks);
    this.itemModelResolver.updateForTopItem(state.item, blockEntity.getItem(), ItemDisplayContext.FIXED, blockEntity.getLevel(), null,
        (int) blockEntity.getBlockPos().asLong());
  }

  @Override
  public void submit(SoullessJackOMimicRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {

    poseStack.pushPose();
    poseStack.translate(0.5F, 0.0F, 0.5F);
    poseStack.mulPose(Axis.YP.rotationDegrees(-state.facing.toYRot()));

    if (!state.item.isEmpty()) {
      poseStack.pushPose();
      poseStack.translate(0.0F, ITEM_HEIGHT, ITEM_FORWARD);
      poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
      state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
      poseStack.popPose();
    }

    poseStack.translate(0.0F, MODEL_OFFSET, 0.0F);
    poseStack.scale(1.0F, -1.0F, -1.0F);
    submitNodeCollector.submitModel(this.model, state.openness, poseStack, this.model.renderType(SoullessJackOMimicModel.TEXTURE), state.lightCoords,
        OverlayTexture.NO_OVERLAY, -1, null, 0, state.breakProgress);
    poseStack.popPose();
  }
}
