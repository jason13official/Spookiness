package io.github.jason13official.spookiness.client.renderer;

import io.github.jason13official.spookiness.client.renderer.state.BlockPartsRenderState;
import io.github.jason13official.spookiness.entity.FloatingLantern;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.util.Mth;

public class FloatingLanternRenderer extends BlockPartsRenderer<FloatingLantern> {

  private static final double PIVOT_HEIGHT = 0.9;

  public FloatingLanternRenderer(Context context) {
    super(context, 0.2F);
  }

  @Override
  protected void collectParts(FloatingLantern entity, BlockPartsRenderState state, float partialTicks) {

    float zSwing = Mth.sin(state.ageInTicks * 0.07F) * 8.0F;
    float xSwing = Mth.cos(state.ageInTicks * 0.05F) * 6.0F;
    this.part(state, entity.getLanternState(), 0.0, PIVOT_HEIGHT, 0.0, 1.0F, 0.0F, xSwing, zSwing, 1.0F);
  }
}
