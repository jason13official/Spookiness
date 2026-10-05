package io.github.jason13official.spookiness.client.renderer;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.entity.FloatingSword;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.illager.IllagerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.entity.state.IllagerRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FloatingSwordRenderer extends MobRenderer<FloatingSword, IllagerRenderState, IllagerModel<IllagerRenderState>> {

  private static final Identifier FLOATING_SWORD_LOCATION = Spookiness.id("textures/entity/floating_sword/floating_sword.png");

  public FloatingSwordRenderer(Context context) {
    super(context, new IllagerModel<>(context.bakeLayer(ModelLayers.VINDICATOR)), 0.0F);
    this.addLayer(new ItemInHandLayer<>(this));
  }

  @Override
  public Identifier getTextureLocation(IllagerRenderState state) {

    return FLOATING_SWORD_LOCATION;
  }

  @Override
  public IllagerRenderState createRenderState() {

    return new IllagerRenderState();
  }

  @Override
  public void extractRenderState(FloatingSword entity, IllagerRenderState state, float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);

    ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, this.itemModelResolver, partialTicks);
    state.mainArm = entity.getMainArm();
    state.isAggressive = entity.isAggressive();
    state.armPose = state.isAggressive ? AbstractIllager.IllagerArmPose.ATTACKING : AbstractIllager.IllagerArmPose.NEUTRAL;
    state.attackAnim = entity.getAttackAnim(partialTicks);
    state.walkAnimationSpeed = 0.0F;
  }

  @Override
  protected @Nullable RenderType getRenderType(IllagerRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {

    return isBodyVisible ? RenderTypes.entityTranslucent(this.getTextureLocation(state)) : super.getRenderType(state, isBodyVisible, forceTransparent, appearGlowing);
  }

  @Override
  public Vec3 getRenderOffset(IllagerRenderState state) {

    return super.getRenderOffset(state).add(0.0, Mth.sin(state.ageInTicks * 0.08F) * 0.08, 0.0);
  }
}
