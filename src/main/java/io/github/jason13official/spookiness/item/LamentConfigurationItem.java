package io.github.jason13official.spookiness.item;

import io.github.jason13official.spookiness.effect.LamentRitual;
import io.github.jason13official.spookiness.registry.ModDataComponents;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
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
    if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel) {
      LamentRitual.start(serverPlayer, player.getItemInHand(hand));
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {

    if (itemStack.has(ModDataComponents.LAMENT_ORIGIN)) {
      builder.accept(Component.translatable("item.spookiness.lament_configuration.return_home").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
  }
}
