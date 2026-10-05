package io.github.jason13official.spookiness.entity;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public final class BookStairs {

  private static final double SEARCH_RADIUS = 16.0;
  private static final long EXPIRY_TICKS = 40;

  private static final Map<UUID, Guide> GUIDES = new ConcurrentHashMap<>();

  public static void requestStep(Player player, FloatingBook platform) {

    long now = platform.level().getGameTime();
    Guide guide = GUIDES.get(player.getUUID());

    if (guide != null && guide.book() == platform) {
      platform.stopGuiding();
      GUIDES.remove(player.getUUID());
      guide = null;
    }

    if (guide != null && guide.book().isAlive() && guide.book().isGuiding(player)) {
      GUIDES.put(player.getUUID(), new Guide(guide.book(), now));
      guide.book().setGuidePlatform(platform);
      return;
    }

    FloatingBook next = findCandidate(player, platform);
    if (next != null) {
      next.startGuiding(player, platform);
      GUIDES.put(player.getUUID(), new Guide(next, now));
    }
  }

  public static boolean isGuide(Player player, FloatingBook book) {

    Guide guide = GUIDES.get(player.getUUID());
    if (guide == null || guide.book() != book) {
      return false;
    }
    if (book.level().getGameTime() - guide.lastRequest() > EXPIRY_TICKS) {
      GUIDES.remove(player.getUUID());
      return false;
    }
    return true;
  }

  public static void forget(FloatingBook book) {

    GUIDES.values().removeIf(guide -> guide.book() == book);
  }

  private static @Nullable FloatingBook findCandidate(Player player, FloatingBook platform) {

    return player.level().getEntitiesOfClass(FloatingBook.class, player.getBoundingBox().inflate(SEARCH_RADIUS), book -> book != platform && book.canGuide())
        .stream()
        .min(Comparator.comparingDouble(book -> book.distanceToSqr(player)))
        .orElse(null);
  }

  private record Guide(FloatingBook book, long lastRequest) {
  }
}
