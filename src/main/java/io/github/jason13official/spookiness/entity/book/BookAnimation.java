package io.github.jason13official.spookiness.entity.book;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public final class BookAnimation {

  private static final float PHASE_ALPHA = 0.5F;
  private static final float PHASE_FADE_PER_TICK = 0.1F;

  private float open;
  private float oOpen;
  private float flip;
  private float oFlip;
  private float flipT;
  private float flipA;
  private float alpha = 1.0F;
  private float oAlpha = 1.0F;

  void tick(RandomSource random, boolean reading, boolean phasing) {

    this.oOpen = this.open;
    this.oFlip = this.flip;

    if (reading) {
      this.open += 0.1F;
      if (this.open < 0.5F || random.nextInt(40) == 0) {
        float old = this.flipT;
        do {
          this.flipT += random.nextInt(4) - random.nextInt(4);
        } while (old == this.flipT);
      }
    } else {
      this.open -= 0.1F;
    }

    this.open = Mth.clamp(this.open, 0.0F, 1.0F);
    float diff = Mth.clamp((this.flipT - this.flip) * 0.4F, -0.2F, 0.2F);
    this.flipA += (diff - this.flipA) * 0.9F;
    this.flip += this.flipA;

    this.oAlpha = this.alpha;
    this.alpha = Mth.approach(this.alpha, phasing ? PHASE_ALPHA : 1.0F, PHASE_FADE_PER_TICK);
  }

  public float open(float partialTicks) {
    return Mth.lerp(partialTicks, this.oOpen, this.open);
  }

  public float flip(float partialTicks) {
    return Mth.lerp(partialTicks, this.oFlip, this.flip);
  }

  public float alpha(float partialTicks) {
    return Mth.lerp(partialTicks, this.oAlpha, this.alpha);
  }
}
