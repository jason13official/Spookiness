package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.worldgen.feature.PumpkinPatchConfiguration;
import io.github.jason13official.spookiness.worldgen.feature.PumpkinPatchFeature;
import java.util.function.BiConsumer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.feature.Feature;

public class ModFeatures {

  public static final Feature<PumpkinPatchConfiguration> PUMPKIN_PATCH = new PumpkinPatchFeature(PumpkinPatchConfiguration.CODEC);

  public static void register(BiConsumer<Feature<?>, Identifier> consumer) {

    consumer.accept(PUMPKIN_PATCH, Spookiness.id("pumpkin_patch"));
  }
}
