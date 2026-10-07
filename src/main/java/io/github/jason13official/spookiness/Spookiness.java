package io.github.jason13official.spookiness;

import io.github.jason13official.spookiness.event.EntityEvents;
import io.github.jason13official.spookiness.event.LivingEvents;
import io.github.jason13official.spookiness.event.PlayerEvents;
import io.github.jason13official.spookiness.event.SetupEvents;
import io.github.jason13official.spookiness.event.WorldEvents;
import io.github.jason13official.spookiness.registry.ModAttachments;
import io.github.jason13official.spookiness.registry.ModBlockEntities;
import io.github.jason13official.spookiness.registry.ModBlocks;
import io.github.jason13official.spookiness.registry.ModDataComponents;
import io.github.jason13official.spookiness.registry.ModEntities;
import io.github.jason13official.spookiness.registry.ModFeatures;
import io.github.jason13official.spookiness.registry.ModItems;
import io.github.jason13official.spookiness.registry.ModSounds;
import io.github.jason13official.spookiness.registry.ModTabs;
import io.github.jason13official.spookiness.registry.ModTriggers;
import io.github.jason13official.spookiness.world.netherrealm.ModStructures;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(Spookiness.MOD_ID)
public class Spookiness {

  public static final String MOD_ID = "spookiness";
  public static final Logger LOG = LoggerFactory.getLogger(MOD_ID);

  public static IEventBus EVENT_BUS;

  /// @see net.minecraft.core.registries.BuiltInRegistries
  public Spookiness(IEventBus modEventBus) {

    EVENT_BUS = modEventBus;

    // neo registration
    bind(Registries.DATA_COMPONENT_TYPE, ModDataComponents::register);
    bind(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModAttachments::register);

    // vanilla registration
    // sounds, fluids, mob effects, blocks
    bind(Registries.BLOCK, ModBlocks::register);
    bind(Registries.BLOCK_ENTITY_TYPE, ModBlockEntities::register);
    bind(Registries.ENTITY_TYPE, ModEntities::register);
    bind(Registries.ITEM, ModItems::register);
    // potion, particle, block entity type, menu,
    // recipe type, recipe serializer, attribute
    // villager type, villager profession,
    bind(Registries.FEATURE, ModFeatures::register);
    bind(Registries.STRUCTURE_TYPE, ModStructures::registerTypes);
    bind(Registries.STRUCTURE_PIECE, ModStructures::registerPieces);
    bind(Registries.CREATIVE_MODE_TAB, ModTabs::register);
    bind(Registries.TRIGGER_TYPE, ModTriggers::register);
    bind(Registries.SOUND_EVENT, ModSounds::register);
    // game rule

    SetupEvents.register(EVENT_BUS, NeoForge.EVENT_BUS);
    EntityEvents.register(NeoForge.EVENT_BUS);
    LivingEvents.register(NeoForge.EVENT_BUS);
    PlayerEvents.register(NeoForge.EVENT_BUS);
    WorldEvents.register(NeoForge.EVENT_BUS);

    if (FMLLoader.getCurrent().getDist() == Dist.CLIENT) {
      new SpookinessClient(EVENT_BUS);
    }
  }

  public static Identifier id(String path) {

    return Identifier.fromNamespaceAndPath(MOD_ID, path);
  }

  public <T> void bind(ResourceKey<Registry<T>> registryKey, Consumer<BiConsumer<T, Identifier>> source) {

    EVENT_BUS.addListener((Consumer<RegisterEvent>) event -> {
      if (registryKey.equals(event.getRegistryKey())) {
        source.accept((t, rl) -> event.register(registryKey, rl, () -> t));
      }
    });
  }
}
