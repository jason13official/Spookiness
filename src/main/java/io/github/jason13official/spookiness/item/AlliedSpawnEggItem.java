package io.github.jason13official.spookiness.item;

import io.github.jason13official.spookiness.companion.Allies;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class AlliedSpawnEggItem extends SpawnEggItem {

  public AlliedSpawnEggItem(Properties properties) {
    super(properties);
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {

    Player player = context.getPlayer();
    if (player == null) {
      return super.useOn(context);
    }
    return Allies.allySpawned(player, () -> super.useOn(context));
  }

  @Override
  public InteractionResult use(Level level, Player player, InteractionHand hand) {

    return Allies.allySpawned(player, () -> super.use(level, player, hand));
  }
}
