package io.github.jason13official.spookiness.entity.boss.mother;

import io.github.jason13official.spookiness.companion.Hallowing;
import java.util.Comparator;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

final class MotherTether {

  private static final int INTERVAL = 160;
  private static final int RECLAIM_TICKS = 80;
  private static final double RANGE = 24.0;
  private static final double PULL = 0.15;
  private static final double HOLD_DISTANCE = 4.5;
  private static final double HOLD_TOLERANCE = 0.4;
  private static final float RECLAIM_HEAL = 40.0F;

  private final HallowedMother mother;
  private int ticks;
  private int cooldown = INTERVAL;

  MotherTether(HallowedMother mother) {
    this.mother = mother;
  }

  void release() {

    Entity tethered = this.mother.getTethered();
    if (tethered != null && this.mother.level() instanceof ServerLevel level) {
      level.sendParticles(ParticleTypes.SOUL, tethered.getX(), tethered.getY() + 1.0, tethered.getZ(), 16, 0.3, 0.3, 0.3, 0.05);
    }
    this.mother.setTethered(null);
    this.ticks = 0;
  }

  void tick(ServerLevel level) {

    Entity tethered = this.mother.getTethered();
    if (tethered == null) {
      if (this.mother.hasTether()) {
        this.release();
      }
      this.seek(level);
      return;
    }

    if (!(tethered instanceof Mob mob) || !mob.isAlive() || !Hallowing.isHallowed(mob)) {
      this.release();
      return;
    }

    this.pull(level, mob);
    if (++this.ticks >= RECLAIM_TICKS) {
      this.reclaim(level, mob);
    }
  }

  private void seek(ServerLevel level) {

    if (--this.cooldown > 0) {
      return;
    }
    this.cooldown = INTERVAL;
    level.getEntitiesOfClass(Mob.class, this.mother.getBoundingBox().inflate(RANGE), Hallowing::isHallowed).stream()
        .min(Comparator.comparingDouble(this.mother::distanceToSqr))
        .ifPresent(mob -> {
          this.mother.setTethered(mob);
          this.ticks = 0;
          this.mother.playSound(SoundEvents.VINE_STEP, 3.0F, 0.5F);
          this.mother.playSound(SoundEvents.EVOKER_PREPARE_ATTACK, 2.0F, 0.6F);
        });
  }

  private void pull(ServerLevel level, Mob mob) {

    Vec3 mouth = this.mother.position().add(this.mother.mouthOffset(this.mother.yBodyRot));
    if (this.ticks % 3 == 0) {
      Vec3 along = mob.position().add(0.0, mob.getBbHeight() * 0.6, 0.0).subtract(mouth);
      for (int i = 1; i < 8; i++) {
        Vec3 point = mouth.add(along.scale(i / 8.0));
        level.sendParticles(ParticleTypes.COMPOSTER, point.x, point.y, point.z, 1, 0.05, 0.05, 0.05, 0.0);
      }
    }
    float radians = this.mother.yBodyRot * Mth.DEG_TO_RAD;
    Vec3 hold = new Vec3(this.mother.getX() - Mth.sin(radians) * HOLD_DISTANCE, mob.getY(), this.mother.getZ() + Mth.cos(radians) * HOLD_DISTANCE);
    Vec3 toHold = hold.subtract(mob.position());
    Vec3 pull = toHold.lengthSqr() > HOLD_TOLERANCE * HOLD_TOLERANCE ? toHold.normalize().scale(Math.min(PULL, toHold.length())) : Vec3.ZERO;
    mob.setDeltaMovement(pull.x, mob.getDeltaMovement().y, pull.z);
    mob.getNavigation().stop();
    mob.hurtMarked = true;
  }

  private void reclaim(ServerLevel level, Mob mob) {

    Hallowing.unhallow(mob);
    MotherBrood.adopt(mob);
    this.mother.brood().add(mob);
    mob.setTarget(this.mother.brood().target());
    this.mother.heal(RECLAIM_HEAL);
    level.sendParticles(ParticleTypes.SCULK_SOUL, mob.getX(), mob.getY() + 1.0, mob.getZ(), 24, 0.4, 0.4, 0.4, 0.05);
    this.mother.playSound(SoundEvents.SOUL_ESCAPE.value(), 2.0F, 0.4F);
    this.mother.setTethered(null);
    this.ticks = 0;
  }
}
