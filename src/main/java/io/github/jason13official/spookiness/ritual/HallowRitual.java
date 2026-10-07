package io.github.jason13official.spookiness.ritual;

import io.github.jason13official.spookiness.advancement.SpookyTrigger;
import io.github.jason13official.spookiness.companion.Hallowing;
import io.github.jason13official.spookiness.effect.Particles;
import io.github.jason13official.spookiness.registry.ModDamageTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

public final class HallowRitual {

  public static final int MAX_ALLIES = 5;
  public static final int DURABILITY_COST = 15;
  public static final float HEALTH_COST = 2.0F;

  public static boolean perform(ServerLevel level, ServerPlayer player, Mob mob, ItemStack mace, InteractionHand hand) {

    if (player.getHealth() <= HEALTH_COST) {
      refuse(level, player, "message.spookiness.hallow_too_weak");
      return false;
    }
    if (Hallowing.count(player) >= MAX_ALLIES) {
      refuse(level, player, "message.spookiness.hallow_too_many");
      return false;
    }

    mace.hurtAndBreak(DURABILITY_COST, player, hand);
    player.hurtServer(level, level.damageSources().source(ModDamageTypes.HALLOWING), HEALTH_COST);
    Particles.soulBurst(level, player.getBoundingBox().getCenter(), 16, 0.3, 0.05);

    Hallowing.hallow(mob, player);

    Particles.soulBurst(level, mob.getBoundingBox().getCenter(), 32, 0.4, 0.08);
    level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.5F, 1.2F);
    player.sendOverlayMessage(Component.translatable("message.spookiness.hallowed", mob.getDisplayName()));
    SpookyTrigger.award(player, SpookyTrigger.HALLOW);
    return true;
  }

  private static void refuse(ServerLevel level, ServerPlayer player, String message) {

    player.sendOverlayMessage(Component.translatable(message));
    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.6F, 0.5F);
  }
}
