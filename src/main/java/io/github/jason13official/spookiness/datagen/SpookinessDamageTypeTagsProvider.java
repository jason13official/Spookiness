package io.github.jason13official.spookiness.datagen;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.registry.ModDamageTypes;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.tags.DamageTypeTags;

public class SpookinessDamageTypeTagsProvider extends DamageTypeTagsProvider {

  public SpookinessDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
    super(output, lookupProvider, Spookiness.MOD_ID);
  }

  @Override
  protected void addTags(HolderLookup.Provider registries) {

    this.tag(DamageTypeTags.BYPASSES_ARMOR).add(ModDamageTypes.HALLOWING);
    this.tag(DamageTypeTags.BYPASSES_SHIELD).add(ModDamageTypes.HALLOWING);
    this.tag(DamageTypeTags.NO_KNOCKBACK).add(ModDamageTypes.HALLOWING);
  }
}
