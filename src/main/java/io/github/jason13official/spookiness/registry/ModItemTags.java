package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.Spookiness;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ModItemTags {

  public static final TagKey<Item> PUMPKIN_HEADS = TagKey.create(Registries.ITEM, Spookiness.id("pumpkin_heads"));
  public static final TagKey<Item> LIT_HEADS = TagKey.create(Registries.ITEM, Spookiness.id("lit_heads"));
}
