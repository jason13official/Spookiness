package io.github.jason13official.spookiness.client.renderer.boss;

import io.github.jason13official.spookiness.client.renderer.BlockPartsRenderer;
import io.github.jason13official.spookiness.client.renderer.state.BlockPartsRenderState;
import io.github.jason13official.spookiness.entity.boss.Gourdwyrm;
import io.github.jason13official.spookiness.entity.boss.GourdwyrmPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;

public class GourdwyrmRenderer extends BlockPartsRenderer<Gourdwyrm> {

  private static final float HEAD_SCALE = 3.0F;
  private static final float SEGMENT_SCALE = 2.0F;
  private static final float LIT_SCALE = 2.6F;
  private static final float LIT_SPIN_SPEED = 12.0F;

  public GourdwyrmRenderer(Context context) {
    super(context, 0.0F);
  }

  @Override
  protected boolean affectedByCulling(Gourdwyrm entity) {
    return false;
  }

  @Override
  protected void collectParts(Gourdwyrm entity, BlockPartsRenderState state, float partialTicks) {

    double baseX = Mth.lerp(partialTicks, entity.xo, entity.getX());
    double baseY = Mth.lerp(partialTicks, entity.yo, entity.getY());
    double baseZ = Mth.lerp(partialTicks, entity.zo, entity.getZ());
    float yRot = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
    this.part(state, Blocks.JACK_O_LANTERN.defaultBlockState(), 0.0, 0.0, 0.0, HEAD_SCALE, yRot + 180.0F, 0.0F);

    double previousX = 0.0;
    double previousZ = 0.0;
    GourdwyrmPart[] segments = entity.getSegments();
    for (GourdwyrmPart segment : segments) {
      if (!entity.isSegmentAlive(segment.index)) {
        continue;
      }
      double x = Mth.lerp(partialTicks, segment.xo, segment.getX()) - baseX;
      double y = Mth.lerp(partialTicks, segment.yo, segment.getY()) - baseY;
      double z = Mth.lerp(partialTicks, segment.zo, segment.getZ()) - baseZ;
      float segmentYRot = (float) (Mth.atan2(previousZ - z, previousX - x) * Mth.RAD_TO_DEG) - 90.0F;
      if (entity.isSegmentLit(segment.index)) {
        float spin = state.ageInTicks * LIT_SPIN_SPEED + segment.index * 40.0F;
        float pulse = LIT_SCALE + Mth.sin(state.ageInTicks * 0.3F + segment.index) * 0.15F;
        this.part(state, Blocks.JACK_O_LANTERN.defaultBlockState(), x, y, z, pulse, spin, 0.0F);
      } else {
        this.part(state, Blocks.CARVED_PUMPKIN.defaultBlockState(), x, y, z, SEGMENT_SCALE, segmentYRot + 180.0F, 0.0F);
      }
      previousX = x;
      previousZ = z;
    }
  }
}
