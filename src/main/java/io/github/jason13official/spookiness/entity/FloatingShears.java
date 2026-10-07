package io.github.jason13official.spookiness.entity;

import net.minecraft.world.entity.ai.goal.Goal;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.jspecify.annotations.Nullable;

public class FloatingShears extends FloatingTool {

  private static final double SHEEP_RANGE = 12.0;
  private static final int LEAVES_RANGE = 6;

  public FloatingShears(EntityType<? extends FloatingShears> type, Level level) {
    super(type, level);
  }

  @Override
  protected Goal createWorkGoal() {
    return new ShearGoal();
  }

  @Override
  protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
    this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.SHEARS));
  }

  private static boolean isWildLeaves(BlockState state) {
    return state.is(BlockTags.LEAVES) && state.hasProperty(LeavesBlock.PERSISTENT) && !state.getValue(LeavesBlock.PERSISTENT);
  }

  private class ShearGoal extends WorkGoal {

    private @Nullable Sheep sheep;
    private @Nullable BlockPos leaves;

    @Override
    protected boolean findWork(ServerLevel level) {
      FloatingShears shears = FloatingShears.this;
      this.sheep = level.getEntitiesOfClass(Sheep.class, shears.getBoundingBox().inflate(SHEEP_RANGE), Sheep::readyForShearing).stream()
          .min(Comparator.comparingDouble(shears::distanceToSqr)).orElse(null);
      this.leaves = null;
      if (this.sheep != null) {
        return true;
      }
      if (!EventHooks.canEntityGrief(level, shears)) {
        return false;
      }
      Optional<BlockPos> found = BlockPos.findClosestMatch(shears.blockPosition(), LEAVES_RANGE, LEAVES_RANGE, pos -> isWildLeaves(level.getBlockState(pos)));
      this.leaves = found.map(BlockPos::immutable).orElse(null);
      return this.leaves != null;
    }

    @Override
    protected @Nullable Vec3 workSite() {
      if (this.sheep != null) {
        return this.sheep.readyForShearing() ? this.sheep.getBoundingBox().getCenter() : null;
      }
      if (this.leaves != null && isWildLeaves(FloatingShears.this.level().getBlockState(this.leaves))) {
        return Vec3.atCenterOf(this.leaves);
      }
      return null;
    }

    @Override
    protected double reachSqr() {
      return this.sheep != null ? 4.0 : 6.25;
    }

    @Override
    protected void work(ServerLevel level) {
      FloatingShears shears = FloatingShears.this;
      ItemStack tool = shears.getMainHandItem();
      if (this.sheep != null) {
        this.sheep.shear(level, SoundSource.HOSTILE, tool);
        this.sheep.gameEvent(GameEvent.SHEAR, shears);
      } else if (this.leaves != null) {
        BlockState state = level.getBlockState(this.leaves);
        Block.dropResources(state, level, this.leaves, null, shears, tool);
        level.destroyBlock(this.leaves, false, shears);
        level.playSound(null, this.leaves, SoundEvents.SHEARS_SNIP, SoundSource.HOSTILE, 1.0F, 1.0F);
      }
      this.sheep = null;
      this.leaves = null;
    }
  }
}
