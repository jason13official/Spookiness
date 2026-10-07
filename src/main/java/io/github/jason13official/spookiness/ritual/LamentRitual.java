package io.github.jason13official.spookiness.ritual;

import io.github.jason13official.spookiness.util.Particles;
import io.github.jason13official.spookiness.advancement.SpookyTrigger;
import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.registry.ModAttachments;
import io.github.jason13official.spookiness.registry.ModDataComponents;
import io.github.jason13official.spookiness.registry.ModItems;
import net.minecraft.core.GlobalPos;
import org.jspecify.annotations.Nullable;
import io.github.jason13official.spookiness.world.NetherrealmArena;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

public final class LamentRitual {

  public static final int DURATION_TICKS = 80;

  private static final int COOLDOWN_TICKS = 100;
  private static final Identifier FREEZE_ID = Spookiness.id("lament_freeze");
  private static final List<Holder<Attribute>> FROZEN_ATTRIBUTES = List.of(Attributes.MOVEMENT_SPEED, Attributes.JUMP_STRENGTH, Attributes.GRAVITY);
  private static final double ITEM_REACH = 0.6;
  private static final double ITEM_DROP = 0.35;
  private static final double RING_RADIUS = 1.2;
  private static final double RING_HEIGHT = 2.2;
  private static final int RING_ARMS = 3;
  private static final double RING_SPIN = 0.45;
  private static final int SEARCH_RADIUS = 16;
  private static final int NETHER_ROOF_MARGIN = 6;
  private static final int FALLBACK_Y = 64;

  public static boolean isActive(Player player) {

    return player.getData(ModAttachments.LAMENT_RITUAL) > 0;
  }

  public static void start(ServerPlayer player, ItemStack stack) {

    if (isActive(player)) {
      return;
    }
    player.setData(ModAttachments.LAMENT_RITUAL, 1);
    if (player.level().dimension() != Level.NETHER) {
      stack.set(ModDataComponents.LAMENT_ORIGIN, GlobalPos.of(player.level().dimension(), player.blockPosition()));
    }
    setFrozen(player, true);
    player.setDeltaMovement(Vec3.ZERO);
    player.hurtMarked = true;
    player.getCooldowns().addCooldown(stack, DURATION_TICKS + COOLDOWN_TICKS);
    player.level().playSound(null, player.blockPosition(), SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 0.6F, 1.4F);
  }

  public static void tick(ServerPlayer player) {

    int ticks = player.getData(ModAttachments.LAMENT_RITUAL);
    if (ticks <= 0) {
      return;
    }
    if (!player.isAlive()) {
      stop(player);
      return;
    }

    emitFlames(player.level(), player, ticks);

    if (ticks >= DURATION_TICKS) {
      stop(player);
      travel(player);
    } else {
      player.setData(ModAttachments.LAMENT_RITUAL, ticks + 1);
    }
  }

  private static void stop(ServerPlayer player) {

    player.setData(ModAttachments.LAMENT_RITUAL, 0);
    setFrozen(player, false);
  }

  private static void setFrozen(ServerPlayer player, boolean frozen) {

    for (Holder<Attribute> attribute : FROZEN_ATTRIBUTES) {
      AttributeInstance instance = player.getAttribute(attribute);
      if (instance == null) {
        continue;
      }
      instance.removeModifier(FREEZE_ID);
      if (frozen) {
        instance.addTransientModifier(new AttributeModifier(FREEZE_ID, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
      }
    }
  }

  private static Vec3 itemPosition(ServerPlayer player) {

    return player.getEyePosition().add(player.getLookAngle().scale(ITEM_REACH)).subtract(0.0, ITEM_DROP, 0.0);
  }

  private static void emitFlames(ServerLevel level, ServerPlayer player, int ticks) {

    RandomSource random = player.getRandom();
    Vec3 item = itemPosition(player);

    for (int i = 0; i < 3; i++) {
      Vec3 dir = new Vec3(random.nextGaussian(), random.nextDouble() * 0.8 + 0.2, random.nextGaussian()).normalize();
      level.sendParticles(ParticleTypes.FLAME, item.x, item.y, item.z, 0, dir.x, dir.y, dir.z, 0.12 + random.nextDouble() * 0.1);
    }

    Particles.risingSpiral(level, ParticleTypes.FLAME, player.position(), RING_RADIUS, RING_HEIGHT, RING_ARMS, ticks, RING_SPIN);

    if (random.nextInt(4) == 0) {
      level.sendParticles(ParticleTypes.LAVA, item.x, item.y, item.z, 1, 0.1, 0.1, 0.1, 0.0);
    }
    if (ticks % 5 == 0) {
      level.sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 0.1, player.getZ(), 6, 0.6, 0.05, 0.6, 0.02);
    }
  }

  private static void burst(ServerLevel level, Vec3 pos) {

    level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y + 1.0, pos.z, 80, 0.6, 1.0, 0.6, 0.15);
    level.sendParticles(ParticleTypes.LAVA, pos.x, pos.y + 1.0, pos.z, 10, 0.5, 0.5, 0.5, 0.0);
    level.playSound(null, BlockPos.containing(pos), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.0F, 0.6F);
  }

  private static void travel(ServerPlayer player) {

    ServerLevel from = player.level();
    ItemStack homeward = from.dimension() == Level.NETHER ? findHomeward(player) : null;
    GlobalPos home = homeward == null ? null : homeward.get(ModDataComponents.LAMENT_ORIGIN);
    ResourceKey<Level> destinationKey = home != null ? home.dimension() : from.dimension() == Level.NETHER ? Level.OVERWORLD : Level.NETHER;
    ServerLevel destination = from.getServer().getLevel(destinationKey);
    if (destination == null) {
      return;
    }

    burst(from, player.position());

    Vec3 target;
    if (home != null) {
      BlockPos pos = home.pos();
      target = Vec3.atBottomCenterOf(isStandable(destination, pos) ? pos : findArrival(destination, pos));
      homeward.remove(ModDataComponents.LAMENT_ORIGIN);
    } else {
      double scale = from.dimensionType().coordinateScale() / destination.dimensionType().coordinateScale();
      BlockPos origin = destination.getWorldBorder().clampToBounds(player.getX() * scale, player.getY(), player.getZ() * scale);
      target = destinationKey == Level.NETHER ? NetherrealmArena.findArrival(destination, origin).orElse(null) : null;
      if (target == null) {
        target = Vec3.atBottomCenterOf(findArrival(destination, origin));
      }
    }

    player.teleport(new TeleportTransition(destination, target, Vec3.ZERO, player.getYRot(), player.getXRot(),
        TeleportTransition.PLAY_PORTAL_SOUND.then(TeleportTransition.PLACE_PORTAL_TICKET)));
    burst(destination, target);
    SpookyTrigger.award(player, SpookyTrigger.LAMENT_RITUAL);
  }

  private static @Nullable ItemStack findHomeward(ServerPlayer player) {

    for (ItemStack stack : new ItemStack[] {player.getMainHandItem(), player.getOffhandItem()}) {
      if (stack.is(ModItems.LAMENT_CONFIGURATION) && stack.has(ModDataComponents.LAMENT_ORIGIN)) {
        return stack;
      }
    }
    for (ItemStack stack : player.getInventory()) {
      if (stack.is(ModItems.LAMENT_CONFIGURATION) && stack.has(ModDataComponents.LAMENT_ORIGIN)) {
        return stack;
      }
    }
    return null;
  }

  private static BlockPos findArrival(ServerLevel level, BlockPos origin) {

    for (int radius = 0; radius <= SEARCH_RADIUS; radius++) {
      for (int dx = -radius; dx <= radius; dx++) {
        for (int dz = -radius; dz <= radius; dz++) {
          if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
            continue;
          }
          BlockPos found = findInColumn(level, origin.getX() + dx, origin.getZ() + dz);
          if (found != null) {
            return found;
          }
        }
      }
    }
    return buildPlatform(level, new BlockPos(origin.getX(), Mth.clamp(FALLBACK_Y, level.getMinY() + 1, level.getMaxY() - 3), origin.getZ()));
  }

  private static BlockPos findInColumn(ServerLevel level, int x, int z) {

    BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    if (!level.dimensionType().hasCeiling()) {
      int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
      pos.set(x, top, z);
      return isStandable(level, pos) ? pos.immutable() : null;
    }

    int top = level.getMinY() + level.dimensionType().logicalHeight() - NETHER_ROOF_MARGIN;
    for (int y = top; y > level.getMinY(); y--) {
      pos.set(x, y, z);
      if (isStandable(level, pos)) {
        return pos.immutable();
      }
    }
    return null;
  }

  private static boolean isStandable(ServerLevel level, BlockPos pos) {

    BlockPos below = pos.below();
    BlockState floor = level.getBlockState(below);
    if (!floor.isFaceSturdy(level, below, Direction.UP) || !floor.getFluidState().isEmpty() || floor.is(Blocks.MAGMA_BLOCK)) {
      return false;
    }
    return isOpen(level, pos) && isOpen(level, pos.above());
  }

  private static boolean isOpen(ServerLevel level, BlockPos pos) {

    BlockState state = level.getBlockState(pos);
    return state.getCollisionShape(level, pos).isEmpty() && state.getFluidState().isEmpty() && !state.is(Blocks.FIRE) && !state.is(Blocks.SOUL_FIRE);
  }

  private static BlockPos buildPlatform(ServerLevel level, BlockPos center) {

    for (int dx = -1; dx <= 1; dx++) {
      for (int dz = -1; dz <= 1; dz++) {
        level.setBlockAndUpdate(center.offset(dx, -1, dz), Blocks.OBSIDIAN.defaultBlockState());
        for (int dy = 0; dy <= 2; dy++) {
          level.setBlockAndUpdate(center.offset(dx, dy, dz), Blocks.AIR.defaultBlockState());
        }
      }
    }
    return center;
  }
}
