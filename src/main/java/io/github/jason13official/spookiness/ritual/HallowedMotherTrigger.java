package io.github.jason13official.spookiness.ritual;

import net.minecraft.util.Mth;
import io.github.jason13official.spookiness.world.SpookyTime;
import io.github.jason13official.spookiness.util.SpookyMath;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.jason13official.spookiness.advancement.SpookyTrigger;
import io.github.jason13official.spookiness.companion.Hallowing;
import io.github.jason13official.spookiness.entity.boss.mother.HallowedMother;
import io.github.jason13official.spookiness.registry.ModAttachments;
import io.github.jason13official.spookiness.worldgen.ModBiomeModifiers;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public final class HallowedMotherTrigger {

  public static final int REQUIRED_ALLIES = 5;

  private static final int CHECK_INTERVAL = 100;
  private static final long ANSWER_TICKS = 1200L;
  private static final double MIN_DISTANCE = 20.0;
  private static final double MAX_DISTANCE = 30.0;
  private static final String ACCEPT_COMMAND = "/spookiness mother accept";
  private static final String DENY_COMMAND = "/spookiness mother deny";

  private static final Map<UUID, Long> PENDING = new ConcurrentHashMap<>();

  public static void tick(ServerPlayer player) {

    if (player.tickCount % CHECK_INTERVAL != 0 || player.isSpectator()) {
      return;
    }
    ServerLevel level = player.level();
    if (level.dimension() != Level.OVERWORLD) {
      return;
    }

    long night = SpookyTime.day(level);
    if (!SpookyTime.isNight(SpookyTime.timeOfDay(level)) || player.getData(ModAttachments.MOTHER_NIGHT) == night) {
      return;
    }

    BlockPos pos = player.blockPosition();
    if (!level.canSeeSky(pos) || !level.getBiome(pos).is(ModBiomeModifiers.HAS_PUMPKIN_PATCH) || Hallowing.count(player) < REQUIRED_ALLIES) {
      return;
    }

    player.setData(ModAttachments.MOTHER_NIGHT, night);
    PENDING.put(player.getUUID(), level.getGameTime() + ANSWER_TICKS);
    player.sendSystemMessage(prompt());
    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.WARDEN_NEARBY_CLOSEST, SoundSource.HOSTILE, 0.8F, 0.6F);
  }

  private static Component prompt() {

    MutableComponent accept = Component.translatable("message.spookiness.mother_accept").withStyle(style -> style.withColor(ChatFormatting.GREEN)
        .withClickEvent(new ClickEvent.RunCommand(ACCEPT_COMMAND)).withHoverEvent(new HoverEvent.ShowText(Component.translatable("message.spookiness.mother_accept_hover"))));
    MutableComponent deny = Component.translatable("message.spookiness.mother_deny").withStyle(style -> style.withColor(ChatFormatting.RED)
        .withClickEvent(new ClickEvent.RunCommand(DENY_COMMAND)).withHoverEvent(new HoverEvent.ShowText(Component.translatable("message.spookiness.mother_deny_hover"))));
    return Component.translatable("message.spookiness.mother_warning").withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC)
        .append(" ").append(accept).append(" ").append(deny);
  }

  public static void forget(ServerPlayer player) {

    PENDING.remove(player.getUUID());
  }

  public static void clear() {

    PENDING.clear();
  }

  private static boolean takePending(ServerPlayer player) {
    Long expiry = PENDING.remove(player.getUUID());
    return expiry != null && player.level().getGameTime() <= expiry;
  }

  public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {

    dispatcher.register(Commands.literal("spookiness").then(Commands.literal("mother")
        .then(Commands.literal("accept").executes(HallowedMotherTrigger::accept))
        .then(Commands.literal("deny").executes(HallowedMotherTrigger::deny))));
  }

  private static int accept(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

    ServerPlayer player = context.getSource().getPlayerOrException();
    if (!takePending(player) || player.level().dimension() != Level.OVERWORLD) {
      player.sendSystemMessage(Component.translatable("message.spookiness.mother_gone").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
      return 0;
    }

    ServerLevel level = player.level();
    double distance = MIN_DISTANCE + player.getRandom().nextDouble() * (MAX_DISTANCE - MIN_DISTANCE);
    Vec3 spot = SpookyMath.onRing(player.position(), SpookyMath.randomAngle(player.getRandom()), distance);
    int x = Mth.floor(spot.x);
    int z = Mth.floor(spot.z);
    int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    HallowedMother.erupt(level, player, new Vec3(x + 0.5, y, z + 0.5));
    SpookyTrigger.award(player, SpookyTrigger.MOTHER_RISES);
    return 1;
  }

  private static int deny(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

    ServerPlayer player = context.getSource().getPlayerOrException();
    if (takePending(player)) {
      player.sendSystemMessage(Component.translatable("message.spookiness.mother_denied").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
    }
    return 1;
  }
}
