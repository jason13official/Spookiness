package io.github.jason13official.spookiness.client.fog;

import io.github.jason13official.spookiness.entity.boss.Wickman;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.FogType;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

public final class KindleFog {

  private static final double RANGE = 48.0;
  private static final float FAR_PLANE = 20.0F;
  private static final float FADE = 0.02F;
  private static final float[] WICK_COLOR = {0.55F, 0.25F, 0.05F};
  private static final float[] FROST_COLOR = {0.7F, 0.8F, 0.9F};

  private static float strength;
  private static float oStrength;
  private static boolean frost;

  public static void tick(ClientTickEvent.Post event) {

    oStrength = strength;
    LocalPlayer player = Minecraft.getInstance().player;
    if (player == null) {
      strength = 0.0F;
      return;
    }

    List<Wickman> wickmen = player.level().getEntitiesOfClass(Wickman.class, player.getBoundingBox().inflate(RANGE), Wickman::isAlive);
    if (!wickmen.isEmpty()) {
      frost = wickmen.getFirst().getVariant() == Wickman.Variant.FROST;
    }
    strength = Mth.approach(strength, wickmen.isEmpty() ? 0.0F : 1.0F, FADE);
  }

  private static float strength(float partialTicks) {
    return Mth.lerp(partialTicks, oStrength, strength);
  }

  public static void renderFog(ViewportEvent.RenderFog event) {

    float amount = strength((float) event.getPartialTick());
    if (amount <= 0.0F || event.getType() != FogType.ATMOSPHERIC) {
      return;
    }
    event.setFarPlaneDistance(Mth.lerp(amount, event.getFarPlaneDistance(), Math.min(FAR_PLANE, event.getFarPlaneDistance())));
    event.setNearPlaneDistance(Mth.lerp(amount, event.getNearPlaneDistance(), 0.0F));
  }

  public static void fogColor(ViewportEvent.ComputeFogColor event) {

    float amount = strength((float) event.getPartialTick()) * 0.7F;
    if (amount <= 0.0F) {
      return;
    }
    float[] tint = frost ? FROST_COLOR : WICK_COLOR;
    event.setRed(Mth.lerp(amount, event.getRed(), tint[0]));
    event.setGreen(Mth.lerp(amount, event.getGreen(), tint[1]));
    event.setBlue(Mth.lerp(amount, event.getBlue(), tint[2]));
  }
}
