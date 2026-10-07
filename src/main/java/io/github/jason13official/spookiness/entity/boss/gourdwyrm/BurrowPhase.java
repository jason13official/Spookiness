package io.github.jason13official.spookiness.entity.boss.gourdwyrm;

import io.github.jason13official.spookiness.util.SpookyMath;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

final class BurrowPhase extends GourdwyrmPhase {

  private static final double DEPTH = 10.0;
  private static final double DIVE_SPEED = 1.0;
  private static final int DIVE_TICKS = 80;
  private static final int RIPPLE_TICKS = 40;
  private static final double RETARGET_RANGE = 24.0;
  private static final double GEYSER_SPEED = 1.6;
  private static final double GEYSER_RADIUS = 3.0;
  private static final float GEYSER_DAMAGE = 10.0F;

  private Stage stage = Stage.DIVE;
  private @Nullable Vec3 strikePoint;

  BurrowPhase(Gourdwyrm wyrm) {
    super(wyrm);
  }

  @Override
  void begin() {
    super.begin();
    this.stage = Stage.DIVE;
    this.strikePoint = null;
  }

  @Override
  protected void tick(ServerLevel level) {

    switch (this.stage) {
      case DIVE -> this.dive(level);
      case RIPPLE -> this.ripple(level);
      case GEYSER -> this.geyser(level);
    }
  }

  private void dive(ServerLevel level) {

    if (this.strikePoint == null) {
      Player target = this.wyrm.pickTarget(level);
      if (target == null) {
        this.wyrm.setPhase(Gourdwyrm.Phase.CIRCLING);
        return;
      }
      this.strikePoint = new Vec3(target.getX(), Math.floor(target.getY()), target.getZ());
      this.wyrm.playSound(SoundEvents.ENDER_DRAGON_GROWL, 4.0F, 0.8F);
    }
    Vec3 below = this.strikePoint.subtract(0.0, DEPTH, 0.0);
    this.wyrm.flyToward(below, DIVE_SPEED);
    this.wyrm.contactDamage(level);
    if (this.wyrm.position().distanceToSqr(below) < 4.0 || this.ticks >= DIVE_TICKS) {
      this.stage = Stage.RIPPLE;
      this.ticks = 0;
      Player target = this.wyrm.pickTarget(level);
      if (target != null && target.position().distanceToSqr(this.strikePoint) < RETARGET_RANGE * RETARGET_RANGE) {
        this.strikePoint = new Vec3(target.getX(), Math.floor(target.getY()), target.getZ());
      }
    }
  }

  private void ripple(ServerLevel level) {

    Vec3 point = this.strikePoint;
    this.wyrm.setDeltaMovement(Vec3.ZERO);
    double radius = GEYSER_RADIUS * this.ticks / RIPPLE_TICKS;
    BlockState ground = level.getBlockState(BlockPos.containing(point).below());
    for (int i = 0; i < 12; i++) {
      Vec3 ripple = SpookyMath.onRing(point, SpookyMath.ringAngle(this.ticks * 0.2, i, 12), radius);
      level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), ripple.x, ripple.y + 0.1, ripple.z, 1, 0.05, 0.0, 0.05, 0.0);
    }
    if (this.ticks % 10 == 0) {
      level.playSound(null, point.x, point.y, point.z, SoundEvents.WARDEN_DIG, SoundSource.HOSTILE, 2.0F, 0.6F + this.ticks / (float) RIPPLE_TICKS * 0.4F);
    }
    if (this.ticks >= RIPPLE_TICKS) {
      this.stage = Stage.GEYSER;
      this.ticks = 0;
      this.wyrm.snapTo(point.x, point.y - DEPTH * 0.5, point.z, this.wyrm.getYRot(), this.wyrm.getXRot());
      this.erupt(level, point);
    }
  }

  private void geyser(ServerLevel level) {

    this.wyrm.setDeltaMovement(0.0, GEYSER_SPEED, 0.0);
    this.wyrm.contactDamage(level);
    if (this.wyrm.getY() >= this.wyrm.getAnchor().getY() + Gourdwyrm.CIRCLE_HEIGHT) {
      this.wyrm.setPhase(Gourdwyrm.Phase.CIRCLING);
    }
  }

  private void erupt(ServerLevel level, Vec3 point) {

    level.playSound(null, point.x, point.y, point.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 3.0F, 0.7F);
    level.sendParticles(ParticleTypes.LAVA, point.x, point.y + 0.5, point.z, 40, GEYSER_RADIUS * 0.5, 0.5, GEYSER_RADIUS * 0.5, 0.2);
    level.sendParticles(ParticleTypes.FLAME, point.x, point.y + 1.0, point.z, 80, GEYSER_RADIUS * 0.5, 2.0, GEYSER_RADIUS * 0.5, 0.1);
    level.sendParticles(ParticleTypes.EXPLOSION, point.x, point.y + 1.0, point.z, 3, 1.0, 0.5, 1.0, 0.0);

    AABB area = new AABB(point, point).inflate(GEYSER_RADIUS, 2.0, GEYSER_RADIUS).expandTowards(0.0, 4.0, 0.0);
    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, area, entity -> entity != this.wyrm && entity.isAlive())) {
      victim.hurtServer(level, this.wyrm.damageSources().mobAttack(this.wyrm), GEYSER_DAMAGE);
      victim.setDeltaMovement(victim.getDeltaMovement().add(0.0, 1.2, 0.0));
      victim.hurtMarked = true;
    }
  }

  private enum Stage {
    DIVE,
    RIPPLE,
    GEYSER
  }
}
