package io.github.jason13official.spookiness.ritual;

import io.github.jason13official.spookiness.companion.Hallowing;
import io.github.jason13official.spookiness.entity.JackOMimic;
import io.github.jason13official.spookiness.entity.boss.Wickman;
import io.github.jason13official.spookiness.item.PumpkinMaceItem;
import io.github.jason13official.spookiness.registry.ModItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jspecify.annotations.Nullable;

public final class MaceRituals {

  private static final int COOLDOWN_TICKS = 40;

  public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {

    Player player = event.getEntity();
    ItemStack mace = event.getItemStack();
    if (!mace.is(ModItems.PUMPKIN_MACE) || !(event.getTarget() instanceof Mob mob)) {
      return;
    }
    Ritual ritual = ritualFor(mob);
    if (ritual == null) {
      return;
    }

    event.setCanceled(true);
    event.setCancellationResult(InteractionResult.SUCCESS);

    if (!(player instanceof ServerPlayer serverPlayer) || !(player.level() instanceof ServerLevel level) || player.getCooldowns().isOnCooldown(mace)) {
      return;
    }

    boolean performed = switch (ritual) {
      case HALLOW -> HallowRitual.perform(level, serverPlayer, mob, mace, event.getHand());
      case KINDLE -> Kindling.start(level, serverPlayer, mob, Wickman.Variant.WICK);
      case FROSTWICK -> Kindling.start(level, serverPlayer, mob, Wickman.Variant.FROST);
    };
    if (performed) {
      player.getCooldowns().addCooldown(mace, COOLDOWN_TICKS);
    }
  }

  private static @Nullable Ritual ritualFor(Mob mob) {

    if (Hallowing.isHallowed(mob) || Kindling.isKindling(mob) || !PumpkinMaceItem.isPumpkinEntity(mob)) {
      return null;
    }
    if (mob instanceof SnowGolem) {
      return Ritual.FROSTWICK;
    }
    ItemStack head = mob.getItemBySlot(EquipmentSlot.HEAD);
    if (mob instanceof JackOMimic || head.is(Items.JACK_O_LANTERN)) {
      return Ritual.KINDLE;
    }
    if (head.is(Items.CARVED_PUMPKIN)) {
      return Ritual.HALLOW;
    }
    return null;
  }

  private enum Ritual {
    HALLOW,
    KINDLE,
    FROSTWICK
  }
}
