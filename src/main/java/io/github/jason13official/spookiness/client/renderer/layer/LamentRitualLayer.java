package io.github.jason13official.spookiness.client.renderer.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.client.model.LamentConfigurationModel;
import io.github.jason13official.spookiness.client.renderer.state.LamentConfigurationRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.context.ContextKey;

public class LamentRitualLayer<S extends LivingEntityRenderState, M extends EntityModel<? super S>> extends RenderLayer<S, M> {

  public static final ContextKey<Integer> RITUAL_TICKS = new ContextKey<>(Spookiness.id("lament_ritual_ticks"));

  private static final float INTRO_TICKS = 20.0F;
  private static final float START_DEPTH = 4.0F;
  private static final float HOLD_DEPTH = 0.8F;
  private static final float HOLD_HEIGHT = 0.1F;
  private static final float SIZE = 1.2F;
  private static final float INTRO_SPIN = 540.0F;
  private static final float TILT = 20.0F;
  private static final float MODEL_CENTER_Y = 1.375F;

  private final LamentConfigurationModel model;

  public LamentRitualLayer(RenderLayerParent<S, M> renderer, EntityModelSet models) {
    super(renderer);
    this.model = new LamentConfigurationModel(models.bakeLayer(LamentConfigurationModel.LAYER_LOCATION));
  }

  @Override
  public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, S state, float yRot, float xRot) {

    Integer ticks = state.getRenderData(RITUAL_TICKS);
    if (ticks == null || ticks <= 0) {
      return;
    }

    float time = ticks - 1 + Mth.frac(state.ageInTicks);
    float intro = Mth.clamp(time / INTRO_TICKS, 0.0F, 1.0F);
    float eased = 1.0F - (1.0F - intro) * (1.0F - intro) * (1.0F - intro);

    LamentConfigurationRenderState lament = new LamentConfigurationRenderState();
    lament.spinAnimationState.start(0);
    lament.ageInTicks = time;

    poseStack.pushPose();
    poseStack.translate(0.0F, HOLD_HEIGHT - 0.04F * Mth.sin(time * 0.15F), -Mth.lerp(eased, START_DEPTH, HOLD_DEPTH));
    poseStack.scale(SIZE, SIZE, SIZE);
    poseStack.mulPose(Axis.XP.rotationDegrees(-TILT));
    poseStack.mulPose(Axis.YP.rotationDegrees(INTRO_SPIN * (1.0F - eased)));
    poseStack.translate(0.0F, -MODEL_CENTER_Y, 0.0F);
    submitNodeCollector.submitModel(this.model, lament, poseStack, this.model.renderType(LamentConfigurationModel.TEXTURE), LightCoordsUtil.FULL_BRIGHT,
        OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
    poseStack.popPose();
  }
}
