package io.github.jason13official.spookiness.world;

import io.github.jason13official.spookiness.effect.SoulBurst;
import io.github.jason13official.spookiness.entity.FloatingBook;
import io.github.jason13official.spookiness.entity.FloatingCandles;
import io.github.jason13official.spookiness.entity.FloatingSword;
import io.github.jason13official.spookiness.registry.ModAttachments;
import io.github.jason13official.spookiness.registry.ModEntities;
import io.github.jason13official.spookiness.registry.ModItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.CandleCakeBlock;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

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

  public static void equipPumpkinHead(Mob mob, RandomSource random) {

    Float chance = PUMPKIN_HEAD_CHANCES.get(mob.getType());
    if (chance == null || mob.isBaby() || !mob.getItemBySlot(EquipmentSlot.HEAD).isEmpty() || random.nextFloat() >= chance) {
      return;
    }

    mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(random.nextFloat() < JACK_O_LANTERN_CHANCE ? Items.JACK_O_LANTERN : Items.CARVED_PUMPKIN));
  }

  public static void awakenEnchantingTableBook(ServerLevel level, BlockPos pos, Player player) {

    if (!(level.getBlockEntity(pos) instanceof EnchantingTableBlockEntity table) || table.getData(ModAttachments.BOOK_AWAKENED)) {
      return;
    }

    FloatingBook book = ModEntities.FLOATING_BOOK.create(level, EntitySpawnReason.TRIGGERED);
    if (book == null) {
      return;
    }

    table.setData(ModAttachments.BOOK_AWAKENED, true);
    table.setChanged();

    Vec3 spawn = Vec3.atBottomCenterOf(pos).add(0.0, 1.0, 0.0);
    book.snapTo(spawn.x, spawn.y, spawn.z, player.getYRot() + 180.0F, 0.0F);
    book.setEnchantingTableHome(pos);
    book.setTarget(player);
    level.addFreshEntity(book);

    SoulBurst.spawn(level, book.getBoundingBox().getCenter(), 24, 0.3, 0.05);
    level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0F, 0.6F);
  }

  public static void tickCandleAwakening(ServerPlayer player) {

    if (player.tickCount % CANDLE_CHECK_INTERVAL != 0 || player.isSpectator() || !player.isHolding(ModItems.PUMPKIN_MACE)) {
      return;
    }

    ServerLevel level = player.level();
    RandomSource random = player.getRandom();
    BlockPos origin = player.blockPosition();
    for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-1, -1, -1), origin.offset(1, 1, 1))) {
      BlockState state = level.getBlockState(pos);
      boolean candles = state.getBlock() instanceof CandleBlock || state.getBlock() instanceof CandleCakeBlock;
      if (candles && random.nextFloat() < CANDLE_AWAKEN_CHANCE) {
        awakenCandles(level, pos.immutable(), state);
      }
    }
  }

  private static void awakenCandles(ServerLevel level, BlockPos pos, BlockState state) {

    FloatingCandles candles = ModEntities.FLOATING_CANDLES.create(level, EntitySpawnReason.TRIGGERED);
    if (candles == null) {
      return;
    }

    boolean cake = state.getBlock() instanceof CandleCakeBlock;
    if (cake) {
      level.setBlockAndUpdate(pos, Blocks.CAKE.defaultBlockState());
    } else {
      level.removeBlock(pos, false);
    }

    Vec3 spawn = Vec3.atBottomCenterOf(pos).add(0.0, cake ? 0.5 : 0.0, 0.0);
    candles.snapTo(spawn.x, spawn.y, spawn.z, level.getRandom().nextFloat() * 360.0F, 0.0F);
    candles.setCandles(cake ? 1 : state.getValue(CandleBlock.CANDLES));
    candles.setColor(FloatingCandles.colorOf(state.getBlock()));
    level.addFreshEntity(candles);

    SoulBurst.spawn(level, candles.getBoundingBox().getCenter(), 16, 0.25, 0.04);
    level.playSound(null, pos, SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 0.6F);
  }

  public static void tickBookshelfAwakening(ServerPlayer player) {

    if (player.tickCount % BOOKSHELF_CHECK_INTERVAL != 0 || player.isSpectator()) {
      return;
    }

    ServerLevel level = player.level();
    RandomSource random = player.getRandom();
    int minX = SectionPos.blockToSectionCoord(player.getX() - BOOKSHELF_RADIUS);
    int maxX = SectionPos.blockToSectionCoord(player.getX() + BOOKSHELF_RADIUS);
    int minZ = SectionPos.blockToSectionCoord(player.getZ() - BOOKSHELF_RADIUS);
    int maxZ = SectionPos.blockToSectionCoord(player.getZ() + BOOKSHELF_RADIUS);

    List<ChiseledBookShelfBlockEntity> shelves = new ArrayList<>();
    for (int x = minX; x <= maxX; x++) {
      for (int z = minZ; z <= maxZ; z++) {
        for (BlockEntity blockEntity : level.getChunk(x, z).getBlockEntities().values()) {
          if (blockEntity instanceof ChiseledBookShelfBlockEntity shelf && shelf.getBlockPos().closerToCenterThan(player.position(), BOOKSHELF_RADIUS)) {
            shelves.add(shelf);
          }
        }
      }
    }

    for (ChiseledBookShelfBlockEntity shelf : shelves) {
      if (random.nextFloat() < BOOKSHELF_AWAKEN_CHANCE) {
        awakenShelfBook(level, shelf, random);
      }
    }
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

    FloatingBook book = ModEntities.FLOATING_BOOK.create(level, EntitySpawnReason.TRIGGERED);
    if (book == null) {
      return;
    }

    int slot = filled.get(random.nextInt(filled.size()));
    ItemStack taken = shelf.removeItem(slot, 1);
    if (taken.isEmpty()) {
      return;
    }

    BlockPos pos = shelf.getBlockPos();
    Direction facing = shelf.getBlockState().getValue(ChiseledBookShelfBlock.FACING);
    Vec3 spawn = Vec3.atCenterOf(pos).add(facing.getStepX() * 0.8, -0.3, facing.getStepZ() * 0.8);
    book.snapTo(spawn.x, spawn.y, spawn.z, facing.toYRot(), 0.0F);
    book.setShelfHome(pos, slot, taken);
    level.addFreshEntity(book);

    SoulBurst.spawn(level, book.getBoundingBox().getCenter(), 16, 0.25, 0.04);
    level.playSound(null, pos, SoundEvents.CHISELED_BOOKSHELF_PICKUP, SoundSource.BLOCKS, 1.0F, 0.6F);
  }

  public static void onCampfireCooked(ServerLevel level, BlockPos pos) {

    if (level.getRandom().nextFloat() >= CAMPFIRE_SWORD_CHANCE) {
      return;
    }

    FloatingSword sword = ModEntities.FLOATING_SWORD.create(level, EntitySpawnReason.TRIGGERED);
    if (sword == null) {
      return;
    }

    Vec3 spawn = Vec3.atBottomCenterOf(pos).add(0.0, 1.0, 0.0);
    sword.snapTo(spawn.x, spawn.y, spawn.z, level.getRandom().nextFloat() * 360.0F, 0.0F);
    EventHooks.finalizeMobSpawn(sword, level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.TRIGGERED, null);
    level.addFreshEntity(sword);

    SoulBurst.spawn(level, sword.getBoundingBox().getCenter(), 32, 0.5, 0.06);
    level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 1.0F, 0.6F);
  }
}
