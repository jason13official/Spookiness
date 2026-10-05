package io.github.jason13official.spookiness.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jason13official.spookiness.client.renderer.state.BooklessEnchantTableRenderState;
import io.github.jason13official.spookiness.registry.ModAttachments;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.blockentity.state.EnchantTableRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class BooklessEnchantTableRenderer extends EnchantTableRenderer {

  public BooklessEnchantTableRenderer(BlockEntityRendererProvider.Context context) {
    super(context);
  }

  @Override
  public EnchantTableRenderState createRenderState() {

    return new BooklessEnchantTableRenderState();
  }

  @Override
  public void extractRenderState(EnchantingTableBlockEntity blockEntity, EnchantTableRenderState state, float partialTicks, Vec3 cameraPosition,
      ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
    super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);

    if (state instanceof BooklessEnchantTableRenderState bookless) {
      bookless.bookAwakened = blockEntity.getData(ModAttachments.BOOK_AWAKENED);
    }
  }

  @Override
  public void submit(EnchantTableRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {

    if (state instanceof BooklessEnchantTableRenderState bookless && bookless.bookAwakened) {
      return;
    }

    super.submit(state, poseStack, submitNodeCollector, camera);
  }
}
