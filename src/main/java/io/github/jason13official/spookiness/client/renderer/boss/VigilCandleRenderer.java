package io.github.jason13official.spookiness.client.renderer.boss;

import io.github.jason13official.spookiness.client.renderer.BlockPartsRenderer;
import io.github.jason13official.spookiness.client.renderer.state.BlockPartsRenderState;
import io.github.jason13official.spookiness.entity.boss.VigilCandle;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;

public class VigilCandleRenderer extends BlockPartsRenderer<VigilCandle> {

  public VigilCandleRenderer(Context context) {
    super(context, 0.2F);
  }

  @Override
  protected void collectParts(VigilCandle entity, BlockPartsRenderState state, float partialTicks) {

    BlockState candle = (entity.isFrost() ? Blocks.LIGHT_BLUE_CANDLE : Blocks.ORANGE_CANDLE).defaultBlockState()
        .setValue(CandleBlock.CANDLES, entity.getCandles()).setValue(CandleBlock.LIT, true);
    float scale = entity.getRenderScale();
    this.part(state, Blocks.HONEYCOMB_BLOCK.defaultBlockState(), 0.0, 0.0, 0.0, scale * VigilCandle.BASE_HEIGHT, 0.0F, 0.0F);
    this.part(state, candle, 0.0, scale * VigilCandle.BASE_HEIGHT, 0.0, scale, 0.0F, 0.0F);
  }
}
