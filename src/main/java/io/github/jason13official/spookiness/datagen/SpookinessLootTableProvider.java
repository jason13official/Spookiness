package io.github.jason13official.spookiness.datagen;

import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.registry.ModBlocks;
import io.github.jason13official.spookiness.registry.ModEntities;
import io.github.jason13official.spookiness.registry.ModItems;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.NbtPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class SpookinessLootTableProvider {

  public static LootTableProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {

    return new LootTableProvider(output, Set.of(), List.of(
        new LootTableProvider.SubProviderEntry(Blocks::new, LootContextParamSets.BLOCK),
        new LootTableProvider.SubProviderEntry(Entities::new, LootContextParamSets.ENTITY)
    ), registries);
  }

  private static class Blocks extends BlockLootSubProvider {

    Blocks(HolderLookup.Provider registries) {
      super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
      this.dropSelf(ModBlocks.SOULLESS_JACK_O_MIMIC);
      this.dropSelf(ModBlocks.GOURDWYRM_TROPHY);
      this.dropOther(ModBlocks.LURKING_CARVED_PUMPKIN, Items.CARVED_PUMPKIN);
      this.dropOther(ModBlocks.LURKING_JACK_O_LANTERN, Items.JACK_O_LANTERN);
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
      return List.of(ModBlocks.SOULLESS_JACK_O_MIMIC, ModBlocks.GOURDWYRM_TROPHY, ModBlocks.LURKING_CARVED_PUMPKIN, ModBlocks.LURKING_JACK_O_LANTERN);
    }
  }

  private static class Entities extends EntityLootSubProvider {

    private final Set<EntityType<?>> added = new HashSet<>();

    Entities(HolderLookup.Provider registries) {
      super(FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void add(EntityType<?> type, ResourceKey<LootTable> lootTable, LootTable.Builder builder) {
      super.add(type, lootTable, builder);
      this.added.add(type);
    }

    private static final float ANCIENT_DEBRIS_CHANCE = 0.25F;

    private static LootPool.Builder one(Item item) {
      return LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(item));
    }

    private static LootPool.Builder some(Item item, float min, float max) {
      return LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
          .add(LootItem.lootTableItem(item).apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max))));
    }

    private static LootItemCondition.Builder frost() {
      CompoundTag tag = new CompoundTag();
      tag.putInt("variant", 1);
      return LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, EntityPredicate.Builder.entity().nbt(new NbtPredicate(tag)));
    }

    @Override
    public void generate() {

      this.add(ModEntities.WICKMAN, LootTable.lootTable()
          .withPool(one(ModItems.PIECE_OF_LAMENT_ONE))
          .withPool(some(Items.BLAZE_ROD, 1.0F, 3.0F).when(InvertedLootItemCondition.invert(frost())))
          .withPool(some(Items.HONEYCOMB, 1.0F, 4.0F).when(InvertedLootItemCondition.invert(frost())))
          .withPool(some(Items.SNOWBALL, 4.0F, 12.0F).when(frost()))
          .withPool(some(Items.PACKED_ICE, 1.0F, 4.0F).when(frost())));

      this.add(ModEntities.HALLOWED_MOTHER, LootTable.lootTable()
          .withPool(one(ModItems.PIECE_OF_LAMENT_TWO))
          .withPool(some(Items.MAGMA_CREAM, 2.0F, 4.0F))
          .withPool(some(Items.PUMPKIN_PIE, 3.0F, 6.0F))
          .withPool(some(Items.PUMPKIN_SEEDS, 8.0F, 16.0F)));

      this.add(ModEntities.GOURDWYRM, LootTable.lootTable()
          .withPool(one(ModItems.HARVEST_CROWN))
          .withPool(one(ModItems.GOURDWYRM_TROPHY))
          .withPool(some(Items.JACK_O_LANTERN, 6.0F, 10.0F))
          .withPool(some(Items.GOLD_INGOT, 8.0F, 16.0F))
          .withPool(some(Items.GOLD_BLOCK, 1.0F, 3.0F))
          .withPool(one(Items.ANCIENT_DEBRIS).when(LootItemRandomChanceCondition.randomChance(ANCIENT_DEBRIS_CHANCE))));

      this.add(ModEntities.JACK_O_MIMIC, LootTable.lootTable()
          .withPool(some(Items.PUMPKIN_SEEDS, 0.0F, 3.0F))
          .withPool(some(Items.TORCH, 0.0F, 1.0F)));

      this.add(ModEntities.FLOATING_SKULL, LootTable.lootTable().withPool(some(Items.BONE, 0.0F, 2.0F)));

      this.knownTypes().filter(type -> type.getDefaultLootTable().isPresent() && !this.added.contains(type))
          .forEach(type -> this.add(type, LootTable.lootTable()));
    }

    private Stream<EntityType<?>> knownTypes() {
      return BuiltInRegistries.ENTITY_TYPE.stream().filter(type -> BuiltInRegistries.ENTITY_TYPE.getKey(type).getNamespace().equals(Spookiness.MOD_ID));
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
      return this.knownTypes();
    }
  }
}
