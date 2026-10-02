package io.github.jason13official.spookiness.worldgen;

import io.github.jason13official.spookiness.Spookiness;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

public class ModPlacedFeatures {

  public static final ResourceKey<PlacedFeature> PUMPKIN_PATCH = createKey("pumpkin_patch");

  public static void bootstrap(BootstrapContext<PlacedFeature> context) {

    HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

    PlacementUtils.register(
        context,
        PUMPKIN_PATCH,
        configuredFeatures.getOrThrow(ModConfiguredFeatures.PUMPKIN_PATCH),
        // RarityFilter.onAverageOnceEvery(64),
        RarityFilter.onAverageOnceEvery(16),
        InSquarePlacement.spread(),
        PlacementUtils.HEIGHTMAP,
        BiomeFilter.biome()
    );
  }

  private static ResourceKey<PlacedFeature> createKey(String name) {

    return ResourceKey.create(Registries.PLACED_FEATURE, Spookiness.id(name));
  }
}
