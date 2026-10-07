package io.github.jason13official.spookiness.entity.boss.gourdwyrm;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.phys.Vec3;

final class CirclingPhase extends GourdwyrmPhase {

  private static final double RADIUS = 28.0;
  private static final double BOB = 4.0;
  private static final double SPEED = 0.012;
  private static final int CIRCLE_TICKS = 240;
  private static final int SHED_CIRCLE_TICKS = 160;
  private static final int EMBER_INTERVAL = 50;

  private double angle;
  private boolean burrowNext = true;

  CirclingPhase(Gourdwyrm wyrm) {
    super(wyrm);
  }

  @Override
  protected void tick(ServerLevel level) {

    this.angle += SPEED * (this.wyrm.isShed() ? Gourdwyrm.SHED_SPEED_MULTIPLIER : 1.0);
    Vec3 center = Vec3.atBottomCenterOf(this.wyrm.getAnchor());
    Vec3 wanted = center.add(Math.cos(this.angle) * RADIUS, Gourdwyrm.CIRCLE_HEIGHT + Math.sin(this.angle * 3.0) * BOB, Math.sin(this.angle) * RADIUS);
    this.wyrm.flyToward(wanted, this.wyrm.speed());

    if (this.ticks % EMBER_INTERVAL == 0) {
      Player target = this.wyrm.pickTarget(level);
      if (target != null) {
        this.spitEmber(level, target);
      }
    }

    if (this.ticks >= (this.wyrm.isShed() ? SHED_CIRCLE_TICKS : CIRCLE_TICKS) && this.wyrm.pickTarget(level) != null) {
      boolean burrow = !this.wyrm.isShed() || this.burrowNext;
      this.burrowNext = !this.burrowNext;
      this.wyrm.setPhase(burrow ? Gourdwyrm.Phase.BURROW : Gourdwyrm.Phase.FINAL);
    }
  }

  private void spitEmber(ServerLevel level, Player target) {
    Vec3 mouth = this.wyrm.getEyePosition();
    Vec3 aim = target.getEyePosition().subtract(mouth).normalize();
    SmallFireball ember = new SmallFireball(level, this.wyrm, aim);
    ember.setPos(mouth.x, mouth.y, mouth.z);
    level.addFreshEntity(ember);
    this.wyrm.playSound(SoundEvents.BLAZE_SHOOT, 3.0F, 0.5F);
  }
}
