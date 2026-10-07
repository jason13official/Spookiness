package io.github.jason13official.spookiness.world;

import net.minecraft.world.level.Level;

public final class SpookyTime {

  public static final long DAY_TICKS = 24000L;
  public static final long NOON = 6000L;
  public static final long DUSK = 12000L;
  public static final long NIGHT_START = 13000L;
  public static final long NIGHT_END = 23000L;
  public static final long HARVEST_INTERVAL_DAYS = 8L;
  public static final long HARVEST_CYCLE_TICKS = DAY_TICKS * HARVEST_INTERVAL_DAYS;

  public static long timeOfDay(Level level) {
    return level.getOverworldClockTime() % DAY_TICKS;
  }

  public static long day(Level level) {
    return level.getOverworldClockTime() / DAY_TICKS;
  }

  public static boolean isNight(long timeOfDay) {
    return timeOfDay >= NIGHT_START && timeOfDay <= NIGHT_END;
  }

  public static boolean isAfternoon(long timeOfDay) {
    return timeOfDay >= NOON && timeOfDay < DUSK;
  }
}
