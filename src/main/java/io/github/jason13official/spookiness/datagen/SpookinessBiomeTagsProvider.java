package io.github.jason13official.spookiness.datagen;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.worldgen.ModBiomeModifiers;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.neoforged.neoforge.common.Tags;

public class SpookinessBiomeTagsProvider extends BiomeTagsProvider {

  public SpookinessBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
    super(output, lookupProvider, Spookiness.MOD_ID);
  }

  @Override
  protected void addTags(HolderLookup.Provider registries) {

    this.tag(ModBiomeModifiers.HAS_PUMPKIN_PATCH).addTag(Tags.Biomes.IS_PLAINS).addTag(BiomeTags.IS_FOREST);
  }
}
