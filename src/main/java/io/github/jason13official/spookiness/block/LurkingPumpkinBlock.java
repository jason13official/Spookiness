package io.github.jason13official.spookiness.block;

import com.mojang.serialization.MapCodec;
import io.github.jason13official.spookiness.block.entity.LurkingPumpkinBlockEntity;
import io.github.jason13official.spookiness.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class LurkingPumpkinBlock extends BaseEntityBlock {

  public static final MapCodec<LurkingPumpkinBlock> CODEC = simpleCodec(LurkingPumpkinBlock::new);
  public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

  public LurkingPumpkinBlock(Properties properties) {
    super(properties);
    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
  }

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new LurkingPumpkinBlockEntity(pos, state);
  }

  @Override
  public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
    return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.LURKING_PUMPKIN, LurkingPumpkinBlockEntity::serverTick);
  }

  @Override
  protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

    awaken(level, pos, player);
    return InteractionResult.SUCCESS;
  }

  @Override
  protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
    awaken(level, pos, player);
  }

  private static void awaken(Level level, BlockPos pos, Player player) {

    if (level instanceof ServerLevel serverLevel && level.getBlockEntity(pos) instanceof LurkingPumpkinBlockEntity pumpkin) {
      pumpkin.awaken(serverLevel, player);
    }
  }
}
