package io.github.jason13official.spookiness.entity.boss.mother;

import io.github.jason13official.spookiness.entity.JackOMimic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

final class MotherVolley {

  private static final int INTERVAL = 140;
  private static final int SIZE = 3;
  private static final int SPIT_GAP = 12;
  private static final double GOURDLING_RANGE = 32.0;
  private static final float SPREAD = 30.0F;

  private final HallowedMother mother;
  private int cooldown = 60;
  private int remaining;
  private int spitTimer;

  MotherVolley(HallowedMother mother) {
    this.mother = mother;
  }

  void cancel() {
    this.remaining = 0;
  }

  void tick(ServerLevel level, LivingEntity target) {

    if (this.remaining > 0) {
      if (--this.spitTimer <= 0) {
        this.mother.spit(level, target, SPREAD);
        this.remaining--;
        this.spitTimer = SPIT_GAP;
      }
      return;
    }
    if (--this.cooldown > 0) {
      return;
    }
    this.cooldown = INTERVAL;
    if (level.getEntitiesOfClass(JackOMimic.class, this.mother.getBoundingBox().inflate(GOURDLING_RANGE)).size() < HallowedMother.MAX_GOURDLINGS) {
      this.remaining = SIZE;
      this.spitTimer = 0;
    }
  }
}
