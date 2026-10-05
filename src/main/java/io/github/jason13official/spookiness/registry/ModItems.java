package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.item.LamentConfigurationItem;
import io.github.jason13official.spookiness.item.PumpkinMaceItem;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.block.Block;

public class ModItems {

  public static Item PUMPKIN_MACE;

  public static Item LAMENT_CONFIGURATION;

  public static List<Item> CREATIVE_TAB_ITEMS = new LinkedList<>();

  public static void register(BiConsumer<Item, Identifier> consumer) {

    CREATIVE_TAB_ITEMS.clear(); // just in case ?

    PUMPKIN_MACE = registerItem("pumpkin_mace", PumpkinMaceItem::new, new Item.Properties() // format
        .rarity(Rarity.EPIC).durability(500).repairable(Items.STICK) // format
        .component(DataComponents.TOOL, PumpkinMaceItem.createToolProperties()) // format
        .attributes(PumpkinMaceItem.createAttributes()).enchantable(15) // format
        .component(DataComponents.WEAPON, new Weapon(1)), consumer); // format

    LAMENT_CONFIGURATION = registerItem("lament_configuration", LamentConfigurationItem::new, new Item.Properties().stacksTo(1), consumer);
  }

  // region vanilla registration methods

  private static ResourceKey<Item> modItemId(String name) {

    return ResourceKey.create(Registries.ITEM, Spookiness.id(name));
  }

  private static ResourceKey<Item> blockIdToItemId(ResourceKey<Block> blockName) {

    return ResourceKey.create(Registries.ITEM, blockName.identifier());
  }

  private static Item registerSpawnEgg(EntityType<?> type, BiConsumer<Item, Identifier> consumer) {

    return registerItem(ResourceKey.create(Registries.ITEM, EntityType.getKey(type).withSuffix("_spawn_egg")), SpawnEggItem::new, (new Item.Properties()).spawnEgg(type), consumer);
  }

  private static Item registerBlock(Block block, BiConsumer<Item, Identifier> consumer) {

    return registerBlock(block, BlockItem::new, consumer);
  }

  private static Item registerBlock(Block block, Item.Properties properties, BiConsumer<Item, Identifier> consumer) {

    return registerBlock(block, BlockItem::new, properties, consumer);
  }

  private static Item registerBlock(Block block, UnaryOperator<Properties> propertiesFunction, BiConsumer<Item, Identifier> consumer) {

    return registerBlock(block, (b, p) -> new BlockItem(b, propertiesFunction.apply(p)), consumer);
  }

  /// what does it do ?
  @SuppressWarnings("deprecation")
  private static Item registerBlock(Block block, Block[] alternatives, BiConsumer<Item, Identifier> consumer) {

    return registerItem(blockIdToItemId(block.builtInRegistryHolder().key()), (p) -> new BlockItem(block, p) {

      public void registerBlocks(Map<Block, Item> map, Item self) {
        super.registerBlocks(map, self);

        for (Block block : alternatives) {
          map.put(block, self);
        }

      }
    }, (new Item.Properties()).useBlockDescriptionPrefix(), consumer);
  }

  private static Item registerBlock(Block block, BiFunction<Block, Item.Properties, Item> itemFactory, BiConsumer<Item, Identifier> consumer) {

    return registerBlock(block, itemFactory, new Item.Properties(), consumer);
  }

  private static Item registerBlock(Block block, BiFunction<Block, Item.Properties, Item> itemFactory, Item.Properties properties, BiConsumer<Item, Identifier> consumer) {

    return registerItem(blockIdToItemId(block.builtInRegistryHolder().key()), (p) -> itemFactory.apply(block, p), properties.useBlockDescriptionPrefix().requiredFeatures(block.requiredFeatures()),
        consumer);
  }

  private static Item registerItem(String name, Function<Properties, Item> itemFactory, BiConsumer<Item, Identifier> consumer) {

    return registerItem(modItemId(name), itemFactory, new Item.Properties(), consumer);
  }

  /// adds to our creative mode tab
  private static Item registerItem(String name, Function<Item.Properties, Item> itemFactory, Item.Properties properties, BiConsumer<Item, Identifier> consumer) {

    return registerItem(modItemId(name), itemFactory, properties, consumer);
  }

  /// adds to our creative mode tab
  private static Item registerItem(String name, Item.Properties properties, BiConsumer<Item, Identifier> consumer) {

    return registerItem(modItemId(name), Item::new, properties, consumer);
  }

  private static Item registerItem(String name, BiConsumer<Item, Identifier> consumer) {

    return registerItem(modItemId(name), Item::new, new Item.Properties(), consumer);
  }

  private static Item registerItem(ResourceKey<Item> key, Function<Item.Properties, Item> itemFactory, BiConsumer<Item, Identifier> consumer) {

    return registerItem(key, itemFactory, new Item.Properties(), consumer);
  }

  private static Item registerItem(ResourceKey<Item> key, Function<Item.Properties, Item> itemFactory, Item.Properties properties, BiConsumer<Item, Identifier> consumer) {

    Item item = itemFactory.apply(properties.setId(key));

    if (item instanceof BlockItem blockItem) {
      blockItem.registerBlocks(Item.BY_BLOCK, item);
    }

    consumer.accept(item, key.identifier());
    CREATIVE_TAB_ITEMS.add(item);

    return item;
  }

  // endregion vanilla registration methods
}
