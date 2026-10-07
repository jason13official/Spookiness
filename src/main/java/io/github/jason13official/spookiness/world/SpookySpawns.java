package io.github.jason13official.spookiness.world;

import io.github.jason13official.spookiness.util.SpookyMath;
import io.github.jason13official.spookiness.util.Spawning;
import net.minecraft.world.level.levelgen.Heightmap;
import io.github.jason13official.spookiness.effect.Particles;
import io.github.jason13official.spookiness.entity.book.FloatingBook;
import io.github.jason13official.spookiness.entity.FloatingCandles;
import io.github.jason13official.spookiness.entity.FloatingLantern;
import io.github.jason13official.spookiness.entity.FloatingSkull;
import io.github.jason13official.spookiness.entity.FloatingTool;
import io.github.jason13official.spookiness.entity.HauntedArmorStand;
import io.github.jason13official.spookiness.registry.ModAttachments;
import io.github.jason13official.spookiness.registry.ModEntities;
import io.github.jason13official.spookiness.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jspecify.annotations.Nullable;

public final class SpookySpawns {

  private static final Map<EntityType<?>, Float> PUMPKIN_HEAD_CHANCES = Map.ofEntries(
      Map.entry(EntityType.ZOMBIE, 0.15F),
      Map.entry(EntityType.HUSK, 0.15F),
      Map.entry(EntityType.ZOMBIE_VILLAGER, 0.15F),
      Map.entry(EntityType.SKELETON, 0.15F),
      Map.entry(EntityType.STRAY, 0.15F),
      Map.entry(EntityType.BOGGED, 0.15F),
      Map.entry(EntityType.SPIDER, 0.15F),
      Map.entry(EntityType.CAVE_SPIDER, 0.15F),
      Map.entry(EntityType.DROWNED, 0.1F),
      Map.entry(EntityType.ENDERMAN, 0.1F),
      Map.entry(EntityType.PIGLIN, 0.1F),
      Map.entry(EntityType.PIG, 0.1F),
      Map.entry(EntityType.COW, 0.08F),
      Map.entry(EntityType.VILLAGER, 0.05F)
  );
  private static final float JACK_O_LANTERN_CHANCE = 0.3F;

  private static final int CANDLE_CHECK_INTERVAL = 20;
  private static final float CANDLE_AWAKEN_CHANCE = 0.005F;

  private static final int BOOKSHELF_CHECK_INTERVAL = 20;
  private static final double BOOKSHELF_RADIUS = 16.0;
  private static final float BOOKSHELF_AWAKEN_CHANCE = 0.005F;

  private static final float CAMPFIRE_SWORD_CHANCE = 0.025F;
  private static final float SHEARS_CHANCE = 0.03F;
  private static final float HOE_CHANCE = 0.02F;
  private static final float PLANT_HOE_CHANCE = 0.005F;
  private static final float LAMB_SHEARS_CHANCE = 0.02F;

  private static final int NIGHT_CHECK_INTERVAL = 20;
  private static final int LANTERN_RADIUS = 4;
  private static final int LANTERN_HEIGHT = 4;
  private static final float LANTERN_AWAKEN_CHANCE = 0.005F;
  private static final double SKULL_RADIUS = 16.0;
  private static final float SKULL_AWAKEN_CHANCE = 0.002F;
  private static final float SKELETON_SKULL_CHANCE = 0.05F;
  private static final double ARMOR_STAND_RADIUS = 16.0;
  private static final float ARMOR_STAND_HAUNT_CHANCE = 0.002F;
  private static final float HARVEST_TOOL_CHANCE = 0.01F;
  private static final int HARVEST_TOOL_RADIUS = 8;

  public static void equipPumpkinHead(Mob mob, RandomSource random) {

    Float chance = PUMPKIN_HEAD_CHANCES.get(mob.getType());
    if (chance == null || mob.isBaby() || !mob.getItemBySlot(EquipmentSlot.HEAD).isEmpty() || random.nextFloat() >= HauntedHarvest.scale(mob.level(), chance)) {
      return;
    }

    mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(random.nextFloat() < JACK_O_LANTERN_CHANCE ? Items.JACK_O_LANTERN : Items.CARVED_PUMPKIN));
  }

  public static void awakenEnchantingTableBook(ServerLevel level, BlockPos pos, Player player) {

    if (!(level.getBlockEntity(pos) instanceof EnchantingTableBlockEntity table) || table.getData(ModAttachments.BOOK_AWAKENED)) {
      return;
    }

    FloatingBook book = Spawning.spawn(level, ModEntities.FLOATING_BOOK, EntitySpawnReason.TRIGGERED, Vec3.atBottomCenterOf(pos).add(0.0, 1.0, 0.0),
        player.getYRot() + 180.0F, spawned -> {
          spawned.setEnchantingTableHome(pos);
          spawned.setTarget(player);
        });
    if (book == null) {
      return;
    }

    table.setData(ModAttachments.BOOK_AWAKENED, true);
    table.setChanged();

    Particles.soulBurst(level, book.getBoundingBox().getCenter(), 24, 0.3, 0.05);
    level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 0.6F);
  }

  public static void tickCandleAwakening(ServerPlayer player) {

    if (player.tickCount % CANDLE_CHECK_INTERVAL != 0 || player.isSpectator() || !player.isHolding(ModItems.PUMPKIN_MACE) && !HauntedHarvest.isActive(player.level())) {
      return;
    }

    ServerLevel level = player.level();
    RandomSource random = player.getRandom();
    BlockPos origin = player.blockPosition();
    for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(1, 1, 1))) {
      BlockState state = level.getBlockState(pos);
      boolean candles = state.getBlock() instanceof CandleBlock || state.getBlock() instanceof CandleCakeBlock;
      if (candles && random.nextFloat() < HauntedHarvest.scale(level, CANDLE_AWAKEN_CHANCE)) {
        awakenCandles(level, pos.immutable(), state);
      }
    }
  }

  private static void awakenCandles(ServerLevel level, BlockPos pos, BlockState state) {

    boolean cake = state.getBlock() instanceof CandleCakeBlock;
    FloatingCandles candles = Spawning.spawn(level, ModEntities.FLOATING_CANDLES, EntitySpawnReason.TRIGGERED,
        Vec3.atBottomCenterOf(pos).add(0.0, cake ? 0.5 : 0.0, 0.0), SpookyMath.randomYaw(level.getRandom()), spawned -> {
          spawned.setCandles(cake ? 1 : state.getValue(CandleBlock.CANDLES));
          spawned.setColor(FloatingCandles.colorOf(state.getBlock()));
        });
    if (candles == null) {
      return;
    }

    if (cake) {
      level.setBlockAndUpdate(pos, Blocks.CAKE.defaultBlockState());
    } else {
      level.removeBlock(pos, false);
    }

    Particles.soulBurst(level, candles.getBoundingBox().getCenter(), 16, 0.25, 0.04);
    level.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 0.6F);
  }

  public static void tickBookshelfAwakening(ServerPlayer player) {

    if (player.tickCount % BOOKSHELF_CHECK_INTERVAL != 0 || player.isSpectator()) {
      return;
    }

    ServerLevel level = player.level();
    RandomSource random = player.getRandom();
    forBlockEntitiesNear(player, BOOKSHELF_RADIUS, ChiseledBookShelfBlockEntity.class, shelf -> {
      if (random.nextFloat() < HauntedHarvest.scale(level, BOOKSHELF_AWAKEN_CHANCE)) {
        awakenShelfBook(level, shelf, random);
      }
    });
  }

  private static <T extends BlockEntity> void forBlockEntitiesNear(ServerPlayer player, double radius, Class<T> type, Consumer<T> action) {

    ServerLevel level = player.level();
    int minX = SectionPos.blockToSectionCoord(player.getX() - radius);
    int maxX = SectionPos.blockToSectionCoord(player.getX() + radius);
    int minZ = SectionPos.blockToSectionCoord(player.getZ() - radius);
    int maxZ = SectionPos.blockToSectionCoord(player.getZ() + radius);

    List<T> found = new ArrayList<>();
    for (int x = minX; x <= maxX; x++) {
      for (int z = minZ; z <= maxZ; z++) {
        for (BlockEntity blockEntity : level.getChunk(x, z).getBlockEntities().values()) {
          if (type.isInstance(blockEntity) && blockEntity.getBlockPos().closerToCenterThan(player.position(), radius)) {
            found.add(type.cast(blockEntity));
          }
        }
      }
    }

    found.forEach(action);
  }

  public static void tickNightAwakenings(ServerPlayer player) {

    if (player.tickCount % NIGHT_CHECK_INTERVAL != 0 || player.isSpectator() || !player.level().isDarkOutside()) {
      return;
    }

    ServerLevel level = player.level();
    RandomSource random = player.getRandom();
    BlockPos origin = player.blockPosition();

    for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-LANTERN_RADIUS, 0, -LANTERN_RADIUS), origin.offset(LANTERN_RADIUS, LANTERN_HEIGHT, LANTERN_RADIUS))) {
      BlockState state = level.getBlockState(pos);
      if (state.is(Blocks.SOUL_LANTERN) && state.getValue(LanternBlock.HANGING) && random.nextFloat() < HauntedHarvest.scale(level, LANTERN_AWAKEN_CHANCE)) {
        awakenLantern(level, pos.immutable());
      }
    }

    forBlockEntitiesNear(player, SKULL_RADIUS, SkullBlockEntity.class, skull -> {
      BlockState state = skull.getBlockState();
      if ((state.is(Blocks.SKELETON_SKULL) || state.is(Blocks.SKELETON_WALL_SKULL)) && random.nextFloat() < HauntedHarvest.scale(level, SKULL_AWAKEN_CHANCE)) {
        awakenSkull(level, skull.getBlockPos());
      }
    });

    List<ArmorStand> stands = level.getEntitiesOfClass(ArmorStand.class, player.getBoundingBox().inflate(ARMOR_STAND_RADIUS),
        stand -> stand.getType() == EntityType.ARMOR_STAND && !stand.isMarker() && !stand.isInvisible());
    for (ArmorStand stand : stands) {
      if (random.nextFloat() < HauntedHarvest.scale(level, ARMOR_STAND_HAUNT_CHANCE) && !HauntedArmorStand.isWatched(level, stand)) {
        HauntedArmorStand.haunt(level, stand);
      }
    }
  }

  public static void tickHarvestTools(ServerPlayer player) {

    if (player.tickCount % NIGHT_CHECK_INTERVAL != 0 || player.isSpectator() || !HauntedHarvest.isActive(player.level())) {
      return;
    }
    ServerLevel level = player.level();
    RandomSource random = player.getRandom();
    if (random.nextFloat() >= HARVEST_TOOL_CHANCE) {
      return;
    }
    BlockPos guess = player.blockPosition().offset(random.nextInt(HARVEST_TOOL_RADIUS * 2 + 1) - HARVEST_TOOL_RADIUS, 0,
        random.nextInt(HARVEST_TOOL_RADIUS * 2 + 1) - HARVEST_TOOL_RADIUS);
    BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, guess).below();
    BlockState soil = level.getBlockState(ground);
    if (soil.is(Blocks.GRASS_BLOCK) || soil.is(Blocks.FARMLAND) || soil.is(Blocks.DIRT)) {
      spawnTool(level, random.nextBoolean() ? ModEntities.FLOATING_HOE : ModEntities.FLOATING_SHEARS, ground, ItemStack.EMPTY);
    }
  }

  private static void awakenLantern(ServerLevel level, BlockPos pos) {

    FloatingLantern lantern = Spawning.spawn(level, ModEntities.FLOATING_LANTERN, EntitySpawnReason.TRIGGERED, Vec3.atBottomCenterOf(pos),
        SpookyMath.randomYaw(level.getRandom()));
    if (lantern == null) {
      return;
    }

    level.removeBlock(pos, false);

    Particles.soulBurst(level, lantern.getBoundingBox().getCenter(), 16, 0.25, 0.04);
    level.playSound(null, pos, SoundEvents.CHAIN_BREAK, SoundSource.BLOCKS, 1.0F, 0.6F);
  }

  private static void awakenSkull(ServerLevel level, BlockPos pos) {

    if (spawnSkull(level, Vec3.atBottomCenterOf(pos)) instanceof FloatingSkull skull) {
      level.removeBlock(pos, false);
      skull.setFromBlock(true);
    }
  }

  public static void onSkeletonDeath(LivingEntity entity) {

    if (entity.getType() == EntityType.SKELETON && entity.level() instanceof ServerLevel level && level.isDarkOutside() && level.getRandom().nextFloat() < HauntedHarvest.scale(level, SKELETON_SKULL_CHANCE)) {
      spawnSkull(level, entity.getEyePosition());
    }
  }

  private static @Nullable FloatingSkull spawnSkull(ServerLevel level, Vec3 spawn) {

    FloatingSkull skull = Spawning.spawnFinalized(level, ModEntities.FLOATING_SKULL, EntitySpawnReason.TRIGGERED, spawn, SpookyMath.randomYaw(level.getRandom()));
    if (skull == null) {
      return null;
    }

    Particles.soulBurst(level, skull.getBoundingBox().getCenter(), 16, 0.25, 0.04);
    level.playSound(null, skull.getX(), skull.getY(), skull.getZ(), SoundEvents.SKELETON_AMBIENT, SoundSource.HOSTILE, 1.0F, 1.8F);
    return skull;
  }

  public static void onSheepSheared(ServerLevel level, Sheep sheep, ItemStack shears) {

    if (sheep.readyForShearing() && shears.is(Tags.Items.TOOLS_SHEAR) && level.getRandom().nextFloat() < HauntedHarvest.scale(level, SHEARS_CHANCE)) {
      spawnTool(level, ModEntities.FLOATING_SHEARS, sheep.blockPosition(), new ItemStack(shears.getItem()));
    }
  }

  public static void onPlantGrown(ServerLevel level, BlockPos pos, BlockState original, BlockState grown) {

    for (Property<?> property : grown.getProperties()) {
      if (property instanceof IntegerProperty age && property.getName().equals("age") && original.hasProperty(age)) {
        int max = age.getPossibleValues().getLast();
        if (grown.getValue(age) == max && original.getValue(age) < max && level.getRandom().nextFloat() < HauntedHarvest.scale(level, PLANT_HOE_CHANCE)) {
          spawnTool(level, ModEntities.FLOATING_HOE, pos, ItemStack.EMPTY);
        }
        return;
      }
    }
  }

  public static void onSheepGrownUp(Sheep sheep) {

    if (!sheep.isBaby() && sheep.level() instanceof ServerLevel level && level.getRandom().nextFloat() < HauntedHarvest.scale(level, LAMB_SHEARS_CHANCE)) {
      spawnTool(level, ModEntities.FLOATING_SHEARS, sheep.blockPosition(), ItemStack.EMPTY);
    }
  }

  public static void onHoeTill(ServerLevel level, BlockPos pos, BlockState state, ItemStack hoe) {

    boolean tillable = state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT) || state.is(Blocks.DIRT_PATH) || state.is(Blocks.COARSE_DIRT);
    if (tillable && level.isEmptyBlock(pos.above()) && level.getRandom().nextFloat() < HauntedHarvest.scale(level, HOE_CHANCE)) {
      spawnTool(level, ModEntities.FLOATING_HOE, pos, new ItemStack(hoe.getItem()));
    }
  }

  private static void spawnTool(ServerLevel level, EntityType<? extends FloatingTool> type, BlockPos pos, ItemStack held) {

    FloatingTool tool = Spawning.spawnFinalized(level, type, EntitySpawnReason.TRIGGERED, Vec3.atBottomCenterOf(pos).add(0.0, 1.0, 0.0),
        SpookyMath.randomYaw(level.getRandom()), spawned -> {
          if (!held.isEmpty()) {
            spawned.setItemSlot(EquipmentSlot.MAINHAND, held);
          }
        });
    if (tool == null) {
      return;
    }

    Particles.soulBurst(level, tool.getBoundingBox().getCenter(), 32, 0.5, 0.06);
    level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 0.6F);
  }

  private static void awakenShelfBook(ServerLevel level, ChiseledBookShelfBlockEntity shelf, RandomSource random) {

    List<Integer> filled = new ArrayList<>();
    for (int slot = 0; slot < shelf.getContainerSize(); slot++) {
      if (!shelf.getItem(slot).isEmpty()) {
        filled.add(slot);
      }
    }
    if (filled.isEmpty()) {
      return;
    }

    int slot = filled.get(random.nextInt(filled.size()));
    ItemStack taken = shelf.removeItem(slot, 1);
    if (taken.isEmpty()) {
      return;
    }

    BlockPos pos = shelf.getBlockPos();
    Direction facing = shelf.getBlockState().getValue(ChiseledBookShelfBlock.FACING);
    FloatingBook book = Spawning.spawn(level, ModEntities.FLOATING_BOOK, EntitySpawnReason.TRIGGERED, FloatingBook.shelfFront(shelf), facing.toYRot(),
        spawned -> spawned.setShelfHome(pos, slot, taken));
    if (book == null) {
      shelf.setItem(slot, taken);
      return;
    }

    Particles.soulBurst(level, book.getBoundingBox().getCenter(), 16, 0.25, 0.04);
    level.playSound(null, pos, SoundEvents.CHISELED_BOOKSHELF_PICKUP, SoundSource.BLOCKS, 1.0F, 0.6F);
  }

  public static void onCampfireCooked(ServerLevel level, BlockPos pos) {

    if (level.getRandom().nextFloat() >= HauntedHarvest.scale(level, CAMPFIRE_SWORD_CHANCE)) {
      return;
    }

    spawnTool(level, ModEntities.FLOATING_SWORD, pos, ItemStack.EMPTY);
  }
}
