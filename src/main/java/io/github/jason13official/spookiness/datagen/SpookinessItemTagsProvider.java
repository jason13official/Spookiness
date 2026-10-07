package io.github.jason13official.spookiness.datagen;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.registry.ModItemTags;
import io.github.jason13official.spookiness.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

public class SpookinessItemTagsProvider extends ItemTagsProvider {

  public SpookinessItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
    super(output, lookupProvider, Spookiness.MOD_ID);
  }

  @Override
  protected void addTags(HolderLookup.Provider registries) {

    this.tag(ModItemTags.PUMPKIN_HEADS).add(Items.CARVED_PUMPKIN, Items.JACK_O_LANTERN);
    this.tag(ModItemTags.LIT_HEADS).add(Items.JACK_O_LANTERN, ModItems.HARVEST_CROWN);
  }
}
