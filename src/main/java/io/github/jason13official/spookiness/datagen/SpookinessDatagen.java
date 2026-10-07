package io.github.jason13official.spookiness.datagen;

import io.github.jason13official.spookiness.registry.ModTimelines;
import io.github.jason13official.spookiness.registry.ModDamageTypes;
import io.github.jason13official.spookiness.registry.ModStructures;
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
        .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ModBiomeModifiers::bootstrap)
        .add(Registries.DAMAGE_TYPE, ModDamageTypes::bootstrap)
        .add(Registries.TIMELINE, ModTimelines::bootstrap)
        .add(Registries.STRUCTURE, ModStructures::bootstrapStructures)
        .add(Registries.STRUCTURE_SET, ModStructures::bootstrapSets));

    event.createProvider(SpookinessBiomeTagsProvider::new);
    event.createProvider(SpookinessDamageTypeTagsProvider::new);
    event.createProvider(SpookinessTimelineTagsProvider::new);
    event.createProvider(SpookinessRecipeProvider.Runner::new);
    event.createProvider(SpookinessModelProvider::new);
    event.createProvider(SpookinessLanguageProvider::new);
    event.createProvider(SpookinessLootTableProvider::create);
    event.createProvider(SpookinessAdvancementProvider::create);
    event.createProvider(SpookinessSoundDefinitionsProvider::new);
  }
}
