package io.github.jason13official.spookiness.entity;

import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.jspecify.annotations.Nullable;

public class FloatingHoe extends FloatingTool {

  private static final Item[] DEFAULT_HOES = {Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE};
  private static final int SEARCH_RANGE = 6;
  private static final int SEARCH_HEIGHT = 3;
  private static final int WATER_RANGE = 2;

  public FloatingHoe(EntityType<? extends FloatingHoe> type, Level level) {
    super(type, level);
  }

  @Override
  protected Goal createWorkGoal() {
    return new TillGoal();
  }

  @Override
  protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
    this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(DEFAULT_HOES[random.nextInt(DEFAULT_HOES.length)]));
  }

  private static boolean isTillable(LevelReader level, BlockPos pos) {
    BlockState state = level.getBlockState(pos);
    if (!state.is(Blocks.DIRT) && !state.is(Blocks.GRASS_BLOCK) || !level.isEmptyBlock(pos.above())) {
      return false;
    }
    for (BlockPos near : BlockPos.betweenClosed(pos.offset(-WATER_RANGE, 0, -WATER_RANGE), pos.offset(WATER_RANGE, 1, WATER_RANGE))) {
      if (level.getFluidState(near).is(FluidTags.WATER)) {
        return true;
      }
    }
    return false;
  }

  private class TillGoal extends WorkGoal {

    private @Nullable BlockPos soil;

    @Override
    protected boolean findWork(ServerLevel level) {
      FloatingHoe hoe = FloatingHoe.this;
      if (!EventHooks.canEntityGrief(level, hoe)) {
        return false;
      }
      this.soil = BlockPos.findClosestMatch(hoe.blockPosition(), SEARCH_RANGE, SEARCH_HEIGHT, pos -> isTillable(level, pos)).map(BlockPos::immutable).orElse(null);
      return this.soil != null;
    }

    @Override
    protected @Nullable Vec3 workSite() {
      return this.soil != null && isTillable(FloatingHoe.this.level(), this.soil) ? Vec3.atCenterOf(this.soil.above()) : null;
    }

    @Override
    protected double reachSqr() {
      return 2.25;
    }

    @Override
    protected void work(ServerLevel level) {
      if (this.soil == null) {
        return;
      }
      BlockState farmland = Blocks.FARMLAND.defaultBlockState();
      level.setBlock(this.soil, farmland, Block.UPDATE_ALL_IMMEDIATE);
      level.gameEvent(GameEvent.BLOCK_CHANGE, this.soil, GameEvent.Context.of(FloatingHoe.this, farmland));
      level.playSound(null, this.soil, SoundEvents.HOE_TILL, SoundSource.HOSTILE, 1.0F, 1.0F);
      this.soil = null;
    }
  }
}
