package io.github.jason13official.spookiness.lighting;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

public final class LivingLights {

  private static final Map<BlockGetter, LevelLights> LEVELS = new ConcurrentHashMap<>();

  public static int getEmission(BlockGetter level, long blockNode) {

    LevelLights lights = LEVELS.get(level);
    return lights == null ? 0 : lights.getEmissions().getOrDefault(blockNode, 0);
  }

  public static void add(Entity entity, int emission) {

    if (emission <= 0) {
      return;
    }

    Level level = entity.level();
    LevelLights lights = LEVELS.computeIfAbsent(level, key -> new LevelLights());
    long pos = entity.blockPosition().asLong();

    lights.getSources().put(entity, new Source(pos, emission));
    lights.recompute(pos);
    checkBlock(level, pos);
  }

  public static boolean has(Entity entity) {

    LevelLights lights = LEVELS.get(entity.level());
    return lights != null && lights.getSources().containsKey(entity);
  }

  public static void move(Entity entity) {

    LevelLights lights = LEVELS.get(entity.level());
    if (lights == null) {
      return;
    }

    Source source = lights.getSources().get(entity);
    long to = entity.blockPosition().asLong();
    if (source == null || source.pos() == to) {
      return;
    }

    lights.getSources().put(entity, new Source(to, source.emission()));
    lights.recompute(source.pos());
    lights.recompute(to);
    checkBlock(entity.level(), source.pos());
    checkBlock(entity.level(), to);
  }

  public static void remove(Entity entity) {

    LevelLights lights = LEVELS.get(entity.level());
    if (lights == null) {
      return;
    }

    Source source = lights.getSources().remove(entity);
    if (source == null) {
      return;
    }

    lights.recompute(source.pos());
    checkBlock(entity.level(), source.pos());
  }

  public static void unload(BlockGetter level) {

    LEVELS.remove(level);
  }

  private static void checkBlock(Level level, long pos) {

    level.getChunkSource().getLightEngine().checkBlock(BlockPos.of(pos));
  }
}
