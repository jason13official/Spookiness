package io.github.jason13official.spookiness.lighting;

import io.github.jason13official.spookiness.entity.FloatingCandles;
import io.github.jason13official.spookiness.entity.JackOMimic;
import io.github.jason13official.spookiness.entity.SpectralJackOMimic;
import io.github.jason13official.spookiness.entity.boss.HallowedMother;
import io.github.jason13official.spookiness.entity.boss.Wickman;
import io.github.jason13official.spookiness.entity.boss.WickmanHead;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import io.github.jason13official.spookiness.item.PumpkinMaceItem;
import io.github.jason13official.spookiness.registry.ModItems;

public final class LanternHeads {

  public static final int EMISSION = 15;

  public static void tick(Entity entity) {

    if (!isManaged(entity)) {
      return;
    }

    LivingEntity living = (LivingEntity) entity;
    int wanted = Math.max(isLanternHeaded(living) ? EMISSION : 0,
        Math.max(PumpkinMaceItem.getLight(living.getMainHandItem()), PumpkinMaceItem.getLight(living.getOffhandItem())));
    int current = LivingLights.emissionOf(entity);
    if (wanted == current) {
      if (current > 0) {
        LivingLights.move(entity);
      }
      return;
    }
    LivingLights.remove(entity);
    LivingLights.add(entity, wanted);
  }

  public static void leave(Entity entity) {

    if (isManaged(entity)) {
      LivingLights.remove(entity);
    }
  }

  private static boolean isManaged(Entity entity) {
    return entity instanceof LivingEntity && !(entity instanceof JackOMimic) && !(entity instanceof SpectralJackOMimic)
        && !(entity instanceof FloatingCandles);
  }

  private static boolean isLanternHeaded(LivingEntity entity) {

    if (entity instanceof HallowedMother) {
      return true;
    }
    if (entity instanceof WickmanHead head) {
      return !head.isFrost();
    }
    if (entity instanceof Wickman wickman) {
      return wickman.getVariant() == Wickman.Variant.WICK && wickman.getPhase() != Wickman.Phase.HEADLESS;
    }
    return entity.getItemBySlot(EquipmentSlot.HEAD).is(Items.JACK_O_LANTERN) || entity.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.HARVEST_CROWN);
  }
}
