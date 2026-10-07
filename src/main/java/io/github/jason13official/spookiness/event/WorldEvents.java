package io.github.jason13official.spookiness.event;

import io.github.jason13official.spookiness.companion.PlayerFollowers;
import io.github.jason13official.spookiness.entity.book.BookStairs;
import io.github.jason13official.spookiness.ritual.HallowedMotherTrigger;
import io.github.jason13official.spookiness.world.HauntedHarvest;
import io.github.jason13official.spookiness.world.TemporaryBlocks;
import io.github.jason13official.spookiness.world.SpookySpawns;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.block.CropGrowEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class WorldEvents {

  public static void register(IEventBus gameBus) {

    // ServerTickEvent.Post
    gameBus.addListener((ServerTickEvent.Post event) -> {
      PlayerFollowers.tick(event.getServer());
      TemporaryBlocks.tick(event.getServer());
    });

    // ServerStoppedEvent
    gameBus.addListener((ServerStoppedEvent event) -> {
      PlayerFollowers.clear();
      BookStairs.clear();
      HauntedHarvest.clear();
      HallowedMotherTrigger.clear();
    });

    // CropGrowEvent.Post
    gameBus.addListener((CropGrowEvent.Post event) -> {
      if (event.getLevel() instanceof ServerLevel level) {
        SpookySpawns.onPlantGrown(level, event.getPos(), event.getOriginalState(), event.getState());
      }
    });

    // BlockEvent.BlockToolModificationEvent
    gameBus.addListener(WorldEvents::onToolModification);
  }

  private static void onToolModification(BlockEvent.BlockToolModificationEvent event) {

    if (!event.isSimulated() && event.getItemAbility() == ItemAbilities.HOE_TILL && event.getPlayer() != null && event.getContext().getLevel() instanceof ServerLevel level) {
      SpookySpawns.onHoeTill(level, event.getPos(), event.getState(), event.getHeldItemStack());
    }
  }
}
