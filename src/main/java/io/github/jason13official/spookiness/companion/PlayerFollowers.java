package io.github.jason13official.spookiness.companion;

import io.github.jason13official.spookiness.registry.ModAttachments;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public final class PlayerFollowers {

  private static final double RING_RADIUS = 1.5;
  private static final double RECALL_DISTANCE = 32.0;

  private static final Set<Mob> LOADED = ConcurrentHashMap.newKeySet();

  public static int count(ServerPlayer owner, Predicate<Mob> filter) {
    int count = 0;
    for (Mob follower : LOADED) {
      if (isFollowing(follower, owner) && filter.test(follower)) {
        count++;
      }
    }
    return count;
  }

  public static List<Mob> followers(ServerPlayer owner, Predicate<Mob> filter) {
    List<Mob> followers = new ArrayList<>();
    for (Mob follower : LOADED) {
      if (isFollowing(follower, owner) && filter.test(follower)) {
        followers.add(follower);
      }
    }
    return followers;
  }

  public static void track(Entity entity) {
    if (entity instanceof Mob follower && follower.isAddedToLevel() && !follower.level().isClientSide() && Allies.ownerOf(follower) != null) {
      LOADED.add(follower);
    }
  }

  public static void untrack(Mob follower) {
    LOADED.remove(follower);
  }

  public static void clear() {
    LOADED.clear();
  }

  public static Vec3 ringPosition(Vec3 center, double startAngle, int index, int count) {

    double angle = startAngle + Math.PI * 2.0 * index / count;
    return center.add(Math.cos(angle) * RING_RADIUS, 1.0, Math.sin(angle) * RING_RADIUS);
  }

  private static boolean isFollowing(Mob follower, ServerPlayer owner) {
    return owner.getUUID().equals(Allies.ownerOf(follower)) && follower.isAlive() && !follower.isRemoved();
  }

  public static void tick(MinecraftServer server) {

    for (Mob follower : List.copyOf(LOADED)) {
      UUID ownerId = Allies.ownerOf(follower);
      ServerPlayer owner = ownerId == null ? null : server.getPlayerList().getPlayer(ownerId);
      if (owner == null || !owner.isAlive() || owner.isSpectator() || !isFollowing(follower, owner)) {
        continue;
      }

      if (follower.level() != owner.level() || follower.distanceTo(owner) > RECALL_DISTANCE) {
        Vec3 pos = ringPosition(owner.position(), owner.getRandom().nextDouble() * Math.PI * 2.0, 0, 1);
        follower.teleport(new TeleportTransition(owner.level(), pos, Vec3.ZERO, owner.getYRot(), 0.0F, TeleportTransition.DO_NOTHING));
      }
    }
  }

  public static void stash(ServerPlayer owner) {

    List<CompoundTag> stashed = new ArrayList<>(owner.getData(ModAttachments.STASHED_COMPANIONS));
    for (Mob follower : List.copyOf(LOADED)) {
      if (!isFollowing(follower, owner)) {
        continue;
      }

      TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, follower.registryAccess());
      if (follower.save(output)) {
        stashed.add(output.buildResult());
      }
      follower.discard();
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
}
