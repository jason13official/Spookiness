package io.github.jason13official.spookiness.datagen;

import io.github.jason13official.spookiness.worldgen.ModBiomeModifiers;
import io.github.jason13official.spookiness.worldgen.ModConfiguredFeatures;
import io.github.jason13official.spookiness.worldgen.ModPlacedFeatures;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class SpookinessDatagen {

  public static void init(GatherDataEvent.Client event) {

    event.createDatapackRegistryObjects(new RegistrySetBuilder()
        .add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap)
        .add(Registries.PLACED_FEATURE, ModPlacedFeatures::bootstrap)
        .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap));

    event.createProvider(SpookinessBiomeTagsProvider::new);
    event.createProvider(SpookinessRecipeProvider.Runner::new);
    event.createProvider(SpookinessModelProvider::new);
    event.createProvider(SpookinessLanguageProvider::new);
  }
}
