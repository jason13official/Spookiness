package io.github.jason13official.spookiness.block;

import com.mojang.serialization.MapCodec;
import io.github.jason13official.spookiness.block.entity.JackOMimicStemBlockEntity;
import io.github.jason13official.spookiness.block.entity.LurkingPumpkinBlockEntity;
import io.github.jason13official.spookiness.companion.Allies;
import io.github.jason13official.spookiness.registry.ModBlocks;
import io.github.jason13official.spookiness.registry.ModEntities;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.CommonHooks;
import org.jspecify.annotations.Nullable;

public class JackOMimicStemBlock extends VegetationBlock implements BonemealableBlock, EntityBlock {

  public static final MapCodec<JackOMimicStemBlock> CODEC = simpleCodec(JackOMimicStemBlock::new);
  public static final IntegerProperty AGE = StemBlock.AGE;
  public static final int MAX_AGE = StemBlock.MAX_AGE;
  public static final int MAX_SPROUTED = 10;

  private static final VoxelShape[] SHAPES = Block.boxes(MAX_AGE, age -> Block.column(2.0, 0.0, 2 + age * 2));

  public JackOMimicStemBlock(Properties properties) {
    super(properties);
    this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
  }

  @Override
  protected MapCodec<? extends VegetationBlock> codec() {
    return CODEC;
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(AGE);
  }

  @Override
  protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    return SHAPES[state.getValue(AGE)];
  }

  @Override
  protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
    return state.is(BlockTags.SUPPORTS_PUMPKIN_STEM);
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new JackOMimicStemBlockEntity(pos, state);
  }

  @Override
  public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity by, ItemStack itemStack) {

    if (by != null && level.getBlockEntity(pos) instanceof JackOMimicStemBlockEntity stem) {
      stem.setOwner(by.getUUID());
    }
  }

  @Override
  protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {

    if (!level.isAreaLoaded(pos, 1) || level.getRawBrightness(pos, 0) < 9) {
      return;
    }
    float speed = CropBlock.getGrowthSpeed(state, level, pos);
    if (!CommonHooks.canCropGrow(level, pos, state, random.nextInt((int) (25.0F / speed) + 1) == 0)) {
      return;
    }

    int age = state.getValue(AGE);
    if (age < MAX_AGE) {
      level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
    } else {
      this.sprout(level, pos, random);
    }
    CommonHooks.fireCropGrowPost(level, pos, state);
  }

  private void sprout(ServerLevel level, BlockPos pos, RandomSource random) {

    if (!(level.getBlockEntity(pos) instanceof JackOMimicStemBlockEntity stem) || stem.getOwner() == null || !hasRoom(level, stem.getOwner())) {
      return;
    }

    Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
    BlockPos fruitPos = pos.relative(direction);
    if (!level.getBlockState(fruitPos).isAir() || !level.getBlockState(fruitPos.below()).is(BlockTags.SUPPORTS_PUMPKIN_STEM_FRUIT)) {
      return;
    }

    level.setBlockAndUpdate(fruitPos, ModBlocks.LURKING_CARVED_PUMPKIN.defaultBlockState().setValue(LurkingPumpkinBlock.FACING, direction));
    if (level.getBlockEntity(fruitPos) instanceof LurkingPumpkinBlockEntity pumpkin) {
      pumpkin.setOwner(stem.getOwner());
    }
  }

  public static boolean hasRoom(ServerLevel level, UUID owner) {

    return level.getServer().getPlayerList().getPlayer(owner) != null
        && level.getEntities(ModEntities.JACK_O_MIMIC, mimic -> mimic.isAlive() && owner.equals(Allies.ownerOf(mimic))).size() < MAX_SPROUTED;
  }

  @Override
  public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
    return state.getValue(AGE) != MAX_AGE;
  }

  @Override
  public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
    return true;
  }

  @Override
  public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {

    int age = Math.min(MAX_AGE, state.getValue(AGE) + Mth.nextInt(random, 2, 5));
    BlockState grown = state.setValue(AGE, age);
    level.setBlock(pos, grown, Block.UPDATE_CLIENTS);
    if (age == MAX_AGE) {
      grown.randomTick(level, pos, random);
    }
  }
}
