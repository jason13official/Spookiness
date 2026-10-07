package io.github.jason13official.spookiness.util;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

public final class SpookyMath {

  public static final double FULL_TURN = Math.PI * 2.0;
  public static final double GOLDEN_ANGLE = Math.PI * (3.0 - Math.sqrt(5.0));

  public static float yawToward(double dx, double dz) {
    return (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
  }

  public static float yawToward(Vec3 direction) {
    return yawToward(direction.x, direction.z);
  }

  public static double randomAngle(RandomSource random) {
    return random.nextDouble() * FULL_TURN;
  }

  public static double ringAngle(double startAngle, int index, int count) {
    return startAngle + FULL_TURN * index / count;
  }

  public static Vec3 onRing(Vec3 center, double angle, double radius) {
    return center.add(Math.cos(angle) * radius, 0.0, Math.sin(angle) * radius);
  }
}
