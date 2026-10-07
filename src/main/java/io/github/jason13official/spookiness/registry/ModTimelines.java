package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.world.SpookyTime;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.KeyframeTrack;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.modifier.ColorModifier;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.timeline.Timeline;

public class ModTimelines {

  public static final ResourceKey<Timeline> HAUNTED_HARVEST = ResourceKey.create(Registries.TIMELINE, Spookiness.id("haunted_harvest"));

  private static final int CLEAR = 0x00000000;
  private static final int HARVEST_FOG = 0x99C8641E;
  private static final int HARVEST_SKY = 0x80401A08;
  private static final int HARVEST_BLOCK_LIGHT = 0x66FF9A30;
  private static final int FADE_IN_START = (int) SpookyTime.NIGHT_START - 500;
  private static final int FADE_IN_END = (int) SpookyTime.NIGHT_START + 1500;
  private static final int FADE_OUT_START = (int) SpookyTime.NIGHT_END - 1000;
  private static final int FADE_OUT_END = (int) SpookyTime.NIGHT_END + 800;

  public static void bootstrap(BootstrapContext<Timeline> context) {

    Holder<WorldClock> overworldClock = context.lookup(Registries.WORLD_CLOCK).getOrThrow(WorldClocks.OVERWORLD);
    context.register(HAUNTED_HARVEST, Timeline.builder(overworldClock)
        .setPeriodTicks((int) SpookyTime.HARVEST_CYCLE_TICKS)
        .addModifierTrack(EnvironmentAttributes.FOG_COLOR, ColorModifier.ALPHA_BLEND, track -> tint(track, HARVEST_FOG))
        .addModifierTrack(EnvironmentAttributes.SKY_COLOR, ColorModifier.ALPHA_BLEND, track -> tint(track, HARVEST_SKY))
        .addModifierTrack(EnvironmentAttributes.BLOCK_LIGHT_TINT, ColorModifier.ALPHA_BLEND, track -> tint(track, HARVEST_BLOCK_LIGHT))
        .build());
  }

  private static void tint(KeyframeTrack.Builder<Integer> track, int color) {

    track.addKeyframe(0, CLEAR).addKeyframe(FADE_IN_START, CLEAR).addKeyframe(FADE_IN_END, color).addKeyframe(FADE_OUT_START, color).addKeyframe(FADE_OUT_END, CLEAR);
  }
}
