package io.github.jason13official.spookiness.block;

import com.mojang.serialization.MapCodec;
import io.github.jason13official.spookiness.block.entity.SoullessJackOMimicBlockEntity;
import io.github.jason13official.spookiness.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public class SoullessJackOMimicBlock extends BaseEntityBlock {

  public static final MapCodec<SoullessJackOMimicBlock> CODEC = simpleCodec(SoullessJackOMimicBlock::new);
  public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
  public static final BooleanProperty LIT = BlockStateProperties.LIT;

  private static final float SEALED_RESISTANCE = 3600000.0F;

  public SoullessJackOMimicBlock(Properties properties) {
    super(properties);
    this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
  }

  @Override
  protected MapCodec<? extends BaseEntityBlock> codec() {
    return CODEC;
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
    builder.add(FACING, LIT);
  }

  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
  }

  @Override
  protected RenderShape getRenderShape(BlockState state) {
    return RenderShape.INVISIBLE;
  }

  @Override
  public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
    return new SoullessJackOMimicBlockEntity(pos, state);
  }

  @Override
  public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
    return createTickerHelper(type, ModBlockEntities.SOULLESS_JACK_O_MIMIC,
        level.isClientSide() ? SoullessJackOMimicBlockEntity::clientTick : SoullessJackOMimicBlockEntity::serverTick);
  }

  private static boolean isHolding(BlockGetter level, BlockPos pos) {
    return level.getBlockEntity(pos) instanceof SoullessJackOMimicBlockEntity mimic && mimic.isHolding();
  }

  @Override
  protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

    if (itemStack.isEmpty() || !(level.getBlockEntity(pos) instanceof SoullessJackOMimicBlockEntity mimic)) {
      return InteractionResult.TRY_WITH_EMPTY_HAND;
    }
    if (level instanceof ServerLevel serverLevel) {
      mimic.offer(serverLevel, player, itemStack);
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

    if (!(level.getBlockEntity(pos) instanceof SoullessJackOMimicBlockEntity mimic) || !mimic.isHolding()) {
      return InteractionResult.PASS;
    }
    if (level instanceof ServerLevel serverLevel) {
      mimic.release(serverLevel, player, false);
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
    return isHolding(level, pos) ? 0.0F : super.getDestroyProgress(state, player, level, pos);
  }

  @Override
  public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
    return isHolding(level, pos) ? SEALED_RESISTANCE : super.getExplosionResistance(state, level, pos, explosion);
  }

  @Override
  public boolean canEntityDestroy(BlockState state, BlockGetter level, BlockPos pos, Entity entity) {
    return !isHolding(level, pos) && super.canEntityDestroy(state, level, pos, entity);
  }
}
