package io.github.jason13official.spookiness.item;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class HarvestCrownItem extends Item {

  private static final int NIGHT_VISION_DURATION = 220;

  public HarvestCrownItem(Properties properties) {
    super(properties);
  }

  @Override
  public void inventoryTick(ItemStack itemStack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {

    if (slot == EquipmentSlot.HEAD && owner instanceof Player player) {
      player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, NIGHT_VISION_DURATION, 0, false, false, true));
    }
  }
}
