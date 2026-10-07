package io.github.jason13official.spookiness.entity.boss.gourdwyrm;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.Vec3;

final class StunnedPhase extends GourdwyrmPhase {

  private static final int DURATION = 120;
  private static final double REST_HEIGHT = 4.0;
  private static final double DRIFT_SPEED = Gourdwyrm.FLY_SPEED * 0.4;

  StunnedPhase(Gourdwyrm wyrm) {
    super(wyrm);
  }

  @Override
  void begin() {
    super.begin();
    this.wyrm.playSound(SoundEvents.ENDER_DRAGON_HURT, 4.0F, 0.5F);
    this.wyrm.playSound(SoundEvents.FIRE_EXTINGUISH, 4.0F, 0.5F);
  }

  @Override
  protected void tick(ServerLevel level) {

    Vec3 rest = Vec3.atBottomCenterOf(this.wyrm.getAnchor()).add(0.0, REST_HEIGHT, 0.0);
    this.wyrm.flyToward(rest, DRIFT_SPEED);
    if (this.ticks % 5 == 0) {
      level.sendParticles(ParticleTypes.LARGE_SMOKE, this.wyrm.getX(), this.wyrm.getY() + this.wyrm.getBbHeight(), this.wyrm.getZ(), 4, 0.6, 0.3, 0.6, 0.02);
    }
    if (this.ticks >= DURATION) {
      this.wyrm.relightLanterns();
      this.wyrm.playSound(SoundEvents.ENDER_DRAGON_GROWL, 4.0F, 0.7F);
      this.wyrm.setPhase(Gourdwyrm.Phase.CIRCLING);
    }
  }
}
