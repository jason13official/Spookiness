package io.github.jason13official.spookiness.companion;

import io.github.jason13official.spookiness.effect.SoulBurst;
import io.github.jason13official.spookiness.entity.SpectralJackOMimic;
import io.github.jason13official.spookiness.registry.ModEntities;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class SpectralCompanions {

  public static void summon(ServerLevel level, Player owner, int count) {

    double startAngle = level.getRandom().nextDouble() * Math.PI * 2.0;
    for (int i = 0; i < count; i++) {
      SpectralJackOMimic mimic = ModEntities.SPECTRAL_JACK_O_MIMIC.create(level, EntitySpawnReason.MOB_SUMMONED);
      if (mimic == null) {
        continue;
      }

      Vec3 pos = PlayerFollowers.ringPosition(owner.position(), startAngle, i, count);
      mimic.snapTo(pos.x, pos.y, pos.z, owner.getYRot(), 0.0F);
      mimic.setOwner(owner);
      level.addFreshEntity(mimic);
      SoulBurst.spawn(level, mimic.getBoundingBox().getCenter(), 32, 0.4, 0.08);
    }

    level.playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.5F, 0.8F);
    owner.sendOverlayMessage(Component.translatable(count == 1 ? "message.spookiness.companion_summoned" : "message.spookiness.companions_summoned", count));
  }
}
