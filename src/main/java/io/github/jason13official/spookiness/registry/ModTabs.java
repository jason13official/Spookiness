package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.Spookiness;
import java.util.function.BiConsumer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModTabs {

  public static CreativeModeTab SPOOKINESS;

  public static void register(BiConsumer<CreativeModeTab, Identifier> consumer) {

    SPOOKINESS = CreativeModeTab.builder()
        .icon(() -> new ItemStack(ModItems.LAMENT_CONFIGURATION))
        .title(Component.literal("Spookiness").withStyle(Style.EMPTY).withColor(0xFE7601))
        .displayItems((itemDisplayParameters, output) -> {

          ModItems.CREATIVE_TAB_ITEMS.forEach(output::accept);
        }).build();

    consumer.accept(SPOOKINESS, Spookiness.id(Spookiness.MOD_ID));
  }
}
