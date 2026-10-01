package io.github.jason13official.spookiness.lighting;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.Entity;

public final class LevelLights {

  private final Map<Entity, Source> sources = new HashMap<>();
  private final Map<Long, Integer> emissions = new ConcurrentHashMap<>();

  public void recompute(long pos) {

    int max = 0;
    for (Source source : this.sources.values()) {
      if (source.pos() == pos) {
        max = Math.max(max, source.emission());
      }
    }

    if (max > 0) {
      this.emissions.put(pos, max);
    } else {
      this.emissions.remove(pos);
    }
  }

  public Map<Entity, Source> getSources() {
    return sources;
  }

  public Map<Long, Integer> getEmissions() {
    return emissions;
  }
}
