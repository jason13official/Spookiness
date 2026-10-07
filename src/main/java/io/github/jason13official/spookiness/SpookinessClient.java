package io.github.jason13official.spookiness;

import io.github.jason13official.spookiness.client.fog.KindleFog;
import io.github.jason13official.spookiness.client.model.FloatingBookModel;
import io.github.jason13official.spookiness.client.model.JackOMimicModel;
import io.github.jason13official.spookiness.client.model.LamentConfigurationModel;
import io.github.jason13official.spookiness.client.renderer.FloatingBookRenderer;
import io.github.jason13official.spookiness.client.renderer.FloatingCandlesRenderer;
import io.github.jason13official.spookiness.client.renderer.FloatingLanternRenderer;
import io.github.jason13official.spookiness.client.renderer.FloatingSkullRenderer;
import io.github.jason13official.spookiness.client.renderer.FloatingToolRenderer;
import io.github.jason13official.spookiness.client.renderer.JackOMimicRenderer;
import io.github.jason13official.spookiness.client.renderer.LamentConfigurationOverlay;
import io.github.jason13official.spookiness.client.renderer.LamentConfigurationSpecialRenderer;
import io.github.jason13official.spookiness.client.renderer.SoullessJackOMimicRenderer;
import io.github.jason13official.spookiness.client.renderer.SoullessJackOMimicSpecialRenderer;
import io.github.jason13official.spookiness.registry.ModBlockEntities;
import io.github.jason13official.spookiness.client.renderer.SpectralJackOMimicRenderer;
import io.github.jason13official.spookiness.client.renderer.BooklessEnchantTableRenderer;
import io.github.jason13official.spookiness.client.renderer.layer.HeadItemLayer;
import io.github.jason13official.spookiness.client.renderer.boss.GourdwyrmRenderer;
import io.github.jason13official.spookiness.client.renderer.boss.HallowedMotherRenderer;
import io.github.jason13official.spookiness.client.renderer.boss.VigilCandleRenderer;
import io.github.jason13official.spookiness.client.renderer.boss.WickmanHeadRenderer;
import io.github.jason13official.spookiness.client.renderer.boss.WickmanRenderer;
import io.github.jason13official.spookiness.registry.ModEntities;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EndermanRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

public class SpookinessClient {

  public SpookinessClient(final IEventBus modEventBus) {

    // EntityRenderersEvent.RegisterLayerDefinitions
    modEventBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> {

      event.registerLayerDefinition(JackOMimicModel.LAYER_LOCATION, JackOMimicModel::createBodyLayer);
      event.registerLayerDefinition(FloatingBookModel.LAYER_LOCATION, FloatingBookModel::createBodyLayer);
      event.registerLayerDefinition(LamentConfigurationModel.LAYER_LOCATION, LamentConfigurationModel::createBodyLayer);
    });

    // EntityRenderersEvent.RegisterRenderers
    modEventBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {

      event.registerEntityRenderer(ModEntities.JACK_O_MIMIC, JackOMimicRenderer::new);
      event.registerEntityRenderer(ModEntities.FLOATING_CANDLES, FloatingCandlesRenderer::new);
      event.registerEntityRenderer(ModEntities.FLOATING_BOOK, FloatingBookRenderer::new);
      event.registerEntityRenderer(ModEntities.FLOATING_SWORD, FloatingToolRenderer::new);
      event.registerEntityRenderer(ModEntities.FLOATING_SHEARS, FloatingToolRenderer::new);
      event.registerEntityRenderer(ModEntities.FLOATING_HOE, FloatingToolRenderer::new);
      event.registerEntityRenderer(ModEntities.FLOATING_LANTERN, FloatingLanternRenderer::new);
      event.registerEntityRenderer(ModEntities.FLOATING_SKULL, FloatingSkullRenderer::new);
      event.registerEntityRenderer(ModEntities.HAUNTED_ARMOR_STAND, ArmorStandRenderer::new);
      event.registerEntityRenderer(ModEntities.SPECTRAL_JACK_O_MIMIC, SpectralJackOMimicRenderer::new);
      event.registerEntityRenderer(ModEntities.WICKMAN, WickmanRenderer::new);
      event.registerEntityRenderer(ModEntities.WICKMAN_HEAD, WickmanHeadRenderer::new);
      event.registerEntityRenderer(ModEntities.VIGIL_CANDLE, VigilCandleRenderer::new);
      event.registerEntityRenderer(ModEntities.HALLOWED_MOTHER, HallowedMotherRenderer::new);
      event.registerEntityRenderer(ModEntities.GOURDWYRM, GourdwyrmRenderer::new);
      event.registerEntityRenderer(ModEntities.PUMPKIN_BOMB, context -> new ThrownItemRenderer<>(context, 1.5F, true));

      event.registerBlockEntityRenderer(BlockEntityType.ENCHANTING_TABLE, BooklessEnchantTableRenderer::new);
      event.registerBlockEntityRenderer(ModBlockEntities.SOULLESS_JACK_O_MIMIC, SoullessJackOMimicRenderer::new);
    });

    // RegisterSpecialModelRendererEvent
    modEventBus.addListener((RegisterSpecialModelRendererEvent event) -> {

      event.register(Spookiness.id("lament"), LamentConfigurationSpecialRenderer.Unbaked.MAP_CODEC);
      event.register(Spookiness.id("soulless_jack_o_mimic"), SoullessJackOMimicSpecialRenderer.Unbaked.MAP_CODEC);
    });

    // ClientTickEvent.Post
    NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> LamentConfigurationOverlay.tick());

    // RenderHandEvent
    NeoForge.EVENT_BUS.addListener(LamentConfigurationOverlay::render);

    // ClientTickEvent.Post
    NeoForge.EVENT_BUS.addListener(KindleFog::tick);

    // ViewportEvent.RenderFog
    NeoForge.EVENT_BUS.addListener(KindleFog::renderFog);

    // ViewportEvent.ComputeFogColor
    NeoForge.EVENT_BUS.addListener(KindleFog::fogColor);

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

//  private static <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void addHeadItemLayer(LivingEntityRenderer<?, S, M> renderer, float headCenterZ) {
//
//    renderer.addLayer(new HeadItemLayer<>(renderer, headCenterZ));
//  }

  private static void addHeadItemLayer(LivingEntityRenderer<?, ?, ?> renderer, float headCenterZ) {

    renderer.addLayer(new HeadItemLayer(renderer, headCenterZ));
  }
}
