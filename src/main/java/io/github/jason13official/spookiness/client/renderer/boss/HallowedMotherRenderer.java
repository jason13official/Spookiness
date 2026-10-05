package io.github.jason13official.spookiness.client.renderer.boss;

import io.github.jason13official.spookiness.client.renderer.BlockPartsRenderer;
import io.github.jason13official.spookiness.client.renderer.state.BlockPartsRenderState;
import io.github.jason13official.spookiness.entity.boss.HallowedMother;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;

public class HallowedMotherRenderer extends BlockPartsRenderer<HallowedMother> {

  private static final float SCALE = 4.0F;
  private static final float SINK = -1.0F;

  public HallowedMotherRenderer(Context context) {
    super(context, 2.5F);
  }

  @Override
  protected void collectParts(HallowedMother entity, BlockPartsRenderState state, float partialTicks) {

    float yRot = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
    float breathe = 1.0F + Mth.sin((entity.tickCount + partialTicks) * 0.05F) * 0.03F;
    this.part(state, (entity.isEnraged() ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN).defaultBlockState(), 0.0, SINK, 0.0, SCALE * breathe, yRot + 180.0F,
        0.0F);

    Entity tethered = entity.getTethered();
    if (tethered != null) {
      double dx = Mth.lerp(partialTicks, tethered.xo, tethered.getX()) - Mth.lerp(partialTicks, entity.xo, entity.getX());
      double dy = Mth.lerp(partialTicks, tethered.yo, tethered.getY()) - Mth.lerp(partialTicks, entity.yo, entity.getY());
      double dz = Mth.lerp(partialTicks, tethered.zo, tethered.getZ()) - Mth.lerp(partialTicks, entity.zo, entity.getZ());
      int links = Math.max(2, (int) Math.sqrt(dx * dx + dy * dy + dz * dz));
      for (int i = 1; i < links; i++) {
        double t = (double) i / links;
        this.part(state, Blocks.MOSS_BLOCK.defaultBlockState(), dx * t, 1.5 + (dy - 1.5) * t, dz * t, 0.3F, 0.0F, 0.0F);
      }
    }
  }
}
