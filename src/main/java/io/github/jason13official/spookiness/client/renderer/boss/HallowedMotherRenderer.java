package io.github.jason13official.spookiness.client.renderer.boss;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.client.model.HallowedMotherModel;
import io.github.jason13official.spookiness.client.model.JackOMimicModel;
import io.github.jason13official.spookiness.client.renderer.state.HallowedMotherRenderState;
import io.github.jason13official.spookiness.entity.boss.HallowedMother;
import java.util.ArrayList;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public class HallowedMotherRenderer extends MobRenderer<HallowedMother, HallowedMotherRenderState, HallowedMotherModel> {

  private static final Identifier TEXTURE = Spookiness.id("textures/entity/jack_o_mimic/jack_o_mimic.png");
  private static final float SCALE = HallowedMother.SCALE;
  private static final int FULL_LIGHT = 15;

  public HallowedMotherRenderer(Context context) {
    super(context, new HallowedMotherModel(context.bakeLayer(JackOMimicModel.LAYER_LOCATION)), 2.5F);
  }

  @Override
  public HallowedMotherRenderState createRenderState() {
    return new HallowedMotherRenderState();
  }

  @Override
  public Identifier getTextureLocation(HallowedMotherRenderState state) {
    return TEXTURE;
  }

  @Override
  protected boolean shouldShowName(HallowedMother entity, double distanceToCameraSq) {
    return false;
  }

  @Override
  protected void scale(HallowedMotherRenderState state, PoseStack poseStack) {
    poseStack.scale(SCALE, SCALE, SCALE);
  }

  @Override
  public void extractRenderState(HallowedMother entity, HallowedMotherRenderState state, float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);
    state.spitAnimationState.copyFrom(entity.spitAnimationState);
    state.breathe = 1.0F + Mth.sin((entity.tickCount + partialTicks) * 0.06F) * 0.025F;

    Entity tethered = entity.getTethered();
    if (tethered == null) {
      return;
    }
    if (state.leashStates == null || state.leashStates.size() != 1) {
      state.leashStates = new ArrayList<>(1);
      state.leashStates.add(new EntityRenderState.LeashState());
    }
    EntityRenderState.LeashState leash = state.leashStates.getFirst();
    leash.offset = entity.mouthOffset(entity.getPreciseBodyRotation(partialTicks));
    leash.start = entity.getPosition(partialTicks).add(leash.offset);
    leash.end = tethered.getPosition(partialTicks).add(0.0, tethered.getBbHeight() * 0.6, 0.0);
    leash.startBlockLight = FULL_LIGHT;
    leash.endBlockLight = FULL_LIGHT;
    leash.startSkyLight = FULL_LIGHT;
    leash.endSkyLight = FULL_LIGHT;
    leash.slack = false;
  }

  @Override
  protected boolean affectedByCulling(HallowedMother entity) {
    return entity.getTethered() == null && super.affectedByCulling(entity);
  }
}
