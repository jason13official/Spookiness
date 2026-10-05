package io.github.jason13official.spookiness.item;

import io.github.jason13official.spookiness.effect.LamentRitual;
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
    if (player instanceof ServerPlayer serverPlayer) {
      LamentRitual.start(serverPlayer, player.getItemInHand(hand));
    }
    return InteractionResult.SUCCESS;
  }
}
