package io.github.jason13official.spookiness.lighting;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

public final class LivingLights {

  private static final Map<BlockGetter, LevelLights> LEVELS = new ConcurrentHashMap<>();

  public static int getEmission(BlockGetter level, long blockNode) {

    LevelLights lights = LEVELS.get(level);
    return lights == null ? 0 : lights.getEmission(blockNode);
  }

  public static void tick(Entity entity) {

    if (entity instanceof LightEmitter emitter) {
      update(entity, emitter.getLightEmission());
    } else if (entity instanceof LivingEntity living) {
      update(entity, LanternHeads.getEmission(living));
    }
  }

  public static void update(Entity entity, int emission) {

    Level level = entity.level();
    LevelLights lights = LEVELS.get(level);
    Source current = lights == null ? null : lights.get(entity);

    if (emission <= 0) {
      if (current != null) {
        lights.remove(entity);
        checkBlock(level, current.pos());
      }
      return;
    }

    long pos = entity.blockPosition().asLong();
    if (current != null && current.pos() == pos && current.emission() == emission) {
      return;
    }

    if (lights == null) {
      lights = LEVELS.computeIfAbsent(level, key -> new LevelLights());
    }
    lights.put(entity, new Source(pos, emission));
    if (current != null && current.pos() != pos) {
      checkBlock(level, current.pos());
    }
    checkBlock(level, pos);
  }

  public static void remove(Entity entity) {

    update(entity, 0);
  }

  public static void unload(BlockGetter level) {

    LEVELS.remove(level);
  }

  private static void checkBlock(Level level, long pos) {

    level.getChunkSource().getLightEngine().checkBlock(BlockPos.of(pos));
  }
}
