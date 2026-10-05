package io.github.jason13official.spookiness;

import io.github.jason13official.spookiness.client.model.FloatingBookModel;
import io.github.jason13official.spookiness.client.model.JackOMimicModel;
import io.github.jason13official.spookiness.client.renderer.FloatingBookRenderer;
import io.github.jason13official.spookiness.client.renderer.FloatingCandlesRenderer;
import io.github.jason13official.spookiness.client.renderer.FloatingSwordRenderer;
import io.github.jason13official.spookiness.client.renderer.JackOMimicRenderer;
import io.github.jason13official.spookiness.client.renderer.SpectralJackOMimicRenderer;
import io.github.jason13official.spookiness.registry.ModEntities;
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
    });
  }
}
