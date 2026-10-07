package io.github.jason13official.spookiness.effect;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class TemporaryBlocks {

  private static final List<Entry> ENTRIES = new ArrayList<>();

  public static boolean place(ServerLevel level, BlockPos pos, BlockState state, int ticks) {

    if (!level.getBlockState(pos).canBeReplaced() || !level.getFluidState(pos).isEmpty() || !level.setBlockAndUpdate(pos, state)) {
      return false;
    }
    ENTRIES.add(new Entry(level.dimension(), pos.immutable(), state, level.getGameTime() + ticks));
    return true;
  }

  public static void tick(MinecraftServer server) {

    if (ENTRIES.isEmpty()) {
      return;
    }
    ENTRIES.removeIf(entry -> {
      ServerLevel level = server.getLevel(entry.dimension());
      if (level == null) {
        return true;
      }
      if (level.getGameTime() < entry.expiry()) {
        return false;
      }
      revert(level, entry);
      return true;
    });
  }

  public static void revertAll(MinecraftServer server) {

    for (Entry entry : ENTRIES) {
      ServerLevel level = server.getLevel(entry.dimension());
      if (level != null) {
        revert(level, entry);
      }
    }
    ENTRIES.clear();
  }

  private static void revert(ServerLevel level, Entry entry) {
    if (level.isLoaded(entry.pos()) && level.getBlockState(entry.pos()) == entry.state()) {
      level.setBlockAndUpdate(entry.pos(), Blocks.AIR.defaultBlockState());
    }
  }

  private record Entry(ResourceKey<Level> dimension, BlockPos pos, BlockState state, long expiry) {
  }
}
