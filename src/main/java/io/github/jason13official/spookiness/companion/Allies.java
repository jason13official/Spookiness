package io.github.jason13official.spookiness.companion;

import io.github.jason13official.spookiness.entity.FloatingCandles;
import io.github.jason13official.spookiness.entity.FloatingLantern;
import io.github.jason13official.spookiness.entity.SpectralJackOMimic;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public final class Allies {

  private static final ThreadLocal<List<Entity>> CAPTURED = new ThreadLocal<>();

  public static <T> T allySpawned(Player player, Supplier<T> action) {

    List<Entity> previous = CAPTURED.get();
    List<Entity> spawned = new ArrayList<>();
    CAPTURED.set(spawned);
    try {
      return action.get();
    } finally {
      CAPTURED.set(previous);
      spawned.forEach(entity -> ally(entity, player));
    }
  }

  public static void onEntityJoin(Entity entity) {

    List<Entity> spawned = CAPTURED.get();
    if (spawned != null) {
      spawned.add(entity);
    }
  }

  public static boolean ally(Entity entity, Player player) {

    if (!(entity.level() instanceof ServerLevel level) || !entity.isAlive()) {
      return false;
    }
    if (entity instanceof SpectralJackOMimic mimic) {
      mimic.setOwner(player);
      return true;
    }
    if (entity instanceof FloatingCandles candles) {
      candles.joinLine(level, player);
      return true;
    }
    if (entity instanceof FloatingLantern lantern) {
      return lantern.claim(level, player);
    }
    return false;
  }
}
