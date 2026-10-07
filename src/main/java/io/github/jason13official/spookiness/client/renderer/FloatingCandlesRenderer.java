package io.github.jason13official.spookiness.client.renderer;

import io.github.jason13official.spookiness.client.renderer.state.BlockPartsRenderState;
import io.github.jason13official.spookiness.entity.FloatingCandles;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.util.Mth;

public class FloatingCandlesRenderer extends BlockPartsRenderer<FloatingCandles> {

  public FloatingCandlesRenderer(Context context) {
    super(context, 0.25F);
  }

  @Override
  protected void collectParts(FloatingCandles entity, BlockPartsRenderState state, float partialTicks) {

    float deathTime = entity.deathTime > 0 ? entity.deathTime + partialTicks : 0.0F;
    float fall = deathTime > 0.0F ? Math.min(Mth.sqrt((deathTime - 1.0F) / 20.0F * 1.6F), 1.0F) : 0.0F;
    this.part(state, entity.getCandleState(), 0.0, 0.0, 0.0, 1.0F, 0.0F, 0.0F, fall * 90.0F);
  }
}
