package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.item.LamentConfigurationItem;
import io.github.jason13official.spookiness.item.PumpkinMaceItem;
import java.util.function.BiConsumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Weapon;

public class ModItems {

  public static Item PUMPKIN_MACE;

  public static Item LAMENT_CONFIGURATION;

  public static void register(BiConsumer<Item, Identifier> consumer) {

    PUMPKIN_MACE = new PumpkinMaceItem(new Item.Properties().rarity(Rarity.EPIC).durability(500)
        .component(DataComponents.TOOL, PumpkinMaceItem.createToolProperties())
        .repairable(Items.STICK)
        .attributes(PumpkinMaceItem.createAttributes())
        .enchantable(15).component(DataComponents.WEAPON, new Weapon(1))
        .setId(ResourceKey.create(Registries.ITEM, Spookiness.id("pumpkin_mace"))));

    consumer.accept(PUMPKIN_MACE, Spookiness.id("pumpkin_mace"));

    LAMENT_CONFIGURATION = new LamentConfigurationItem(new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, Spookiness.id("lament_configuration"))));

    consumer.accept(LAMENT_CONFIGURATION, Spookiness.id("lament_configuration"));
  }
}
