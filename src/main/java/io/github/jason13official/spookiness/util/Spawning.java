package io.github.jason13official.spookiness.util;

import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.jspecify.annotations.Nullable;

public final class Spawning {

  public static <T extends Entity> @Nullable T spawn(ServerLevel level, EntityType<T> type, EntitySpawnReason reason, Vec3 pos, float yRot) {
    return spawn(level, type, reason, pos, yRot, entity -> {
    });
  }

  public static <T extends Entity> @Nullable T spawn(ServerLevel level, EntityType<T> type, EntitySpawnReason reason, Vec3 pos, float yRot, Consumer<? super T> setup) {

    T entity = type.create(level, reason);
    if (entity == null) {
      return null;
    }
    entity.snapTo(pos.x, pos.y, pos.z, yRot, 0.0F);
    setup.accept(entity);
    level.addFreshEntity(entity);
    return entity;
  }

  public static <T extends Mob> @Nullable T spawnFinalized(ServerLevel level, EntityType<T> type, EntitySpawnReason reason, Vec3 pos, float yRot) {
    return spawnFinalized(level, type, reason, pos, yRot, mob -> {
    });
  }

  public static <T extends Mob> @Nullable T spawnFinalized(ServerLevel level, EntityType<T> type, EntitySpawnReason reason, Vec3 pos, float yRot,
      Consumer<? super T> setup) {

    T mob = type.create(level, reason);
    if (mob == null) {
      return null;
    }
    mob.snapTo(pos.x, pos.y, pos.z, yRot, 0.0F);
    setup.accept(mob);
    EventHooks.finalizeMobSpawn(mob, level, level.getCurrentDifficultyAt(BlockPos.containing(pos)), reason, null);
    if (mob.isSpawnCancelled()) {
      mob.discard();
      return null;
    }
    level.addFreshEntity(mob);
    return mob;
  }
}
