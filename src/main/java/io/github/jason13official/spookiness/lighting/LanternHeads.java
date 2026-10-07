package io.github.jason13official.spookiness.lighting;

import io.github.jason13official.spookiness.entity.PumpkinHeads;
import io.github.jason13official.spookiness.item.PumpkinMaceItem;
import net.minecraft.world.entity.LivingEntity;

public final class LanternHeads {

  public static final int EMISSION = 15;

  public static int getEmission(LivingEntity entity) {

    int fromHead = PumpkinHeads.isLit(entity) ? EMISSION : 0;
    return Math.max(fromHead, Math.max(PumpkinMaceItem.getLight(entity.getMainHandItem()), PumpkinMaceItem.getLight(entity.getOffhandItem())));
  }
}
