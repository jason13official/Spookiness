package io.github.jason13official.spookiness.util;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public final class Particles {

  private static final int SPIRAL_RISE_TICKS = 20;

  public static void soulBurst(ServerLevel level, Vec3 center, int count, double radius, double speed) {

    for (int i = 0; i < count; i++) {
      double y = 1.0 - (i + 0.5) * 2.0 / count;
      double ring = Math.sqrt(1.0 - y * y);
      double theta = SpookyMath.GOLDEN_ANGLE * i;
      double x = Math.cos(theta) * ring;
      double z = Math.sin(theta) * ring;

      level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x + x * radius, center.y + y * radius, center.z + z * radius, 0, x, y, z, speed);
    }
  }

  public static void risingSpiral(ServerLevel level, ParticleOptions particle, Vec3 base, double radius, double height, int arms, int ticks, double spin) {

    double rise = (ticks % SPIRAL_RISE_TICKS) / (double) SPIRAL_RISE_TICKS * height;
    for (int arm = 0; arm < arms; arm++) {
      Vec3 point = SpookyMath.onRing(base, SpookyMath.ringAngle(ticks * spin, arm, arms), radius);
      level.sendParticles(particle, point.x, point.y + rise, point.z, 1, 0.0, 0.0, 0.0, 0.0);
    }
  }
}
