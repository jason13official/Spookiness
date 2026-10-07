package io.github.jason13official.spookiness.datagen;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.registry.ModTimelines;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.tags.TimelineTags;
import net.minecraft.world.timeline.Timeline;

public class SpookinessTimelineTagsProvider extends KeyTagProvider<Timeline> {

  public SpookinessTimelineTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
    super(output, Registries.TIMELINE, lookupProvider, Spookiness.MOD_ID);
  }

  @Override
  protected void addTags(HolderLookup.Provider registries) {

    this.tag(TimelineTags.IN_OVERWORLD).add(ModTimelines.HAUNTED_HARVEST);
  }
}
