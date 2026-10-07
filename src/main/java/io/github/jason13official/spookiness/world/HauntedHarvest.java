package io.github.jason13official.spookiness.world;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

public final class HauntedHarvest {

  public static final float WAKE_MULTIPLIER = 4.0F;

  private static final long CYCLE_TICKS = 192000L;
  private static final long START = 13000L;
  private static final long END = 23000L;

  private static final Map<UUID, Long> ANNOUNCED = new ConcurrentHashMap<>();

  public static boolean isActive(Level level) {
    if (level.dimension() != Level.OVERWORLD) {
      return false;
    }
    long time = level.getOverworldClockTime() % CYCLE_TICKS;
    return time >= START && time <= END;
  }

  public static float scale(Level level, float chance) {
    return isActive(level) ? chance * WAKE_MULTIPLIER : chance;
  }

  public static void clear() {

    ANNOUNCED.clear();
  }

  public static void tick(ServerPlayer player) {

    if (player.tickCount % 20 != 0 || !isActive(player.level())) {
      return;
    }
    long cycle = player.level().getOverworldClockTime() / CYCLE_TICKS;
    Long previous = ANNOUNCED.put(player.getUUID(), cycle);
    if (previous == null || previous != cycle) {
      player.sendSystemMessage(Component.translatable("message.spookiness.haunted_harvest").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
      player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMBIENT_CAVE.value(), SoundSource.AMBIENT, 1.0F, 0.6F);
    }
  }
}
