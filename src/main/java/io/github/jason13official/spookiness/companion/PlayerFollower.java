package io.github.jason13official.spookiness.companion;

import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public interface PlayerFollower extends OwnableEntity {

  default @Nullable UUID getOwnerUUID() {
    EntityReference<LivingEntity> owner = this.getOwnerReference();
    return owner == null ? null : owner.getUUID();
  }

  boolean befriend(ServerLevel level, Player player);
}
