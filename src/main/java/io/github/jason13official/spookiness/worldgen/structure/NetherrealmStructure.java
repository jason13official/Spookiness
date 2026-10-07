package io.github.jason13official.spookiness.worldgen.structure;

import io.github.jason13official.spookiness.registry.ModStructures;
import io.github.jason13official.spookiness.world.NetherrealmArena;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class NetherrealmStructure extends Structure {

  public static final MapCodec<NetherrealmStructure> CODEC = simpleCodec(NetherrealmStructure::new);

  public NetherrealmStructure(Structure.StructureSettings settings) {
    super(settings);
  }

  @Override
  public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {

    ChunkPos chunkPos = context.chunkPos();
    int y = context.chunkGenerator().getSeaLevel() + NetherrealmArena.PLATFORM_OFFSET;
    BlockPos center = new BlockPos(chunkPos.getMiddleBlockX(), y, chunkPos.getMiddleBlockZ());
    return Optional.of(new Structure.GenerationStub(center, builder -> builder.addPiece(new NetherrealmPiece(center))));
  }

  @Override
  public StructureType<?> type() {
    return ModStructures.NETHERREALM_TYPE;
  }
}
