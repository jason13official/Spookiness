package io.github.jason13official.spookiness.lighting;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

public final class LevelLights {

  private final Map<Entity, Source> sources = new HashMap<>();
  private final Map<Long, Map<Entity, Integer>> byPos = new HashMap<>();
  private final Map<Long, Integer> emissions = new ConcurrentHashMap<>();

  public int getEmission(long pos) {
    return this.emissions.getOrDefault(pos, 0);
  }

  public @Nullable Source get(Entity entity) {
    return this.sources.get(entity);
  }

  public void put(Entity entity, Source source) {

    Source previous = this.sources.put(entity, source);
    if (previous != null) {
      this.detach(entity, previous.pos());
    }
    this.byPos.computeIfAbsent(source.pos(), pos -> new HashMap<>()).put(entity, source.emission());
    this.recompute(source.pos());
  }

  public @Nullable Source remove(Entity entity) {

    Source previous = this.sources.remove(entity);
    if (previous != null) {
      this.detach(entity, previous.pos());
    }
    return previous;
  }

  private void detach(Entity entity, long pos) {

    Map<Entity, Integer> atPos = this.byPos.get(pos);
    if (atPos != null) {
      atPos.remove(entity);
      if (atPos.isEmpty()) {
        this.byPos.remove(pos);
      }
    }
    this.recompute(pos);
  }

  private void recompute(long pos) {

    Map<Entity, Integer> atPos = this.byPos.get(pos);
    int max = 0;
    if (atPos != null) {
      for (int emission : atPos.values()) {
        max = Math.max(max, emission);
      }
    }

    if (max > 0) {
      this.emissions.put(pos, max);
    } else {
      this.emissions.remove(pos);
    }
  }
}
