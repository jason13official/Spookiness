package io.github.jason13official.spookiness.entity.boss.gourdwyrm;

import io.github.jason13official.spookiness.lighting.LivingLights;
import net.minecraft.world.phys.Vec3;

final class GourdwyrmBody {

  static final float SEGMENT_SIZE = 2.0F;

  private static final int SPACING = 3;
  private static final int HISTORY = Gourdwyrm.SEGMENTS * SPACING + 1;
  private static final int SEGMENT_LIGHT = 15;

  private final Gourdwyrm wyrm;
  private final GourdwyrmPart[] segments = new GourdwyrmPart[Gourdwyrm.SEGMENTS];
  private final Vec3[] path = new Vec3[HISTORY];
  private int pathHead = -1;

  GourdwyrmBody(Gourdwyrm wyrm) {
    this.wyrm = wyrm;
    for (int i = 0; i < Gourdwyrm.SEGMENTS; i++) {
      this.segments[i] = new GourdwyrmPart(wyrm, i, SEGMENT_SIZE);
    }
  }

  GourdwyrmPart[] segments() {
    return this.segments;
  }

  void assignIds(int firstId) {
    for (int i = 0; i < this.segments.length; i++) {
      this.segments[i].setId(firstId + i);
    }
  }

  void tick() {
    this.recordPath();
    this.positionSegments();
    this.updateLights();
  }

  void refreshDimensions() {
    for (GourdwyrmPart segment : this.segments) {
      segment.refreshDimensions();
    }
  }

  void removeLights() {
    for (GourdwyrmPart segment : this.segments) {
      LivingLights.remove(segment);
    }
  }

  private void recordPath() {

    Vec3 pos = this.wyrm.position();
    if (this.pathHead < 0) {
      for (int i = 0; i < HISTORY; i++) {
        this.path[i] = pos;
      }
      this.pathHead = 0;
      return;
    }
    this.pathHead = (this.pathHead + 1) % HISTORY;
    this.path[this.pathHead] = pos;
  }

  private Vec3 pathSample(int ticksAgo) {
    return this.path[Math.floorMod(this.pathHead - ticksAgo, HISTORY)];
  }

  private void positionSegments() {

    float centerOffset = (this.wyrm.getBbHeight() - SEGMENT_SIZE) * 0.5F;
    for (int i = 0; i < this.segments.length; i++) {
      GourdwyrmPart segment = this.segments[i];
      Vec3 sample = this.pathSample((i + 1) * SPACING);
      segment.xo = segment.getX();
      segment.yo = segment.getY();
      segment.zo = segment.getZ();
      segment.xOld = segment.xo;
      segment.yOld = segment.yo;
      segment.zOld = segment.zo;
      segment.setPos(sample.x, sample.y + centerOffset, sample.z);
    }
  }

  private void updateLights() {
    for (GourdwyrmPart segment : this.segments) {
      boolean lit = this.wyrm.isAlive() && this.wyrm.isSegmentAlive(segment.index) && this.wyrm.isSegmentLit(segment.index);
      LivingLights.update(segment, lit ? SEGMENT_LIGHT : 0);
    }
  }
}
