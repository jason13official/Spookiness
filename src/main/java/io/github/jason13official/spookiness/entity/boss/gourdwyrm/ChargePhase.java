package io.github.jason13official.spookiness.entity.boss.gourdwyrm;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

final class ChargePhase extends GourdwyrmPhase {

  private static final int WINDUP_TICKS = 20;
  private static final int CHARGE_TICKS = 50;
  private static final double CHARGE_SPEED = 1.4;
  private static final double OVERSHOOT = 12.0;

  private @Nullable Vec3 strikePoint;

  ChargePhase(Gourdwyrm wyrm) {
    super(wyrm);
  }

  @Override
  void begin() {
    super.begin();
    this.strikePoint = null;
  }

  @Override
  protected void tick(ServerLevel level) {

    if (this.strikePoint == null) {
      this.windUp(level);
      return;
    }

    this.wyrm.flyToward(this.strikePoint, CHARGE_SPEED);
    this.wyrm.contactDamage(level);
    if (this.ticks >= CHARGE_TICKS || this.wyrm.position().distanceToSqr(this.strikePoint) < 4.0) {
      this.wyrm.setPhase(Gourdwyrm.Phase.CIRCLING);
    }
  }

  private void windUp(ServerLevel level) {

    Player target = this.wyrm.pickTarget(level);
    if (target == null) {
      this.wyrm.setPhase(Gourdwyrm.Phase.CIRCLING);
      return;
    }
    this.wyrm.setDeltaMovement(this.wyrm.getDeltaMovement().scale(0.8));
    this.wyrm.faceToward(target.position().subtract(this.wyrm.position()));
    if (this.ticks >= WINDUP_TICKS) {
      Vec3 aim = target.getEyePosition().subtract(this.wyrm.position()).normalize();
      this.strikePoint = target.getEyePosition().add(aim.scale(OVERSHOOT));
      this.ticks = 0;
      this.wyrm.playSound(SoundEvents.ENDER_DRAGON_GROWL, 4.0F, 1.2F);
      level.sendParticles(ParticleTypes.FLAME, this.wyrm.getX(), this.wyrm.getY() + 1.0, this.wyrm.getZ(), 60, 2.5, 2.5, 2.5, 0.2);
    }
  }
}
