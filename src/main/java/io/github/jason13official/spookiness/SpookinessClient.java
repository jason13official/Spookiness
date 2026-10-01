package io.github.jason13official.spookiness;

import io.github.jason13official.spookiness.client.model.JackOMimicModel;
import io.github.jason13official.spookiness.client.renderer.JackOMimicRenderer;
import io.github.jason13official.spookiness.registry.ModEntities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class SpookinessClient {

  public SpookinessClient(final IEventBus modEventBus) {

    // EntityRenderersEvent.RegisterLayerDefinitions
    modEventBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> {

      event.registerLayerDefinition(JackOMimicModel.LAYER_LOCATION, JackOMimicModel::createBodyLayer);
    });

    // EntityRenderersEvent.RegisterRenderers
    modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {

      event.registerEntityRenderer(ModEntities.JACK_O_MIMIC, JackOMimicRenderer::new);
    });
  }
}
