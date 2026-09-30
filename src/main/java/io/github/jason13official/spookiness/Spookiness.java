package io.github.jason13official.spookiness;

import com.mojang.logging.LogUtils;
import io.github.jason13official.spookiness.registry.ModItems;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.slf4j.Logger;

@Mod(Spookiness.MOD_ID)
public class Spookiness {

  public static final String MOD_ID = "spookiness";
  public static final Logger LOGGER = LogUtils.getLogger();

  public static IEventBus EVENT_BUS;

  public Spookiness(IEventBus modEventBus, ModContainer modContainer) {
    EVENT_BUS = modEventBus;
    bind(Registries.ITEM, ModItems::register);
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
