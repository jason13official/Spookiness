package io.github.jason13official.spookiness.lighting;

import io.github.jason13official.spookiness.entity.JackOMimic;
import io.github.jason13official.spookiness.entity.SpectralJackOMimic;
import io.github.jason13official.spookiness.entity.boss.Wickman;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LevelAccessor;

public final class LanternHeads {

  public static final int EMISSION = 15;

  private static final Set<Entity> LIT = ConcurrentHashMap.newKeySet();

  public static void tick(Entity entity) {

    if (!(entity instanceof LivingEntity living) || entity instanceof JackOMimic || entity instanceof SpectralJackOMimic) {
      return;
    }

    boolean glowing = isLanternHeaded(living);
    boolean lit = LIT.contains(entity);
    if (glowing && !lit) {
      LivingLights.add(entity, EMISSION);
      LIT.add(entity);
    } else if (!glowing && lit) {
      LivingLights.remove(entity);
      LIT.remove(entity);
    } else if (lit) {
      LivingLights.move(entity);
    }
  }

  public static void leave(Entity entity) {

    if (LIT.remove(entity)) {
      LivingLights.remove(entity);
    }
  }

  public static void unload(LevelAccessor level) {

    LIT.removeIf(entity -> entity.level() == level);
  }

  private static boolean isLanternHeaded(LivingEntity entity) {

    if (entity instanceof Wickman wickman) {
      return wickman.getVariant() == Wickman.Variant.WICK && wickman.getPhase() != Wickman.Phase.HEADLESS;
    }
    return entity.getItemBySlot(EquipmentSlot.HEAD).is(Items.JACK_O_LANTERN);
  }
}
