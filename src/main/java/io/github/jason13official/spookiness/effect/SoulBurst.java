package io.github.jason13official.spookiness.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class SoulBurst {

  private static final double GOLDEN_ANGLE = Math.PI * (3.0 - Math.sqrt(5.0));

  public static void spawn(ServerLevel level, Vec3 center, int count, double radius, double speed) {

    for (int i = 0; i < count; i++) {
      double y = 1.0 - (i + 0.5) * 2.0 / count;
      double ring = Math.sqrt(1.0 - y * y);
      double theta = GOLDEN_ANGLE * i;
      double x = Math.cos(theta) * ring;
      double z = Math.sin(theta) * ring;

      level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x + x * radius, center.y + y * radius, center.z + z * radius, 0, x, y, z, speed);
    }
  }
}
