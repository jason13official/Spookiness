package io.github.jason13official.spookiness.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.client.model.JackOMimicModel;
import io.github.jason13official.spookiness.client.renderer.state.JackOGolemRenderState;
import io.github.jason13official.spookiness.entity.JackOMimic;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;

public class JackOMimicRenderer extends LivingEntityRenderer<JackOMimic, JackOGolemRenderState, JackOMimicModel<JackOGolemRenderState>> {

  private static final Identifier JACK_O_MIMIC_LOCATION = Spookiness.id("textures/entity/jack_o_mimic/jack_o_mimic.png");

  public JackOMimicRenderer(Context context) {
    // super(context, new JackOMimicModel<>(context.bakeLayer(JackOMimicModel.LAYER_LOCATION)), 1.0f);
    super(context, new JackOMimicModel<>(context.bakeLayer(JackOMimicModel.LAYER_LOCATION)), 0.0f);
  }

  @Override
  public Identifier getTextureLocation(JackOGolemRenderState livingEntityRenderState) {

    return JACK_O_MIMIC_LOCATION;
  }

  @Override
  public JackOGolemRenderState createRenderState() {

    return new JackOGolemRenderState();
  }

  @Override
  public void extractRenderState(JackOMimic entity, JackOGolemRenderState state, float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);

    state.yapAnimationState.copyFrom(entity.yapAnimationState);
  }

  @Override
  public void submit(JackOGolemRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {

    // why oh why oh why
    super.submit(state, poseStack, submitNodeCollector, camera);
  }
}
