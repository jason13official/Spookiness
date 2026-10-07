package io.github.jason13official.spookiness.entity.boss.mother;

import io.github.jason13official.spookiness.entity.JackOMimic;
import io.github.jason13official.spookiness.util.SpookyMath;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

final class MotherBurst {

  private static final int CHECK_INTERVAL = 10;
  private static final int COOLDOWN = 500;
  private static final double CROWD_RANGE = 5.0;
  private static final int CROWD_COUNT = 4;
  private static final int SPIT_GAP = 4;
  private static final int MAX_SPITS = HallowedMother.MAX_GOURDLINGS;
  private static final int GATHER_TICKS = 40;
  private static final double GATHER_PULL = 0.35;
  private static final double GATHER_STOP = 1.5;
  private static final double BLAST_RADIUS = 10.0;
  private static final double BLAST_SPEED = 1.0;
  private static final double BLAST_LIFT = 0.75;
  private static final float BLAST_DAMAGE = 6.0F;

  private final HallowedMother mother;
  private Stage stage = Stage.NONE;
  private int ticks;
  private int spits;
  private int cooldown = COOLDOWN / 5;

  MotherBurst(HallowedMother mother) {
    this.mother = mother;
  }

  boolean isActive() {
    return this.stage != Stage.NONE;
  }

  boolean tryStart(ServerLevel level) {

    if (--this.cooldown > 0 || this.mother.tickCount % CHECK_INTERVAL != 0 || !this.isCrowded(level)) {
      return false;
    }
    this.mother.releaseTether();
    this.mother.cancelVolley();
    this.stage = Stage.SPITTING;
    this.ticks = 0;
    this.spits = 0;
    this.mother.playSound(SoundEvents.RAVAGER_ROAR, 3.0F, 0.6F);
    return true;
  }

  void tick(ServerLevel level, @Nullable LivingEntity target) {

    this.ticks++;
    switch (this.stage) {
      case SPITTING -> this.spit(level, target);
      case GATHERING -> this.gather(level);
      default -> this.stage = Stage.NONE;
    }
  }

  private void spit(ServerLevel level, @Nullable LivingEntity target) {

    if (this.ticks % SPIT_GAP != 0) {
      return;
    }
    if (this.mother.brood().living(level).stream().filter(JackOMimic.class::isInstance).count() >= HallowedMother.MAX_GOURDLINGS || this.spits >= MAX_SPITS) {
      this.stage = Stage.GATHERING;
      this.ticks = 0;
      this.mother.setGaping(true);
      this.mother.playSound(SoundEvents.WARDEN_ROAR, 3.0F, 0.8F);
      return;
    }
    this.mother.spit(level, target, 180.0F);
    this.spits++;
  }

  private void gather(ServerLevel level) {

    Vec3 mouth = this.mother.position().add(this.mother.mouthOffset(this.mother.yBodyRot));
    for (Mob mob : this.mother.brood().living(level)) {
      Vec3 toMouth = mouth.subtract(mob.position());
      if (toMouth.lengthSqr() > GATHER_STOP * GATHER_STOP) {
        Vec3 pull = toMouth.normalize().scale(GATHER_PULL);
        mob.setDeltaMovement(pull.x, Math.max(pull.y, mob.getDeltaMovement().y), pull.z);
        mob.hurtMarked = true;
      }
      if (this.ticks % 4 == 0) {
        level.sendParticles(ParticleTypes.SOUL, mob.getX(), mob.getY() + 0.5, mob.getZ(), 2, 0.2, 0.2, 0.2, 0.01);
      }
    }
    if (this.ticks % 5 == 0) {
      level.sendParticles(ParticleTypes.FLAME, mouth.x, mouth.y, mouth.z, 10, 0.6, 0.4, 0.6, 0.02);
    }
    if (this.ticks >= GATHER_TICKS) {
      this.blast(level);
      this.mother.setGaping(false);
      this.stage = Stage.NONE;
      this.cooldown = COOLDOWN;
    }
  }

  private void blast(ServerLevel level) {

    Vec3 center = this.mother.position();
    for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, this.mother.getBoundingBox().inflate(BLAST_RADIUS),
        entity -> entity != this.mother && entity.isAlive())) {
      if (entity instanceof Player player && (player.isSpectator() || player.isCreative())) {
        continue;
      }
      if (!this.mother.isBrood(entity)) {
        entity.hurtServer(level, this.mother.damageSources().mobAttack(this.mother), BLAST_DAMAGE);
      }
      Vec3 away = entity.position().subtract(center).multiply(1.0, 0.0, 1.0);
      Vec3 direction = away.lengthSqr() > 1.0E-4 ? away.normalize() : Vec3.directionFromRotation(0.0F, SpookyMath.randomYaw(this.mother.getRandom()));
      entity.setDeltaMovement(direction.x * BLAST_SPEED, BLAST_LIFT, direction.z * BLAST_SPEED);
      entity.hurtMarked = true;
    }

    level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y + 1.5, center.z, 1, 0.0, 0.0, 0.0, 0.0);
    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y + 1.0, center.z, 120, 3.0, 1.0, 3.0, 0.3);
    level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 3.0F, 0.6F);
  }

  private boolean isCrowded(ServerLevel level) {

    return level.getEntitiesOfClass(LivingEntity.class, this.mother.getBoundingBox().inflate(CROWD_RANGE),
        entity -> entity != this.mother && entity.isAlive() && !(entity instanceof JackOMimic) && !this.mother.isBrood(entity)
            && !(entity instanceof Player player && (player.isSpectator() || player.isCreative())))
        .size() >= CROWD_COUNT;
  }

  private enum Stage {
    NONE,
    SPITTING,
    GATHERING
  }
}
