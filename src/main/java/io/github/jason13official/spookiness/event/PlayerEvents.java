package io.github.jason13official.spookiness.event;

import io.github.jason13official.spookiness.ritual.HallowedMotherTrigger;
import io.github.jason13official.spookiness.ritual.MaceRituals;
import io.github.jason13official.spookiness.companion.PlayerFollowers;
import io.github.jason13official.spookiness.entity.book.BookStairs;
import io.github.jason13official.spookiness.ritual.LamentRitual;
import io.github.jason13official.spookiness.world.HauntedHarvest;
import io.github.jason13official.spookiness.world.SpookySpawns;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.player.PlayerEnchantItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class PlayerEvents {

  public static void register(IEventBus gameBus) {

    // PlayerTickEvent.Post
    gameBus.addListener(PlayerEvents::onTick);

    // PlayerInteractEvent.EntityInteract
    gameBus.addListener(PlayerEvents::onEntityInteract);

    // PlayerEnchantItemEvent
    gameBus.addListener(PlayerEvents::onEnchantItem);

    // PlayerEvent.PlayerLoggedInEvent
    gameBus.addListener((PlayerEvent.PlayerLoggedInEvent event) -> {
      if (event.getEntity() instanceof ServerPlayer player) {
        PlayerFollowers.restore(player);
      }
    });

    // PlayerEvent.PlayerLoggedOutEvent
    gameBus.addListener((PlayerEvent.PlayerLoggedOutEvent event) -> {
      if (event.getEntity() instanceof ServerPlayer player) {
        PlayerFollowers.stash(player);
        BookStairs.forget(player);
        HallowedMotherTrigger.forget(player);
      }
    });
  }

  private static void onTick(PlayerTickEvent.Post event) {

    if (event.getEntity() instanceof ServerPlayer player) {
      SpookySpawns.tickCandleAwakening(player);
      SpookySpawns.tickBookshelfAwakening(player);
      SpookySpawns.tickNightAwakenings(player);
      SpookySpawns.tickHarvestTools(player);
      HauntedHarvest.tick(player);
      LamentRitual.tick(player);
      HallowedMotherTrigger.tick(player);
    }
  }

  private static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {

    MaceRituals.onEntityInteract(event);
    if (!event.isCanceled() && event.getLevel() instanceof ServerLevel level && event.getTarget() instanceof Sheep sheep) {
      SpookySpawns.onSheepSheared(level, sheep, event.getItemStack());
    }
  }

  private static void onEnchantItem(PlayerEnchantItemEvent event) {

    if (event.getEntity() instanceof ServerPlayer player && player.containerMenu instanceof EnchantmentMenu menu) {
      menu.access.execute((level, pos) -> SpookySpawns.awakenEnchantingTableBook((ServerLevel) level, pos, player));
    }
  }
}
