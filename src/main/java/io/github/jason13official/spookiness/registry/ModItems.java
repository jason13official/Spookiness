package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.item.AlliedSpawnEggItem;
import io.github.jason13official.spookiness.item.HarvestCrownItem;
import io.github.jason13official.spookiness.item.LamentConfigurationItem;
import io.github.jason13official.spookiness.item.PumpkinMaceItem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
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
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.block.Block;

public class ModItems {

  public static Item PUMPKIN_MACE;

  public static Item LAMENT_CONFIGURATION;

  public static Item SOULLESS_JACK_O_MIMIC;

  public static Item PIECE_OF_LAMENT_ONE;

  public static Item PIECE_OF_LAMENT_TWO;

  public static Item HARVEST_CROWN;

  public static Item GOURDWYRM_TROPHY;

  public static Item LURKING_CARVED_PUMPKIN;

  public static Item LURKING_JACK_O_LANTERN;

  public static Item JACK_O_MIMIC_SEEDS;

  public static List<SpawnEgg> SPAWN_EGGS = new ArrayList<>();

  public static List<Item> CREATIVE_TAB_ITEMS = new ArrayList<>();

  public static void register(BiConsumer<Item, Identifier> consumer) {

    PUMPKIN_MACE = registerItem("pumpkin_mace", PumpkinMaceItem::new, new Item.Properties() // format
        .rarity(Rarity.EPIC).durability(500).repairable(Items.STICK) // format
        .component(DataComponents.TOOL, PumpkinMaceItem.createToolProperties()) // format
        .attributes(PumpkinMaceItem.createAttributes()).enchantable(15) // format
        .component(DataComponents.WEAPON, new Weapon(1)), consumer); // format

    PIECE_OF_LAMENT_ONE = registerItem("piece_of_lament_one", new Item.Properties().stacksTo(1), consumer);

    PIECE_OF_LAMENT_TWO = registerItem("piece_of_lament_two", new Item.Properties().stacksTo(1), consumer);

    LAMENT_CONFIGURATION = registerItem("lament_configuration", LamentConfigurationItem::new, new Item.Properties().stacksTo(1), consumer);

    SOULLESS_JACK_O_MIMIC = registerBlock(ModBlocks.SOULLESS_JACK_O_MIMIC, consumer);

    LURKING_CARVED_PUMPKIN = registerBlock(ModBlocks.LURKING_CARVED_PUMPKIN, consumer);

    LURKING_JACK_O_LANTERN = registerBlock(ModBlocks.LURKING_JACK_O_LANTERN, consumer);

    JACK_O_MIMIC_SEEDS = registerItem("jack_o_mimic_seeds", p -> new BlockItem(ModBlocks.JACK_O_MIMIC_STEM, p.useItemDescriptionPrefix()), new Item.Properties(), consumer);

    GOURDWYRM_TROPHY = registerBlock(ModBlocks.GOURDWYRM_TROPHY, new Item.Properties().rarity(Rarity.EPIC).fireResistant(), consumer);

    HARVEST_CROWN = registerItem("harvest_crown", HarvestCrownItem::new, new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()
        .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setEquipSound(SoundEvents.ARMOR_EQUIP_GOLD).build()), consumer);

    SPAWN_EGGS.clear();
    registerSpawnEgg(ModEntities.JACK_O_MIMIC, 0xE38A1D, 0x3B2508, consumer);
    registerSpawnEgg(ModEntities.FLOATING_CANDLES, 0xEFE3C2, 0xF5A623, consumer);
    registerSpawnEgg(ModEntities.FLOATING_BOOK, 0x6E4325, 0xE5D9AE, consumer);
    registerSpawnEgg(ModEntities.FLOATING_SWORD, 0xB8BEC8, 0x5E3FAE, consumer);
    registerSpawnEgg(ModEntities.FLOATING_SHEARS, 0xD8D8D8, 0x8E4A2E, consumer);
    registerSpawnEgg(ModEntities.FLOATING_HOE, 0x7A5A35, 0x6B9A3A, consumer);
    registerSpawnEgg(ModEntities.FLOATING_LANTERN, 0x3B3F4A, 0x5FE3E0, consumer);
    registerSpawnEgg(ModEntities.FLOATING_SKULL, 0xC1C1C1, 0x5FE3E0, consumer);
    registerSpawnEgg(ModEntities.HAUNTED_ARMOR_STAND, 0x9C7B4E, 0x4A3A2A, consumer);
    registerSpawnEgg(ModEntities.SPECTRAL_JACK_O_MIMIC, 0x5FE3E0, 0x1A4E7A, consumer);
    registerSpawnEgg(ModEntities.WICKMAN, 0xC9A65A, 0xFF7A1A, consumer);
    registerSpawnEgg(ModEntities.HALLOWED_MOTHER, 0x8A5A2B, 0x4E7A2E, consumer);
    registerSpawnEgg(ModEntities.GOURDWYRM, 0xD9731E, 0x3B5A1E, consumer);
  }

  // region vanilla registration methods

  private static ResourceKey<Item> modItemId(String name) {

    return ResourceKey.create(Registries.ITEM, Spookiness.id(name));
  }

  private static ResourceKey<Item> blockIdToItemId(ResourceKey<Block> blockName) {

    return ResourceKey.create(Registries.ITEM, blockName.identifier());
  }

  private static void registerSpawnEgg(EntityType<?> type, int baseColor, int spotColor, BiConsumer<Item, Identifier> consumer) {

    Item egg = registerItem(ResourceKey.create(Registries.ITEM, EntityType.getKey(type).withSuffix("_spawn_egg")), AlliedSpawnEggItem::new, (new Item.Properties()).spawnEgg(type), consumer);
    SPAWN_EGGS.add(new SpawnEgg(egg, type, baseColor, spotColor));
  }

  private static Item registerBlock(Block block, BiConsumer<Item, Identifier> consumer) {

    return registerBlock(block, new Item.Properties(), consumer);
  }

  private static Item registerBlock(Block block, Item.Properties properties, BiConsumer<Item, Identifier> consumer) {

    return registerItem(blockIdToItemId(block.builtInRegistryHolder().key()), p -> new BlockItem(block, p),
        properties.useBlockDescriptionPrefix().requiredFeatures(block.requiredFeatures()), consumer);
  }

  /// adds to our creative mode tab
  private static Item registerItem(String name, Function<Item.Properties, Item> itemFactory, Item.Properties properties, BiConsumer<Item, Identifier> consumer) {

    return registerItem(modItemId(name), itemFactory, properties, consumer);
  }

  /// adds to our creative mode tab
  private static Item registerItem(String name, Item.Properties properties, BiConsumer<Item, Identifier> consumer) {

    return registerItem(modItemId(name), Item::new, properties, consumer);
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

  public record SpawnEgg(Item item, EntityType<?> type, int baseColor, int spotColor) {

  }
}
