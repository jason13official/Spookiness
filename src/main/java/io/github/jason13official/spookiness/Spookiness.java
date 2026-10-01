package io.github.jason13official.spookiness;

import io.github.jason13official.spookiness.entity.JackOMimic;
import io.github.jason13official.spookiness.registry.ModEntities;
import io.github.jason13official.spookiness.registry.ModItems;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
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

    bind(Registries.ENTITY_TYPE, ModEntities::register);
    bind(Registries.ITEM, ModItems::register);

    // EntityAttributeCreationEvent
    EVENT_BUS.addListener((EntityAttributeCreationEvent event) -> {

      event.put(ModEntities.JACK_O_MIMIC, JackOMimic.createAttributes().build());
    });

    // FinalizeSpawnEvent
    NeoForge.EVENT_BUS.addListener((FinalizeSpawnEvent event) -> {

      // if (event.getEntity() instanceof AbstractSkeleton skeleton && skeleton.getRandom().nextBoolean()) {
      if (event.getEntity() instanceof AbstractSkeleton skeleton) {
        skeleton.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.JACK_O_LANTERN));
      }
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
