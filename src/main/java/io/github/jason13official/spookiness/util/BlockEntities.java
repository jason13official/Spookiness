package io.github.jason13official.spookiness.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public final class BlockEntities {

  public static <T extends BlockEntity> List<T> near(Level level, Vec3 center, double radius, Class<T> type) {

    int minX = SectionPos.blockToSectionCoord(center.x - radius);
    int maxX = SectionPos.blockToSectionCoord(center.x + radius);
    int minZ = SectionPos.blockToSectionCoord(center.z - radius);
    int maxZ = SectionPos.blockToSectionCoord(center.z + radius);

    List<T> found = new ArrayList<>();
    for (int x = minX; x <= maxX; x++) {
      for (int z = minZ; z <= maxZ; z++) {
        for (BlockEntity blockEntity : level.getChunk(x, z).getBlockEntities().values()) {
          if (type.isInstance(blockEntity) && blockEntity.getBlockPos().closerToCenterThan(center, radius)) {
            found.add(type.cast(blockEntity));
          }
        }
      }
    }
    return found;
  }
}
