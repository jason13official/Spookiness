package io.github.jason13official.spookiness;

import io.github.jason13official.spookiness.companion.PlayerFollowers;
import io.github.jason13official.spookiness.datagen.SpookinessDatagen;
import io.github.jason13official.spookiness.effect.LamentRitual;
import io.github.jason13official.spookiness.entity.FloatingBook;
import io.github.jason13official.spookiness.entity.FloatingCandles;
import io.github.jason13official.spookiness.entity.FloatingSword;
import io.github.jason13official.spookiness.entity.JackOMimic;
import io.github.jason13official.spookiness.entity.SpectralJackOMimic;
import io.github.jason13official.spookiness.item.PumpkinMaceItem;
import io.github.jason13official.spookiness.lighting.LivingLights;
import io.github.jason13official.spookiness.registry.ModAttachments;
import io.github.jason13official.spookiness.registry.ModDataComponents;
import io.github.jason13official.spookiness.registry.ModEntities;
import io.github.jason13official.spookiness.registry.ModFeatures;
import io.github.jason13official.spookiness.registry.ModItems;
import io.github.jason13official.spookiness.world.SpookySpawns;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEnchantItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
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

    bind(Registries.DATA_COMPONENT_TYPE, ModDataComponents::register);
    bind(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ModAttachments::register);
    bind(Registries.ENTITY_TYPE, ModEntities::register);
    bind(Registries.ITEM, ModItems::register);
    bind(Registries.FEATURE, ModFeatures::register);

    // GatherDataEvent.Client
    EVENT_BUS.addListener(SpookinessDatagen::init);

    // EntityAttributeCreationEvent
    EVENT_BUS.addListener((EntityAttributeCreationEvent event) -> {

      event.put(ModEntities.JACK_O_MIMIC, JackOMimic.createAttributes().build());
      event.put(ModEntities.FLOATING_CANDLES, FloatingCandles.createAttributes().build());
      event.put(ModEntities.FLOATING_BOOK, FloatingBook.createAttributes().build());
      event.put(ModEntities.FLOATING_SWORD, FloatingSword.createAttributes().build());
      event.put(ModEntities.SPECTRAL_JACK_O_MIMIC, SpectralJackOMimic.createAttributes().build());
    });

    // RegisterSpawnPlacementsEvent
    EVENT_BUS.addListener((RegisterSpawnPlacementsEvent event) -> {

      event.register(ModEntities.JACK_O_MIMIC, SpawnPlacementTypes.ON_GROUND,
          Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, JackOMimic::checkJackOMimicSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
    });

    // FinalizeSpawnEvent
    NeoForge.EVENT_BUS.addListener((FinalizeSpawnEvent event) -> {

      Mob mob = event.getEntity();
      SpookySpawns.equipPumpkinHead(mob, mob.getRandom());
    });

    // LivingChangeTargetEvent
    NeoForge.EVENT_BUS.addListener((LivingChangeTargetEvent event) -> {

      LivingEntity entity = event.getEntity();

      // skip the check if not wearing a jack_o_lantern helmet, or is not jack o mimic
      if (!entity.getItemBySlot(EquipmentSlot.HEAD).is(Items.JACK_O_LANTERN) || !entity.is(ModEntities.JACK_O_MIMIC)) {
        return;
      }

      // entity is wearing a jack_o_lantern helmet or is Jack O' Mimic

      // do not target players wielding our pumpkin_mace
      if (event.getNewAboutToBeSetTarget() instanceof Player player && player.getMainHandItem().is(ModItems.PUMPKIN_MACE)) {
        event.setCanceled(true);
      }
    });

    // LevelEvent.Unload
    NeoForge.EVENT_BUS.addListener((LevelEvent.Unload event) -> LivingLights.unload(event.getLevel()));

    // LivingDeathEvent
    NeoForge.EVENT_BUS.addListener((LivingDeathEvent event) -> {

      LivingEntity entity = event.getEntity();
      if (!(entity instanceof JackOMimic mimic)) {
        return; // not a death we care about
      }

      DamageSource source = event.getSource();
      if (!source.is(DamageTypes.MACE_SMASH) || !(mimic.level() instanceof ServerLevel level)) {
        return;
      }

      ItemStack weapon = source.getWeaponItem();
      if (weapon != null && weapon.is(ModItems.PUMPKIN_MACE)) {
        mimic.spawnSoulBurst(level);
      }
    });

    // PlayerEnchantItemEvent
    NeoForge.EVENT_BUS.addListener((PlayerEnchantItemEvent event) -> {

      if (event.getEntity() instanceof ServerPlayer player && player.containerMenu instanceof EnchantmentMenu menu) {
        menu.access.execute((level, pos) -> SpookySpawns.awakenEnchantingTableBook((ServerLevel) level, pos, player));
      }
    });

    // PlayerTickEvent.Post
    NeoForge.EVENT_BUS.addListener((PlayerTickEvent.Post event) -> {

      if (event.getEntity() instanceof ServerPlayer player) {
        SpookySpawns.tickCandleAwakening(player);
        SpookySpawns.tickBookshelfAwakening(player);
        LamentRitual.tick(player);
      }
    });

    // ServerTickEvent.Post
    NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> PlayerFollowers.tick(event.getServer()));

    // PlayerEvent.PlayerLoggedOutEvent
    NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {

      if (event.getEntity() instanceof ServerPlayer player) {
        PlayerFollowers.stash(player);
      }
    });

    // PlayerEvent.PlayerLoggedInEvent
    NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {

      if (event.getEntity() instanceof ServerPlayer player) {
        PlayerFollowers.restore(player);
      }
    });

    // LivingDeathEvent
    NeoForge.EVENT_BUS.addListener((LivingDeathEvent event) -> {

      LivingEntity entity = event.getEntity();
      DamageSource source = event.getSource();
      if (!(entity.level() instanceof ServerLevel level) || !(source.getEntity() instanceof Player player) || !PumpkinMaceItem.isPumpkinEntity(entity)) {
        return;
      }

      ItemStack weapon = source.getWeaponItem();
      if (weapon != null && weapon.is(ModItems.PUMPKIN_MACE)) {
        PumpkinMaceItem.addPumpkinKill(level, player, weapon);
      }
    });

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
