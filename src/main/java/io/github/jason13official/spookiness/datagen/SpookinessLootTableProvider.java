package io.github.jason13official.spookiness.datagen;

import io.github.jason13official.spookiness.registry.ModBlocks;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class SpookinessLootTableProvider {

  public static LootTableProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {

    return new LootTableProvider(output, Set.of(), List.of(new LootTableProvider.SubProviderEntry(Blocks::new, LootContextParamSets.BLOCK)), registries);
  }

  private static class Blocks extends BlockLootSubProvider {

    Blocks(HolderLookup.Provider registries) {
      super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
      this.dropSelf(ModBlocks.SOULLESS_JACK_O_MIMIC);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
      return List.of(ModBlocks.SOULLESS_JACK_O_MIMIC);
    }
  }
}
