package io.github.jason13official.spookiness.worldgen;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.registry.ModEntities;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.random.Weighted;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModBiomeModifiers {

  public static final TagKey<Biome> HAS_PUMPKIN_PATCH = TagKey.create(Registries.BIOME, Spookiness.id("has_pumpkin_patch"));

  public static final ResourceKey<BiomeModifier> ADD_PUMPKIN_PATCH = createKey("add_pumpkin_patch");
  public static final ResourceKey<BiomeModifier> ADD_JACK_O_MIMIC_SPAWNS = createKey("add_jack_o_mimic_spawns");

  public static void bootstrap(BootstrapContext<BiomeModifier> context) {

    HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
    HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);
    HolderSet<Biome> pumpkinPatchBiomes = biomes.getOrThrow(HAS_PUMPKIN_PATCH);

    context.register(ADD_PUMPKIN_PATCH, new BiomeModifiers.AddFeaturesBiomeModifier(
        pumpkinPatchBiomes,
        HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.PUMPKIN_PATCH)),
        GenerationStep.Decoration.VEGETAL_DECORATION
    ));

    context.register(ADD_JACK_O_MIMIC_SPAWNS, BiomeModifiers.AddSpawnsBiomeModifier.singleSpawn(
        pumpkinPatchBiomes,
        new Weighted<>(new MobSpawnSettings.SpawnerData(ModEntities.JACK_O_MIMIC, 1, 1), 5)
    ));
  }

  private static ResourceKey<BiomeModifier> createKey(String name) {

    return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, Spookiness.id(name));
  }
}
