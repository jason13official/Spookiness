package io.github.jason13official.spookiness.entity.boss.gourdwyrm;

import net.minecraft.server.level.ServerLevel;

abstract class GourdwyrmPhase {

  protected final Gourdwyrm wyrm;
  protected int ticks;

  GourdwyrmPhase(Gourdwyrm wyrm) {
    this.wyrm = wyrm;
  }

  void begin() {
    this.ticks = 0;
  }

  final void serverTick(ServerLevel level) {
    this.ticks++;
    this.tick(level);
  }

  protected abstract void tick(ServerLevel level);
}
