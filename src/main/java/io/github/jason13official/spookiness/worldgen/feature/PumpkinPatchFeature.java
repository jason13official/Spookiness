package io.github.jason13official.spookiness.worldgen.feature;

import io.github.jason13official.spookiness.util.SpookyMath;
import com.mojang.serialization.Codec;
import io.github.jason13official.spookiness.entity.JackOMimic;
import io.github.jason13official.spookiness.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AttachedStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.neoforged.neoforge.event.EventHooks;

public class PumpkinPatchFeature extends Feature<PumpkinPatchConfiguration> {

  public PumpkinPatchFeature(Codec<PumpkinPatchConfiguration> codec) {
    super(codec);
  }

  @Override
  public boolean place(FeaturePlaceContext<PumpkinPatchConfiguration> context) {

    WorldGenLevel level = context.level();
    RandomSource random = context.random();
    BlockPos origin = context.origin();
    PumpkinPatchConfiguration config = context.config();
    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    int placed = 0;
    for (int i = 0; i < config.pumpkinTries(); i++) {
      randomOffset(pos, origin, random, config);
      if (!isOpenGrass(level, pos)) {
        continue;
      }

      BlockState state = config.pumpkins().getState(level, random, pos);
      level.setBlock(pos, state, Block.UPDATE_CLIENTS);
      placed++;

      if (state.is(Blocks.PUMPKIN) && random.nextFloat() < config.stemChance()) {
        this.placeStem(level, random, pos);
      }

      if (random.nextFloat() < config.vineChance()) {
        this.placeVines(level, random, pos);
      }
    }

    if (placed == 0) {
      return false;
    }

    if (random.nextFloat() < config.mimicChance()) {
      this.placeMimic(level, random, origin, config);
    }

    for (int i = 0; i < config.grassTries(); i++) {
      randomOffset(pos, origin, random, config);
      if (isOpenGrass(level, pos)) {
        this.placeGrass(level, config.grass().getState(level, random, pos), pos);
      }
    }

    return true;
  }

  private void placeGrass(WorldGenLevel level, BlockState state, BlockPos pos) {

    if (!state.canSurvive(level, pos)) {
      return;
    }

    if (state.getBlock() instanceof DoublePlantBlock) {
      if (level.isEmptyBlock(pos.above())) {
        DoublePlantBlock.placeAt(level, state, pos, Block.UPDATE_CLIENTS);
      }
    } else {
      level.setBlock(pos, state, Block.UPDATE_CLIENTS);
    }
  }

  private void placeMimic(WorldGenLevel level, RandomSource random, BlockPos origin, PumpkinPatchConfiguration config) {

    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    for (int i = 0; i < 8; i++) {
      randomOffset(pos, origin, random, config);
      if (!isOpenGrass(level, pos)) {
        continue;
      }

      JackOMimic mimic = ModEntities.JACK_O_MIMIC.create(level.getLevel(), EntitySpawnReason.CHUNK_GENERATION);
      if (mimic == null) {
        return;
      }

      mimic.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, SpookyMath.randomYaw(random), 0.0F);
      EventHooks.finalizeMobSpawn(mimic, level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.CHUNK_GENERATION, null);
      mimic.setPersistenceRequired();
      level.addFreshEntityWithPassengers(mimic);
      return;
    }
  }

  private static void randomOffset(BlockPos.MutableBlockPos pos, BlockPos origin, RandomSource random, PumpkinPatchConfiguration config) {

    pos.setWithOffset(origin,
        random.nextInt(config.xzSpread() + 1) - random.nextInt(config.xzSpread() + 1),
        random.nextInt(config.ySpread() + 1) - random.nextInt(config.ySpread() + 1),
        random.nextInt(config.xzSpread() + 1) - random.nextInt(config.xzSpread() + 1));
  }

  private void placeStem(WorldGenLevel level, RandomSource random, BlockPos pumpkinPos) {

    for (Direction direction : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
      BlockPos stemPos = pumpkinPos.relative(direction);
      if (isOpenGrass(level, stemPos)) {
        level.setBlock(stemPos.below(), Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(stemPos, Blocks.ATTACHED_PUMPKIN_STEM.defaultBlockState().setValue(AttachedStemBlock.FACING, direction.getOpposite()), Block.UPDATE_CLIENTS);
        return;
      }
    }
  }

  private void placeVines(WorldGenLevel level, RandomSource random, BlockPos pumpkinPos) {

    for (Direction direction : Direction.Plane.HORIZONTAL) {
      BlockPos vinePos = pumpkinPos.relative(direction);
      Direction towardsPumpkin = direction.getOpposite();
      if (random.nextBoolean() && level.isEmptyBlock(vinePos) && VineBlock.isAcceptableNeighbour(level, pumpkinPos, towardsPumpkin)) {
        level.setBlock(vinePos, Blocks.VINE.defaultBlockState().setValue(VineBlock.getPropertyForFace(towardsPumpkin), true), Block.UPDATE_CLIENTS);
      }
    }
  }

  private static boolean isOpenGrass(WorldGenLevel level, BlockPos pos) {

    return level.isEmptyBlock(pos) && level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK);
  }
}
