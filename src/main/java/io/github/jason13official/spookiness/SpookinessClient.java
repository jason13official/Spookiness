package io.github.jason13official.spookiness;

import io.github.jason13official.spookiness.client.model.FloatingBookModel;
import io.github.jason13official.spookiness.client.model.JackOMimicModel;
import io.github.jason13official.spookiness.client.renderer.FloatingBookRenderer;
import io.github.jason13official.spookiness.client.renderer.FloatingCandlesRenderer;
import io.github.jason13official.spookiness.client.renderer.FloatingSwordRenderer;
import io.github.jason13official.spookiness.client.renderer.JackOMimicRenderer;
import io.github.jason13official.spookiness.client.renderer.SpectralJackOMimicRenderer;
import io.github.jason13official.spookiness.client.renderer.BooklessEnchantTableRenderer;
import io.github.jason13official.spookiness.client.renderer.layer.HeadItemLayer;
import io.github.jason13official.spookiness.registry.ModEntities;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class SpookinessClient {

  public SpookinessClient(final IEventBus modEventBus) {

    // EntityRenderersEvent.RegisterLayerDefinitions
    modEventBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> {

      event.registerLayerDefinition(JackOMimicModel.LAYER_LOCATION, JackOMimicModel::createBodyLayer);
      event.registerLayerDefinition(FloatingBookModel.LAYER_LOCATION, FloatingBookModel::createBodyLayer);
    });

    // EntityRenderersEvent.RegisterRenderers
    modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {

      event.registerEntityRenderer(ModEntities.JACK_O_MIMIC, JackOMimicRenderer::new);
      event.registerEntityRenderer(ModEntities.FLOATING_CANDLES, FloatingCandlesRenderer::new);
      event.registerEntityRenderer(ModEntities.FLOATING_BOOK, FloatingBookRenderer::new);
      event.registerEntityRenderer(ModEntities.FLOATING_SWORD, FloatingSwordRenderer::new);
      event.registerEntityRenderer(ModEntities.SPECTRAL_JACK_O_MIMIC, SpectralJackOMimicRenderer::new);

      event.registerBlockEntityRenderer(BlockEntityType.ENCHANTING_TABLE, BooklessEnchantTableRenderer::new);
    });

    // EntityRenderersEvent.AddLayers
    modEventBus.addListener((EntityRenderersEvent.AddLayers event) -> {

      for (EntityType<?> type : new EntityType<?>[] {EntityType.SPIDER, EntityType.CAVE_SPIDER, EntityType.PIG}) {
        if (event.getRenderer(type) instanceof LivingEntityRenderer<?, ?, ?> renderer) {
          addHeadItemLayer(renderer, -0.25F);
        }
      }

      if (event.getRenderer(EntityType.COW) instanceof LivingEntityRenderer<?, ?, ?> renderer) {
        addHeadItemLayer(renderer, -0.1875F);
      }

      if (event.getRenderer(EntityType.ENDERMAN) instanceof EndermanRenderer renderer) {
        renderer.addLayer(new CustomHeadLayer<>(renderer, event.getEntityModels(), event.getContext().getPlayerSkinRenderCache()));
      }
    });
  }

  private static <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void addHeadItemLayer(LivingEntityRenderer<?, S, M> renderer, float headCenterZ) {

    renderer.addLayer(new HeadItemLayer<>(renderer, headCenterZ));
  }
}
