package io.github.jason13official.spookiness.entity.boss.mother;

import io.github.jason13official.spookiness.companion.Hallowing;
import io.github.jason13official.spookiness.entity.JackOMimic;
import io.github.jason13official.spookiness.entity.SpectralJackOMimic;
import java.util.Comparator;
import java.util.UUID;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

final class MotherFeast {

  private static final int CHECK_INTERVAL = 10;
  private static final int COOLDOWN = 400;
  private static final double RANGE = 16.0;
  private static final float MAX_PREY_WIDTH = 1.5F;
  private static final int INHALE_TICKS = 40;
  private static final double INHALE_PULL = 0.5;
  private static final double SWALLOW_DISTANCE = 1.5;
  private static final int DIGEST_TICKS = 120;
  private static final int DIGEST_INTERVAL = 30;
  private static final float DIGEST_DAMAGE = 2.0F;
  private static final int DIGEST_POISON_TICKS = 60;
  private static final double BELLY_FORWARD = 0.6;
  private static final double BELLY_HEIGHT = 1.0;
  private static final double SPIT_OUT_SPEED = 1.4;
  private static final double SPIT_OUT_LIFT = 0.5;

  private final HallowedMother mother;
  private Stage stage = Stage.NONE;
  private int ticks;
  private int cooldown = COOLDOWN / 2;
  private @Nullable UUID meal;
  private boolean spitEarly;

  MotherFeast(HallowedMother mother) {
    this.mother = mother;
  }

  boolean isActive() {
    return this.stage != Stage.NONE;
  }

  void spitEarly() {
    if (this.stage == Stage.DIGESTING) {
      this.spitEarly = true;
    }
  }

  boolean tryStart(ServerLevel level) {

    if (--this.cooldown > 0 || this.mother.tickCount % CHECK_INTERVAL != 0) {
      return false;
    }
    LivingEntity prey = level.getEntitiesOfClass(LivingEntity.class, this.mother.getBoundingBox().inflate(RANGE), this::isPrey).stream()
        .min(Comparator.comparingDouble((LivingEntity entity) -> -entity.getHealth()).thenComparingDouble(this.mother::distanceToSqr))
        .orElse(null);
    if (prey == null) {
      return false;
    }
    this.mother.releaseTether();
    this.mother.cancelVolley();
    this.meal = prey.getUUID();
    this.stage = Stage.INHALING;
    this.ticks = 0;
    this.spitEarly = false;
    this.mother.setGaping(true);
    this.mother.playSound(SoundEvents.WARDEN_SNIFF, 3.0F, 0.5F);
    this.mother.playSound(SoundEvents.BREEZE_IDLE_GROUND, 1.5F, 0.6F);
    return true;
  }

  void tick(ServerLevel level) {

    this.ticks++;
    LivingEntity prey = this.meal != null && level.getEntity(this.meal) instanceof LivingEntity living && living.isAlive() ? living : null;
    if (prey == null || prey instanceof Player player && (player.isSpectator() || player.isCreative())) {
      this.end();
      return;
    }

    Vec3 belly = this.bellyPosition();
    if (this.stage == Stage.INHALING) {
      this.inhale(level, prey, belly);
      return;
    }

    prey.setDeltaMovement(Vec3.ZERO);
    prey.teleportTo(belly.x, belly.y, belly.z);
    prey.fallDistance = 0.0;
    if (this.ticks % DIGEST_INTERVAL == 0) {
      prey.addEffect(new MobEffectInstance(MobEffects.POISON, DIGEST_POISON_TICKS, 1), this.mother);
      prey.hurtServer(level, this.mother.damageSources().indirectMagic(this.mother, this.mother), DIGEST_DAMAGE);
      level.broadcastEntityEvent(this.mother, HallowedMother.SPIT_EVENT);
      level.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, HallowedMother.CLOUD_GREEN), belly.x, belly.y + 1.0, belly.z, 16, 0.6, 0.4, 0.6,
          0.0);
      this.mother.playSound(SoundEvents.GENERIC_EAT.value(), 2.0F, 0.6F);
    }
    if (this.ticks >= DIGEST_TICKS || this.spitEarly) {
      this.spitOut(level, prey);
    }
  }

  private void inhale(ServerLevel level, LivingEntity prey, Vec3 belly) {

    Vec3 toBelly = belly.subtract(prey.position());
    if (toBelly.length() > RANGE + 4.0) {
      this.end();
      return;
    }
    Vec3 pull = toBelly.normalize().scale(Math.min(INHALE_PULL, toBelly.length()));
    prey.setDeltaMovement(pull);
    prey.hurtMarked = true;
    level.sendParticles(ParticleTypes.CLOUD, prey.getX(), prey.getY() + prey.getBbHeight() * 0.5, prey.getZ(), 3, 0.3, 0.3, 0.3, 0.02);
    if (toBelly.length() < SWALLOW_DISTANCE || this.ticks >= INHALE_TICKS) {
      this.stage = Stage.DIGESTING;
      this.ticks = 0;
      this.mother.playSound(SoundEvents.GENERIC_EAT.value(), 3.0F, 0.5F);
    }
  }

  private void spitOut(ServerLevel level, LivingEntity prey) {

    float radians = this.mother.yBodyRot * Mth.DEG_TO_RAD;
    Vec3 forward = new Vec3(-Mth.sin(radians), 0.0, Mth.cos(radians));
    Vec3 mouth = this.mother.position().add(this.mother.mouthOffset(this.mother.yBodyRot));
    prey.teleportTo(mouth.x, mouth.y, mouth.z);
    prey.setDeltaMovement(forward.x * SPIT_OUT_SPEED, SPIT_OUT_LIFT, forward.z * SPIT_OUT_SPEED);
    prey.hurtMarked = true;
    level.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, HallowedMother.CLOUD_ORANGE), mouth.x, mouth.y, mouth.z, 24, 0.5, 0.5, 0.5, 0.0);
    this.mother.playSound(SoundEvents.PLAYER_BURP, 3.0F, 0.5F);
    this.end();
  }

  private void end() {

    this.stage = Stage.NONE;
    this.ticks = 0;
    this.meal = null;
    this.spitEarly = false;
    this.cooldown = COOLDOWN;
    this.mother.setGaping(false);
  }

  private Vec3 bellyPosition() {

    float radians = this.mother.yBodyRot * Mth.DEG_TO_RAD;
    return this.mother.position().add(-Mth.sin(radians) * BELLY_FORWARD, BELLY_HEIGHT, Mth.cos(radians) * BELLY_FORWARD);
  }

  private boolean isPrey(LivingEntity entity) {

    if (entity == this.mother || !entity.isAlive() || entity instanceof JackOMimic || this.mother.isBrood(entity) || entity.getBbWidth() > MAX_PREY_WIDTH) {
      return false;
    }
    if (entity instanceof Player player) {
      return !player.isSpectator() && !player.isCreative();
    }
    return Hallowing.isHallowed(entity) || entity instanceof IronGolem || entity instanceof SpectralJackOMimic
        || entity instanceof Mob mob && mob.getTarget() == this.mother;
  }

  private enum Stage {
    NONE,
    INHALING,
    DIGESTING
  }
}
