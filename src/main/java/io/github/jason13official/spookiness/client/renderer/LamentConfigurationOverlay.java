package io.github.jason13official.spookiness.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.jason13official.spookiness.client.model.LamentConfigurationModel;
import io.github.jason13official.spookiness.client.renderer.state.LamentConfigurationRenderState;
import io.github.jason13official.spookiness.effect.LamentRitual;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import org.jspecify.annotations.Nullable;

public final class LamentConfigurationOverlay {

  private static final float INTRO_TICKS = 20.0F;
  private static final float START_DEPTH = -10.0F;
  private static final float HOLD_DEPTH = -1.2F;
  private static final float SIZE = 2.0F;
  private static final float INTRO_SPIN = 540.0F;
  private static final float TILT = 20.0F;
  private static final float MODEL_CENTER_Y = 1.375F;

  private static final LamentConfigurationRenderState STATE = new LamentConfigurationRenderState();
  private static @Nullable LamentConfigurationModel model;
  private static int activeTicks;

  public static void tick() {

    LocalPlayer player = Minecraft.getInstance().player;
    if (player == null || !LamentRitual.isActive(player)) {
      activeTicks = 0;
      STATE.spinAnimationState.stop();
      return;
    }
    if (activeTicks == 0) {
      model = new LamentConfigurationModel(Minecraft.getInstance().getEntityModels().bakeLayer(LamentConfigurationModel.LAYER_LOCATION));
      STATE.spinAnimationState.start(player.tickCount);
    }
    activeTicks++;
  }

  public static void render(RenderHandEvent event) {

    LocalPlayer player = Minecraft.getInstance().player;
    if (activeTicks == 0 || model == null || player == null) {
      return;
    }
    event.setCanceled(true);
    if (event.getHand() != InteractionHand.MAIN_HAND) {
      return;
    }

    float partialTick = event.getPartialTick();
    float time = activeTicks - 1 + partialTick;
    float intro = Mth.clamp(time / INTRO_TICKS, 0.0F, 1.0F);
    float eased = 1.0F - (1.0F - intro) * (1.0F - intro) * (1.0F - intro);

    STATE.ageInTicks = player.tickCount + partialTick;

    PoseStack poseStack = event.getPoseStack();
    poseStack.pushPose();
    poseStack.translate(0.0F, 0.04F * Mth.sin(time * 0.15F), Mth.lerp(eased, START_DEPTH, HOLD_DEPTH));
    poseStack.scale(SIZE, SIZE, SIZE);
    poseStack.mulPose(Axis.XP.rotationDegrees(TILT));
    poseStack.mulPose(Axis.YP.rotationDegrees(INTRO_SPIN * (1.0F - eased)));
    poseStack.translate(0.0F, MODEL_CENTER_Y, 0.0F);
    poseStack.scale(1.0F, -1.0F, -1.0F);
    event.getSubmitNodeCollector().submitModel(model, STATE, poseStack, model.renderType(LamentConfigurationModel.TEXTURE), LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1, null, 0, null);
    poseStack.popPose();
  }
}
