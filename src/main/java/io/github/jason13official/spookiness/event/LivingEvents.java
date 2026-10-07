package io.github.jason13official.spookiness.event;

import io.github.jason13official.spookiness.entity.PumpkinHeads;
import io.github.jason13official.spookiness.companion.Allies;
import io.github.jason13official.spookiness.companion.Hallowing;
import io.github.jason13official.spookiness.entity.JackOMimic;
import io.github.jason13official.spookiness.entity.boss.mother.MotherBrood;
import io.github.jason13official.spookiness.entity.boss.Wickman;
import io.github.jason13official.spookiness.item.PumpkinMaceItem;
import io.github.jason13official.spookiness.registry.ModItems;
import io.github.jason13official.spookiness.world.SpookySpawns;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public final class LivingEvents {

  public static void register(IEventBus gameBus) {

    // LivingChangeTargetEvent
    gameBus.addListener(LivingEvents::onChangeTarget);

    // LivingIncomingDamageEvent
    gameBus.addListener(LivingEvents::onIncomingDamage);

    // LivingKnockBackEvent
    gameBus.addListener(MotherBrood::onKnockBack);

    // LivingFallEvent
    gameBus.addListener(MotherBrood::onFall);

    // MobEffectEvent.Applicable
    gameBus.addListener(MotherBrood::onEffectApplicable);

    // LivingDeathEvent
    gameBus.addListener(LivingEvents::onDeath);
  }

  private static void onChangeTarget(LivingChangeTargetEvent event) {

    Allies.onChangeTarget(event);
    MotherBrood.onChangeTarget(event);

    LivingEntity entity = event.getEntity();
    if (!(event.getNewAboutToBeSetTarget() instanceof Player player) || MotherBrood.isBrood(entity)) {
      return;
    }
    if (PumpkinHeads.isLanternHeaded(entity) && player.getMainHandItem().is(ModItems.PUMPKIN_MACE)
        || entity instanceof JackOMimic && player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.HARVEST_CROWN)) {
      event.setCanceled(true);
    }
  }

  private static void onIncomingDamage(LivingIncomingDamageEvent event) {

    Hallowing.onIncomingDamage(event);
    Wickman.onIncomingDamage(event);
  }

  private static void onDeath(LivingDeathEvent event) {

    LivingEntity entity = event.getEntity();
    SpookySpawns.onSkeletonDeath(entity);

    DamageSource source = event.getSource();
    ItemStack weapon = source.getWeaponItem();
    if (!(entity.level() instanceof ServerLevel level) || weapon == null || !weapon.is(ModItems.PUMPKIN_MACE)) {
      return;
    }
    if (entity instanceof JackOMimic mimic && source.is(DamageTypes.MACE_SMASH)) {
      mimic.spawnSoulBurst(level);
    }
    if (source.getEntity() instanceof Player player && PumpkinMaceItem.isPumpkinEntity(entity)) {
      PumpkinMaceItem.addPumpkinKill(level, player, weapon);
    }
  }
}
