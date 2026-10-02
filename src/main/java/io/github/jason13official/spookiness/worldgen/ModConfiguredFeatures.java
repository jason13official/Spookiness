package io.github.jason13official.spookiness.worldgen;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.registry.ModFeatures;
import io.github.jason13official.spookiness.worldgen.feature.PumpkinPatchConfiguration;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;

public class ModConfiguredFeatures {

  public static final ResourceKey<ConfiguredFeature<?, ?>> PUMPKIN_PATCH = createKey("pumpkin_patch");

  public static void bootstrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {

    WeightedList.Builder<BlockState> pumpkins = WeightedList.<BlockState>builder().add(Blocks.PUMPKIN.defaultBlockState(), 12);
    addHorizontalFacings(pumpkins, Blocks.CARVED_PUMPKIN, 1);
    addHorizontalFacings(pumpkins, Blocks.JACK_O_LANTERN, 1);

    WeightedList.Builder<BlockState> grass = WeightedList.<BlockState>builder()
        .add(Blocks.SHORT_GRASS.defaultBlockState(), 6)
        .add(Blocks.TALL_GRASS.defaultBlockState(), 2)
        .add(Blocks.FERN.defaultBlockState(), 1);

    FeatureUtils.register(context, PUMPKIN_PATCH, ModFeatures.PUMPKIN_PATCH, new PumpkinPatchConfiguration(
        new WeightedStateProvider(pumpkins),
        new WeightedStateProvider(grass),
        40,
        96,
        15,
        5,
        0.4F,
        1.0F,
        1.0F
    ));
  }

  private static void addHorizontalFacings(WeightedList.Builder<BlockState> builder, Block block, int weight) {

    for (Direction direction : Direction.Plane.HORIZONTAL) {
      builder.add(block.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, direction), weight);
    }
  }

  private static ResourceKey<ConfiguredFeature<?, ?>> createKey(String name) {

    return ResourceKey.create(Registries.CONFIGURED_FEATURE, Spookiness.id(name));
  }
}
