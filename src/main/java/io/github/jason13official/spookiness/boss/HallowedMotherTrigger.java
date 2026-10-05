package io.github.jason13official.spookiness.boss;

import io.github.jason13official.spookiness.companion.Hallowing;
import io.github.jason13official.spookiness.entity.boss.HallowedMother;
import io.github.jason13official.spookiness.registry.ModAttachments;
import io.github.jason13official.spookiness.worldgen.ModBiomeModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public final class HallowedMotherTrigger {

  public static final int REQUIRED_ALLIES = 5;

  private static final int CHECK_INTERVAL = 100;
  private static final long DAY_LENGTH = 24000L;
  private static final long MIDNIGHT_START = 17500L;
  private static final long MIDNIGHT_END = 18500L;
  private static final double MIN_DISTANCE = 20.0;
  private static final double MAX_DISTANCE = 30.0;

  public static void tick(ServerPlayer player) {

    if (player.tickCount % CHECK_INTERVAL != 0 || player.isSpectator()) {
      return;
    }
    ServerLevel level = player.level();
    if (level.dimension() != Level.OVERWORLD) {
      return;
    }

    long time = level.getOverworldClockTime();
    long dayTime = time % DAY_LENGTH;
    long night = time / DAY_LENGTH;
    if (dayTime < MIDNIGHT_START || dayTime > MIDNIGHT_END || player.getData(ModAttachments.MOTHER_NIGHT) == night) {
      return;
    }

    BlockPos pos = player.blockPosition();
    if (!level.canSeeSky(pos) || !level.getBiome(pos).is(ModBiomeModifiers.HAS_PUMPKIN_PATCH) || Hallowing.count(player) < REQUIRED_ALLIES) {
      return;
    }

    player.setData(ModAttachments.MOTHER_NIGHT, night);
    player.sendSystemMessage(Component.translatable("message.spookiness.mother_warning"));

    double angle = player.getRandom().nextDouble() * Math.PI * 2.0;
    double distance = MIN_DISTANCE + player.getRandom().nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
    int x = (int) Math.floor(player.getX() + Math.cos(angle) * distance);
    int z = (int) Math.floor(player.getZ() + Math.sin(angle) * distance);
    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    HallowedMother.erupt(level, player, new Vec3(x + 0.5, y, z + 0.5));
  }
}
