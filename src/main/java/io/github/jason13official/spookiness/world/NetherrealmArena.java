package io.github.jason13official.spookiness.world;

import io.github.jason13official.spookiness.util.SpookyMath;
import io.github.jason13official.spookiness.registry.ModStructures;
import com.mojang.datafixers.util.Pair;
import io.github.jason13official.spookiness.block.entity.SoullessJackOMimicBlockEntity;
import io.github.jason13official.spookiness.effect.Particles;
import io.github.jason13official.spookiness.entity.boss.gourdwyrm.Gourdwyrm;
import io.github.jason13official.spookiness.entity.boss.VigilCandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import io.github.jason13official.spookiness.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
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
  private static final double WYRM_RISE = 16.0;

  public static BoundingBox bounds(BlockPos center) {

    return new BoundingBox(center.getX() - RADIUS - 1, center.getY() - RADIUS / 4 - 3, center.getZ() - RADIUS - 1, center.getX() + RADIUS + 1,
        center.getY() + CLEARANCE, center.getZ() + RADIUS + 1);
  }

  public static List<BlockPos> pillarBases(BlockPos center) {

    List<BlockPos> bases = new ArrayList<>(PILLARS);
    for (int i = 0; i < PILLARS; i++) {
      Vec3 offset = SpookyMath.onRing(Vec3.ZERO, SpookyMath.ringAngle(0.0, i, PILLARS), PILLAR_RING);
      bases.add(center.offset((int) Math.round(offset.x), 0, (int) Math.round(offset.z)));
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

  public static BlockPos altarPos(BlockPos center) {
    return center.above(2);
  }

  public static void onAltarChanged(ServerLevel level, BlockPos pos, ItemStack inserted, ItemStack removed, @Nullable Player player) {

    Optional<BlockPos> found = centerAt(level, pos);
    if (found.isEmpty() || !altarPos(found.get()).equals(pos)) {
      return;
    }
    BlockPos center = found.get();
    if (removed.is(ModItems.LAMENT_CONFIGURATION)) {
      forfeit(level, center);
    }
    if (inserted.is(ModItems.LAMENT_CONFIGURATION) && !isFightActive(level, center)) {
      awaken(level, center);
    }
  }

  private static void awaken(ServerLevel level, BlockPos center) {

    Gourdwyrm wyrm = Gourdwyrm.awaken(level, center, Vec3.atBottomCenterOf(center).add(0.0, WYRM_RISE, 0.0));
    if (wyrm == null) {
      return;
    }
    for (BlockPos base : pillarBases(center)) {
      VigilCandle.plant(level, wyrm, Vec3.atBottomCenterOf(base.above(PILLAR_HEIGHT + 1)), false, true);
    }
    Particles.soulBurst(level, Vec3.atCenterOf(altarPos(center)), 64, 0.6, 0.12);
  }

  private static void forfeit(ServerLevel level, BlockPos center) {

    AABB arena = new AABB(center).inflate(RADIUS * 3);
    for (Gourdwyrm wyrm : level.getEntitiesOfClass(Gourdwyrm.class, arena)) {
      level.sendParticles(ParticleTypes.LARGE_SMOKE, wyrm.getX(), wyrm.getY() + 1.5, wyrm.getZ(), 60, 1.5, 1.5, 1.5, 0.05);
      wyrm.discard();
    }
    snuffGreatCandles(level, arena);
    level.playSound(null, center, SoundEvents.ENDER_DRAGON_AMBIENT, SoundSource.HOSTILE, 3.0F, 0.4F);
  }

  public static void onWyrmDefeated(ServerLevel level, BlockPos center) {

    snuffGreatCandles(level, new AABB(center).inflate(RADIUS * 3));
    if (level.getBlockEntity(altarPos(center)) instanceof SoullessJackOMimicBlockEntity altar) {
      altar.release(level, null, true);
    }
  }

  private static void snuffGreatCandles(ServerLevel level, AABB area) {

    for (VigilCandle candle : level.getEntitiesOfClass(VigilCandle.class, area, VigilCandle::isGreat)) {
      candle.snuff(level);
    }
  }
}
