package io.github.jason13official.spookiness.entity;

import io.github.jason13official.spookiness.registry.ModItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class PumpkinHeads {

  private static ItemStack head(LivingEntity entity) {
    return entity.getItemBySlot(EquipmentSlot.HEAD);
  }

  public static boolean isPumpkinHeaded(LivingEntity entity) {
    return entity instanceof JackOMimic || head(entity).is(ModItemTags.PUMPKIN_HEADS);
  }

  public static boolean isLit(LivingEntity entity) {
    return entity instanceof JackOMimic || head(entity).is(ModItemTags.LIT_HEADS);
  }

  public static boolean isLanternHeaded(LivingEntity entity) {
    return isPumpkinHeaded(entity) && isLit(entity);
  }

  public static boolean isCarvedHeaded(LivingEntity entity) {
    return isPumpkinHeaded(entity) && !isLit(entity);
  }
}
