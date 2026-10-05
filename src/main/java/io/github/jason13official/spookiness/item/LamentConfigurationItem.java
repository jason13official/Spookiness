package io.github.jason13official.spookiness.item;

import io.github.jason13official.spookiness.effect.LamentRitual;
import io.github.jason13official.spookiness.world.netherrealm.NetherrealmArena;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class LamentConfigurationItem extends Item {

  public LamentConfigurationItem(Properties properties) {
    super(properties);
  }

  @Override
  public InteractionResult use(Level level, Player player, InteractionHand hand) {

    if (LamentRitual.isActive(player)) {
      return InteractionResult.FAIL;
    }
    if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
      if (NetherrealmArena.tryAwaken(serverLevel, serverPlayer, player.getItemInHand(hand))) {
        return InteractionResult.SUCCESS;
      }
      LamentRitual.start(serverPlayer, player.getItemInHand(hand));
    }
    return InteractionResult.SUCCESS;
  }
}
