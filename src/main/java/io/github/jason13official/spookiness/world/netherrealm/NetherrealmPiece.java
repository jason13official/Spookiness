package io.github.jason13official.spookiness.world.netherrealm;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

public class NetherrealmPiece extends StructurePiece {

  private static final BlockState FLOOR = Blocks.SOUL_SOIL.defaultBlockState();
  private static final BlockState FLOOR_ACCENT = Blocks.PACKED_MUD.defaultBlockState();
  private static final BlockState RIM = Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState();
  private static final BlockState SUPPORT = Blocks.BASALT.defaultBlockState();
  private static final BlockState RAIL = Blocks.DARK_OAK_FENCE.defaultBlockState();
  private static final BlockState PILLAR_CORE = Blocks.HAY_BLOCK.defaultBlockState();
  private static final BlockState PILLAR_FRAME = Blocks.DARK_OAK_LOG.defaultBlockState();
  private static final BlockState PILLAR_CAP = Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
  private static final BlockState ALTAR = Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState();
  private static final BlockState ALTAR_TOP = Blocks.JACK_O_LANTERN.defaultBlockState();
  private static final BlockState PUMPKIN = Blocks.PUMPKIN.defaultBlockState();
  private static final BlockState AIR = Blocks.AIR.defaultBlockState();

  private final BlockPos center;

  public NetherrealmPiece(BlockPos center) {
    super(ModStructures.NETHERREALM_PIECE, 0, NetherrealmArena.bounds(center));
    this.center = center;
    this.setOrientation(null);
  }

  public NetherrealmPiece(CompoundTag tag) {
    super(ModStructures.NETHERREALM_PIECE, tag);
    this.center = tag.read("Center", BlockPos.CODEC).orElseThrow();
  }

  @Override
  protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
    tag.store("Center", BlockPos.CODEC, this.center);
  }

  @Override
  public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox chunkBB, ChunkPos chunkPos,
      BlockPos referencePos) {

    int radius = NetherrealmArena.RADIUS;
    int floorY = this.center.getY();
    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    for (int x = chunkBB.minX(); x <= chunkBB.maxX(); x++) {
      for (int z = chunkBB.minZ(); z <= chunkBB.maxZ(); z++) {
        int dx = x - this.center.getX();
        int dz = z - this.center.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance > radius + 0.5) {
          continue;
        }

        for (int y = floorY + 1; y <= floorY + NetherrealmArena.CLEARANCE; y++) {
          place(level, chunkBB, pos.set(x, y, z), AIR);
        }

        boolean rim = distance > radius - 1.0;
        place(level, chunkBB, pos.set(x, floorY, z), rim ? RIM : floorBlock(x, z, distance));
        place(level, chunkBB, pos.set(x, floorY - 1, z), RIM);
        int depth = 2 + (int) ((radius - distance) / 4.0);
        for (int y = floorY - 2; y >= floorY - depth; y--) {
          place(level, chunkBB, pos.set(x, y, z), SUPPORT);
        }
        if (rim) {
          place(level, chunkBB, pos.set(x, floorY + 1, z), RAIL);
        }
        if (!rim && distance > 6.0 && Mth.getSeed(x, floorY, z) % 37 == 0) {
          place(level, chunkBB, pos.set(x, floorY + 1, z), PUMPKIN);
        }
      }
    }

    for (BlockPos pillar : NetherrealmArena.pillarBases(this.center)) {
      this.buildPillar(level, chunkBB, pillar, pos);
    }
    this.buildAltar(level, chunkBB, pos);
  }

  private static BlockState floorBlock(int x, int z, double distance) {

    if (distance < 7.0 && distance > 5.0) {
      return FLOOR_ACCENT;
    }
    return Math.floorMod(Mth.getSeed(x, 0, z), 9) == 0 ? FLOOR_ACCENT : FLOOR;
  }

  private void buildPillar(WorldGenLevel level, BoundingBox chunkBB, BlockPos base, BlockPos.MutableBlockPos pos) {

    for (int y = 1; y <= NetherrealmArena.PILLAR_HEIGHT; y++) {
      for (int ox = -1; ox <= 1; ox++) {
        for (int oz = -1; oz <= 1; oz++) {
          boolean corner = ox != 0 && oz != 0;
          BlockState state = y == NetherrealmArena.PILLAR_HEIGHT ? PILLAR_CAP : corner || y % 4 == 0 ? PILLAR_FRAME : PILLAR_CORE;
          place(level, chunkBB, pos.set(base.getX() + ox, base.getY() + y, base.getZ() + oz), state);
        }
      }
    }
  }

  private void buildAltar(WorldGenLevel level, BoundingBox chunkBB, BlockPos.MutableBlockPos pos) {

    for (int ox = -1; ox <= 1; ox++) {
      for (int oz = -1; oz <= 1; oz++) {
        place(level, chunkBB, pos.set(this.center.getX() + ox, this.center.getY() + 1, this.center.getZ() + oz), ALTAR);
      }
    }
    place(level, chunkBB, pos.set(this.center.getX(), this.center.getY() + 2, this.center.getZ()), ALTAR_TOP);
  }

  private static void place(WorldGenLevel level, BoundingBox chunkBB, BlockPos pos, BlockState state) {

    if (chunkBB.isInside(pos)) {
      level.setBlock(pos, state, 2);
    }
  }
}
