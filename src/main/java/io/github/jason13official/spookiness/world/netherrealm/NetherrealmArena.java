package io.github.jason13official.spookiness.world.netherrealm;

import com.mojang.datafixers.util.Pair;
import io.github.jason13official.spookiness.effect.SoulBurst;
import io.github.jason13official.spookiness.entity.boss.Gourdwyrm;
import io.github.jason13official.spookiness.entity.boss.VigilCandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class NetherrealmArena {

  public static final int RADIUS = 28;
  public static final int PLATFORM_OFFSET = 2;
  public static final int CLEARANCE = 28;
  public static final int PILLARS = 8;
  public static final int PILLAR_RING = 22;
  public static final int PILLAR_HEIGHT = 14;

  private static final int SEARCH_RADIUS_CHUNKS = 100;
  private static final double ALTAR_REACH = 8.0;
  private static final double WYRM_RISE = 16.0;

  public static BoundingBox bounds(BlockPos center) {

    return new BoundingBox(center.getX() - RADIUS - 1, center.getY() - RADIUS / 4 - 3, center.getZ() - RADIUS - 1, center.getX() + RADIUS + 1,
        center.getY() + CLEARANCE, center.getZ() + RADIUS + 1);
  }

  public static List<BlockPos> pillarBases(BlockPos center) {

    List<BlockPos> bases = new ArrayList<>(PILLARS);
    for (int i = 0; i < PILLARS; i++) {
      double angle = Math.PI * 2.0 * i / PILLARS;
      bases.add(center.offset((int) Math.round(Math.cos(angle) * PILLAR_RING), 0, (int) Math.round(Math.sin(angle) * PILLAR_RING)));
    }
    return bases;
  }

  private static Optional<Holder.Reference<Structure>> structure(ServerLevel level) {

    return level.registryAccess().lookupOrThrow(Registries.STRUCTURE).get(ModStructures.NETHERREALM);
  }

  public static Optional<Vec3> findArrival(ServerLevel nether, BlockPos origin) {

    Optional<Holder.Reference<Structure>> structure = structure(nether);
    if (structure.isEmpty()) {
      return Optional.empty();
    }
    Pair<BlockPos, Holder<Structure>> found = nether.getChunkSource().getGenerator()
        .findNearestMapStructure(nether, HolderSet.direct(structure.get()), origin, SEARCH_RADIUS_CHUNKS, false);
    if (found == null) {
      return Optional.empty();
    }
    BlockPos start = found.getFirst();
    int floorY = nether.getChunkSource().getGenerator().getSeaLevel() + PLATFORM_OFFSET;
    return Optional.of(new Vec3(start.getX() + 0.5, floorY + 1, start.getZ() + 0.5 + RADIUS - 4));
  }

  public static Optional<BlockPos> centerAt(ServerLevel level, BlockPos pos) {

    if (level.dimension() != Level.NETHER) {
      return Optional.empty();
    }
    Optional<Holder.Reference<Structure>> structure = structure(level);
    if (structure.isEmpty()) {
      return Optional.empty();
    }
    StructureStart start = level.structureManager().getStructureWithPieceAt(pos, structure.get().value());
    if (!start.isValid()) {
      return Optional.empty();
    }
    BlockPos center = start.getBoundingBox().getCenter();
    int floorY = level.getChunkSource().getGenerator().getSeaLevel() + PLATFORM_OFFSET;
    return Optional.of(new BlockPos(center.getX(), floorY, center.getZ()));
  }

  public static boolean isFightActive(ServerLevel level, BlockPos center) {

    return !level.getEntitiesOfClass(Gourdwyrm.class, new AABB(center).inflate(RADIUS * 3)).isEmpty();
  }

  public static boolean tryAwaken(ServerLevel level, ServerPlayer player, ItemStack lament) {

    Optional<BlockPos> found = centerAt(level, player.blockPosition());
    if (found.isEmpty()) {
      return false;
    }
    BlockPos center = found.get();
    if (player.position().distanceTo(Vec3.atBottomCenterOf(center)) > ALTAR_REACH) {
      player.sendOverlayMessage(Component.translatable("message.spookiness.netherrealm_altar"));
      return true;
    }
    if (isFightActive(level, center)) {
      player.sendOverlayMessage(Component.translatable("message.spookiness.netherrealm_active"));
      return true;
    }

    Gourdwyrm wyrm = Gourdwyrm.awaken(level, center, Vec3.atBottomCenterOf(center).add(0.0, WYRM_RISE, 0.0));
    if (wyrm == null) {
      return true;
    }
    for (BlockPos base : pillarBases(center)) {
      VigilCandle.plant(level, wyrm, Vec3.atBottomCenterOf(base.above(PILLAR_HEIGHT + 1)), false, true);
    }
    lament.consume(1, player);
    SoulBurst.spawn(level, Vec3.atCenterOf(center.above(2)), 64, 0.6, 0.12);
    return true;
  }
}
