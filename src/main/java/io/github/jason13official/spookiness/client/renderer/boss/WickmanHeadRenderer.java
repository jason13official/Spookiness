package io.github.jason13official.spookiness.client.renderer.boss;

import io.github.jason13official.spookiness.client.renderer.BlockPartsRenderer;
import io.github.jason13official.spookiness.client.renderer.state.BlockPartsRenderState;
import io.github.jason13official.spookiness.entity.boss.WickmanHead;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;

public class WickmanHeadRenderer extends BlockPartsRenderer<WickmanHead> {

  private static final float SCALE = 1.2F;
  private static final float WOBBLE = 8.0F;

  public WickmanHeadRenderer(Context context) {
    super(context, 0.4F);
  }

  @Override
  protected void collectParts(WickmanHead entity, BlockPartsRenderState state, float partialTicks) {

    float age = entity.tickCount + partialTicks;
    float yRot = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
    float bob = Mth.sin(age * 0.2F) * 0.08F;
    float wobble = Mth.sin(age * 0.35F) * WOBBLE;
    this.part(state, (entity.isFrost() ? Blocks.CARVED_PUMPKIN : Blocks.JACK_O_LANTERN).defaultBlockState(), 0.0, bob - 0.1, 0.0, SCALE, yRot + 180.0F, 0.0F,
        wobble);
  }
}
