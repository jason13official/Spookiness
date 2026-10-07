package io.github.jason13official.spookiness.lighting;

import io.github.jason13official.spookiness.item.PumpkinMaceItem;
import io.github.jason13official.spookiness.registry.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class LanternHeads {

  public static final int EMISSION = 15;

  public static int getEmission(LivingEntity entity) {

    ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
    int fromHead = head.is(Items.JACK_O_LANTERN) || head.is(ModItems.HARVEST_CROWN) ? EMISSION : 0;
    return Math.max(fromHead, Math.max(PumpkinMaceItem.getLight(entity.getMainHandItem()), PumpkinMaceItem.getLight(entity.getOffhandItem())));
  }
}
