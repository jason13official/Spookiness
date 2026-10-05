package io.github.jason13official.spookiness.companion;

import io.github.jason13official.spookiness.effect.SoulBurst;
import io.github.jason13official.spookiness.entity.SpectralJackOMimic;
import io.github.jason13official.spookiness.registry.ModAttachments;
import io.github.jason13official.spookiness.registry.ModEntities;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public final class SpectralCompanions {

  private static final double SUMMON_RADIUS = 1.5;
  private static final double RECALL_DISTANCE = 32.0;

  private static final Set<SpectralJackOMimic> LOADED = ConcurrentHashMap.newKeySet();

  public static void track(SpectralJackOMimic mimic) {
    LOADED.add(mimic);
  }

  public static void untrack(SpectralJackOMimic mimic) {
    LOADED.remove(mimic);
  }

  public static void summon(ServerLevel level, Player owner, int count) {

    double startAngle = level.getRandom().nextDouble() * Math.PI * 2.0;
    for (int i = 0; i < count; i++) {
      SpectralJackOMimic mimic = ModEntities.SPECTRAL_JACK_O_MIMIC.create(level, EntitySpawnReason.MOB_SUMMONED);
      if (mimic == null) {
        continue;
      }

      Vec3 pos = ringPosition(owner.position(), startAngle, i, count);
      mimic.snapTo(pos.x, pos.y, pos.z, owner.getYRot(), 0.0F);
      mimic.setOwner(owner);
      level.addFreshEntity(mimic);
      SoulBurst.spawn(level, mimic.getBoundingBox().getCenter(), 32, 0.4, 0.08);
    }

    level.playSound(null, owner.getX(), owner.getY(), owner.getZ(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.5F, 0.8F);
    owner.sendOverlayMessage(Component.translatable(count == 1 ? "message.spookiness.companion_summoned" : "message.spookiness.companions_summoned", count));
  }

  public static void tick(MinecraftServer server) {

    for (SpectralJackOMimic mimic : List.copyOf(LOADED)) {
      UUID ownerId = mimic.getOwnerUUID();
      ServerPlayer owner = ownerId == null ? null : server.getPlayerList().getPlayer(ownerId);
      if (owner == null || !owner.isAlive() || owner.isSpectator() || mimic.isRemoved() || !mimic.isAlive()) {
        continue;
      }

      if (mimic.level() != owner.level() || mimic.distanceTo(owner) > RECALL_DISTANCE) {
        Vec3 pos = ringPosition(owner.position(), owner.getRandom().nextDouble() * Math.PI * 2.0, 0, 1);
        mimic.teleport(new TeleportTransition(owner.level(), pos, Vec3.ZERO, owner.getYRot(), 0.0F, TeleportTransition.DO_NOTHING));
      }
    }
  }

  public static void stash(ServerPlayer owner) {

    List<CompoundTag> stashed = new ArrayList<>(owner.getData(ModAttachments.STASHED_COMPANIONS));
    for (SpectralJackOMimic mimic : List.copyOf(LOADED)) {
      if (!owner.getUUID().equals(mimic.getOwnerUUID()) || mimic.isRemoved() || !mimic.isAlive()) {
        continue;
      }

      TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, mimic.registryAccess());
      if (mimic.save(output)) {
        stashed.add(output.buildResult());
      }
      mimic.discard();
    }

    owner.setData(ModAttachments.STASHED_COMPANIONS, List.copyOf(stashed));
  }

  public static void restore(ServerPlayer owner) {

    List<CompoundTag> stashed = owner.getData(ModAttachments.STASHED_COMPANIONS);
    if (stashed.isEmpty()) {
      return;
    }
    owner.removeData(ModAttachments.STASHED_COMPANIONS);

    ServerLevel level = owner.level();
    double startAngle = owner.getRandom().nextDouble() * Math.PI * 2.0;
    for (int i = 0; i < stashed.size(); i++) {
      Vec3 pos = ringPosition(owner.position(), startAngle, i, stashed.size());
      Entity entity = EntityType.loadEntityRecursive(stashed.get(i), level, EntitySpawnReason.LOAD, loaded -> {
        loaded.snapTo(pos.x, pos.y, pos.z, owner.getYRot(), 0.0F);
        return loaded;
      });
      if (entity != null) {
        level.addFreshEntity(entity);
      }
    }
  }

  private static Vec3 ringPosition(Vec3 center, double startAngle, int index, int count) {

    double angle = startAngle + Math.PI * 2.0 * index / count;
    return center.add(Math.cos(angle) * SUMMON_RADIUS, 1.0, Math.sin(angle) * SUMMON_RADIUS);
  }
}
