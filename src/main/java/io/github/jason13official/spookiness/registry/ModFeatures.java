package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.worldgen.feature.PumpkinPatchConfiguration;
import io.github.jason13official.spookiness.worldgen.feature.PumpkinPatchFeature;
import java.util.function.BiConsumer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.Feature;

public class ModFeatures {

  public static Feature<PumpkinPatchConfiguration> PUMPKIN_PATCH;

  public static void register(BiConsumer<Feature<?>, Identifier> consumer) {

    PUMPKIN_PATCH = new PumpkinPatchFeature(PumpkinPatchConfiguration.CODEC);
    consumer.accept(PUMPKIN_PATCH, Spookiness.id("pumpkin_patch"));
  }
}
