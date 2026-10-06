package io.github.jason13official.spookiness.client.renderer;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.client.model.FloatingToolModel;
import io.github.jason13official.spookiness.client.renderer.state.FloatingToolRenderState;
import io.github.jason13official.spookiness.entity.FloatingTool;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FloatingToolRenderer extends MobRenderer<FloatingTool, FloatingToolRenderState, FloatingToolModel> {

  private static final Identifier FLOATING_SWORD_LOCATION = Spookiness.id("textures/entity/floating_sword/floating_sword.png");
  private static final float HIDDEN_ALPHA = 0.12F;

  public FloatingToolRenderer(Context context) {
    super(context, new FloatingToolModel(context.bakeLayer(ModelLayers.VINDICATOR)), 0.0F);
    this.addLayer(new ItemInHandLayer<>(this));
  }

  @Override
  public Identifier getTextureLocation(FloatingToolRenderState state) {

    return FLOATING_SWORD_LOCATION;
  }

  @Override
  public FloatingToolRenderState createRenderState() {

    return new FloatingToolRenderState();
  }

  @Override
  public void extractRenderState(FloatingTool entity, FloatingToolRenderState state, float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);

    ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, this.itemModelResolver, partialTicks);
    state.mainArm = entity.getMainArm();
    state.isAggressive = entity.isAggressive();
    state.armPose = state.isAggressive ? AbstractIllager.IllagerArmPose.ATTACKING : AbstractIllager.IllagerArmPose.NEUTRAL;
    state.attackAnim = entity.getAttackAnim(partialTicks);
    state.walkAnimationSpeed = 0.0F;
    state.visibility = entity.getVisibility(partialTicks);
  }

  @Override
  protected int getModelTint(FloatingToolRenderState state) {

    return ARGB.white(Mth.lerp(state.visibility, HIDDEN_ALPHA, 1.0F));
  }

  @Override
  protected @Nullable RenderType getRenderType(FloatingToolRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {

    return isBodyVisible ? RenderTypes.entityTranslucent(this.getTextureLocation(state)) : super.getRenderType(state, isBodyVisible, forceTransparent, appearGlowing);
  }

  @Override
  public Vec3 getRenderOffset(FloatingToolRenderState state) {

    return super.getRenderOffset(state).add(0.0, Mth.sin(state.ageInTicks * 0.08F) * 0.08, 0.0);
  }
}
