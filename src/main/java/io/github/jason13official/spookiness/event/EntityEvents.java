package io.github.jason13official.spookiness.event;

import io.github.jason13official.spookiness.ritual.Kindling;
import io.github.jason13official.spookiness.companion.Allies;
import io.github.jason13official.spookiness.companion.Hallowing;
import io.github.jason13official.spookiness.companion.PlayerFollowers;
import io.github.jason13official.spookiness.entity.boss.mother.MotherBrood;
import io.github.jason13official.spookiness.world.SpookySpawns;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class EntityEvents {

  public static void register(IEventBus gameBus) {

    // FinalizeSpawnEvent
    gameBus.addListener((FinalizeSpawnEvent event) -> SpookySpawns.equipPumpkinHead(event.getEntity(), event.getEntity().getRandom()));

    // EntityJoinLevelEvent
    gameBus.addListener(EntityEvents::onJoinLevel);

    // EntityTickEvent.Post
    gameBus.addListener(EntityEvents::onTick);

    // EntityLeaveLevelEvent
    gameBus.addListener(EntityEvents::onLeaveLevel);
  }

  private static void onJoinLevel(EntityJoinLevelEvent event) {

    if (event.getLevel().isClientSide()) {
      return;
    }
    if (!event.loadedFromDisk()) {
      Allies.onEntityJoin(event.getEntity());
    }
    if (event.getEntity() instanceof Mob mob) {
      if (Hallowing.isHallowed(mob)) {
        Hallowing.applyGoals(mob);
      }
      if (MotherBrood.isBrood(mob)) {
        MotherBrood.applyGoals(mob);
      }
    }
  }

  private static void onTick(EntityTickEvent.Post event) {

    Entity entity = event.getEntity();
    if (entity.level().isClientSide()) {
      Hallowing.clientTick(entity);
    } else {
      Kindling.tick(entity);
      PlayerFollowers.track(entity);
    }
  }

  private static void onLeaveLevel(EntityLeaveLevelEvent event) {

    if (!event.getLevel().isClientSide() && event.getEntity() instanceof Mob mob) {
      PlayerFollowers.untrack(mob);
    }
  }
}
