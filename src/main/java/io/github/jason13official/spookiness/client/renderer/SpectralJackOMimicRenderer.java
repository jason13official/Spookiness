package io.github.jason13official.spookiness.client.renderer;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.client.model.JackOMimicModel;
import io.github.jason13official.spookiness.client.renderer.state.JackOMimicRenderState;
import io.github.jason13official.spookiness.entity.SpectralJackOMimic;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SpectralJackOMimicRenderer extends MobRenderer<SpectralJackOMimic, JackOMimicRenderState, JackOMimicModel<JackOMimicRenderState>> {

  private static final Identifier JACK_O_MIMIC_LOCATION = Spookiness.id("textures/entity/jack_o_mimic/jack_o_mimic.png");
  private static final int SPECTRAL_TINT = 0xA0B8D8FF;

  public SpectralJackOMimicRenderer(Context context) {
    super(context, new JackOMimicModel<>(context.bakeLayer(JackOMimicModel.LAYER_LOCATION)), 0.0F);
  }

  @Override
  public Identifier getTextureLocation(JackOMimicRenderState state) {

    return JACK_O_MIMIC_LOCATION;
  }

  @Override
  public JackOMimicRenderState createRenderState() {

    return new JackOMimicRenderState();
  }

  @Override
  public void extractRenderState(SpectralJackOMimic entity, JackOMimicRenderState state, float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);

    state.yapAnimationState.copyFrom(entity.yapAnimationState);
  }

  @Override
  protected int getModelTint(JackOMimicRenderState state) {

    return SPECTRAL_TINT;
  }

  @Override
  protected int getBlockLightLevel(SpectralJackOMimic entity, BlockPos blockPos) {

    return 15;
  }

  @Override
  protected @Nullable RenderType getRenderType(JackOMimicRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {

    return isBodyVisible ? RenderTypes.entityTranslucent(this.getTextureLocation(state)) : super.getRenderType(state, isBodyVisible, forceTransparent, appearGlowing);
  }

  @Override
  public Vec3 getRenderOffset(JackOMimicRenderState state) {

    return super.getRenderOffset(state).add(0.0, Mth.sin(state.ageInTicks * 0.1F) * 0.08, 0.0);
  }
}
