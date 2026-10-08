package io.github.jason13official.spookiness.util;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class PoisonClouds {

  public static final int ORANGE = 0xFF8A1E;
  public static final int GREEN = 0x6BCB3A;

  private static final int WAIT_TICKS = 10;
  private static final int POISON_TICKS = 80;

  public static void spawn(ServerLevel level, @Nullable LivingEntity owner, Vec3 pos, float radius, int duration) {

    AreaEffectCloud cloud = new AreaEffectCloud(level, pos.x, pos.y, pos.z);
    if (owner != null) {
      cloud.setOwner(owner);
    }
    cloud.setRadius(radius);
    cloud.setDuration(duration);
    cloud.setRadiusPerTick(-radius / duration);
    cloud.setWaitTime(WAIT_TICKS);
    cloud.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_TICKS, 0));
    cloud.setCustomParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, level.getRandom().nextBoolean() ? ORANGE : GREEN));
    level.addFreshEntity(cloud);
  }
}
