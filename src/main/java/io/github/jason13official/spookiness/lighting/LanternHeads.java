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

public final class LanternHeads {

  public static final int EMISSION = 15;

  public static void tick(Entity entity) {

    if (!isManaged(entity)) {
      return;
    }

    boolean glowing = isLanternHeaded((LivingEntity) entity);
    boolean lit = LivingLights.has(entity);
    if (glowing && !lit) {
      LivingLights.add(entity, EMISSION);
    } else if (!glowing && lit) {
      LivingLights.remove(entity);
    } else if (lit) {
      LivingLights.move(entity);
    }
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
    return entity.getItemBySlot(EquipmentSlot.HEAD).is(Items.JACK_O_LANTERN);
  }
}
