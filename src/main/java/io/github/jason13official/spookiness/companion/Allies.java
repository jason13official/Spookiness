package io.github.jason13official.spookiness.companion;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import org.jspecify.annotations.Nullable;

public final class Allies {

  private static final ThreadLocal<List<Entity>> CAPTURED = new ThreadLocal<>();

  public static @Nullable UUID ownerOf(Entity entity) {

    if (entity instanceof PlayerFollower follower) {
      return follower.getOwnerUUID();
    }
    return Hallowing.ownerOf(entity);
  }

  public static @Nullable Player owner(Entity entity) {

    UUID owner = ownerOf(entity);
    return owner == null ? null : entity.level().getPlayerByUUID(owner);
  }

  public static boolean isAlly(Entity entity, Entity other) {

    UUID owner = ownerOf(entity);
    return owner != null && (owner.equals(other.getUUID()) || owner.equals(ownerOf(other)));
  }

  public static void onChangeTarget(LivingChangeTargetEvent event) {

    LivingEntity target = event.getNewAboutToBeSetTarget();
    if (target != null && isAlly(event.getEntity(), target)) {
      event.setCanceled(true);
    }
  }

  public static <T> T allySpawned(Player player, Supplier<T> action) {

    List<Entity> previous = CAPTURED.get();
    List<Entity> spawned = new ArrayList<>();
    CAPTURED.set(spawned);
    try {
      return action.get();
    } finally {
      CAPTURED.set(previous);
      spawned.forEach(entity -> ally(entity, player));
    }
  }

  public static void onEntityJoin(Entity entity) {

    List<Entity> spawned = CAPTURED.get();
    if (spawned != null) {
      spawned.add(entity);
    }
  }

  public static boolean ally(Entity entity, Player player) {

    return entity.level() instanceof ServerLevel level && entity.isAlive() && entity instanceof PlayerFollower follower && follower.befriend(level, player);
  }
}
