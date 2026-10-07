package io.github.jason13official.spookiness.effect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jason13official.spookiness.Spookiness;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class TemporaryBlocks extends SavedData {

  public static final Codec<TemporaryBlocks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      Entry.CODEC.listOf().optionalFieldOf("entries", List.of()).forGetter(blocks -> List.copyOf(blocks.entries))
  ).apply(instance, TemporaryBlocks::new));

  public static final SavedDataType<TemporaryBlocks> TYPE = new SavedDataType<>(Spookiness.id("temporary_blocks"), TemporaryBlocks::new, CODEC);

  private final List<Entry> entries;

  public TemporaryBlocks() {
    this(List.of());
  }

  private TemporaryBlocks(List<Entry> entries) {
    this.entries = new ArrayList<>(entries);
  }

  private static TemporaryBlocks get(ServerLevel level) {
    return level.getDataStorage().computeIfAbsent(TYPE);
  }

  public static boolean place(ServerLevel level, BlockPos pos, BlockState state, int ticks) {

    if (!level.getBlockState(pos).canBeReplaced() || !level.getFluidState(pos).isEmpty() || !level.setBlockAndUpdate(pos, state)) {
      return false;
    }
    TemporaryBlocks blocks = get(level);
    blocks.entries.add(new Entry(pos.immutable(), state, level.getGameTime() + ticks));
    blocks.setDirty();
    return true;
  }

  public static void tick(MinecraftServer server) {

    for (ServerLevel level : server.getAllLevels()) {
      TemporaryBlocks blocks = get(level);
      if (!blocks.entries.isEmpty() && blocks.entries.removeIf(entry -> revert(level, entry))) {
        blocks.setDirty();
      }
    }
  }

  private static boolean revert(ServerLevel level, Entry entry) {

    if (level.getGameTime() < entry.expiry() || !level.isLoaded(entry.pos())) {
      return false;
    }
    if (level.getBlockState(entry.pos()) == entry.state()) {
      level.setBlockAndUpdate(entry.pos(), Blocks.AIR.defaultBlockState());
    }
    return true;
  }

  private record Entry(BlockPos pos, BlockState state, long expiry) {

    private static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos),
        BlockState.CODEC.fieldOf("state").forGetter(Entry::state),
        Codec.LONG.fieldOf("expiry").forGetter(Entry::expiry)
    ).apply(instance, Entry::new));
  }
}
