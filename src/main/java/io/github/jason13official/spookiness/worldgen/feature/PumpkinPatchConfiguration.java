package io.github.jason13official.spookiness.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record PumpkinPatchConfiguration(
    BlockStateProvider pumpkins,
    BlockStateProvider grass,
    int pumpkinTries,
    int grassTries,
    int xzSpread,
    int ySpread,
    float stemChance,
    float vineChance,
    float mimicChance
) implements FeatureConfiguration {

  public static final Codec<PumpkinPatchConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      BlockStateProvider.CODEC.fieldOf("pumpkins").forGetter(PumpkinPatchConfiguration::pumpkins),
      BlockStateProvider.CODEC.fieldOf("grass").forGetter(PumpkinPatchConfiguration::grass),
      ExtraCodecs.POSITIVE_INT.fieldOf("pumpkin_tries").forGetter(PumpkinPatchConfiguration::pumpkinTries),
      ExtraCodecs.NON_NEGATIVE_INT.fieldOf("grass_tries").forGetter(PumpkinPatchConfiguration::grassTries),
      ExtraCodecs.NON_NEGATIVE_INT.fieldOf("xz_spread").forGetter(PumpkinPatchConfiguration::xzSpread),
      ExtraCodecs.NON_NEGATIVE_INT.fieldOf("y_spread").forGetter(PumpkinPatchConfiguration::ySpread),
      Codec.floatRange(0.0F, 1.0F).fieldOf("stem_chance").forGetter(PumpkinPatchConfiguration::stemChance),
      Codec.floatRange(0.0F, 1.0F).fieldOf("vine_chance").forGetter(PumpkinPatchConfiguration::vineChance),
      Codec.floatRange(0.0F, 1.0F).fieldOf("mimic_chance").forGetter(PumpkinPatchConfiguration::mimicChance)
  ).apply(instance, PumpkinPatchConfiguration::new));
}
