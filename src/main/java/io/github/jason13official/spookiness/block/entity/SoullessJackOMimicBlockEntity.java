package io.github.jason13official.spookiness.block.entity;

import net.minecraft.world.phys.AABB;
import io.github.jason13official.spookiness.registry.ModItems;
import io.github.jason13official.spookiness.entity.boss.Gourdwyrm;
import io.github.jason13official.spookiness.block.SoullessJackOMimicBlock;
import io.github.jason13official.spookiness.registry.ModBlockEntities;
import io.github.jason13official.spookiness.world.netherrealm.NetherrealmArena;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class SoullessJackOMimicBlockEntity extends BlockEntity {

  private static final double OPEN_RANGE = 4.0;
  private static final float OPEN_SPEED = 0.1F;
  private static final double FIGHT_RANGE = 96.0;
  private static final int FIGHT_CHECK_INTERVAL = 10;
  private static final double FLAME_SPREAD = 0.3;
  private static final double FLAME_HEIGHT = 0.6;
  private static final double ENCHANT_REACH = 3.0;
  private static final int ENCHANT_PER_TICK = 2;

  private ItemStack item = ItemStack.EMPTY;
  private float openness;
  private float oOpenness;

  public SoullessJackOMimicBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.SOULLESS_JACK_O_MIMIC, pos, state);
  }

  public static void serverTick(Level level, BlockPos pos, BlockState state, SoullessJackOMimicBlockEntity mimic) {

    if (level.getGameTime() % FIGHT_CHECK_INTERVAL != 0) {
      return;
    }
    boolean fighting = mimic.item.is(ModItems.LAMENT_CONFIGURATION) && !level.getEntitiesOfClass(Gourdwyrm.class, new AABB(pos).inflate(FIGHT_RANGE)).isEmpty();
    if (state.getValue(SoullessJackOMimicBlock.LIT) != fighting) {
      level.setBlock(pos, state.setValue(SoullessJackOMimicBlock.LIT, fighting), Block.UPDATE_ALL);
    }
  }

  public static void clientTick(Level level, BlockPos pos, BlockState state, SoullessJackOMimicBlockEntity mimic) {

    boolean fighting = state.getValue(SoullessJackOMimicBlock.LIT);
    if (fighting) {
      emitFightParticles(level, pos, level.getRandom());
    }

    boolean near = fighting || level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, OPEN_RANGE, false) != null;
    mimic.oOpenness = mimic.openness;
    mimic.openness = Mth.approach(mimic.openness, near ? 1.0F : 0.0F, OPEN_SPEED);
    if (mimic.oOpenness == 0.0F && mimic.openness > 0.0F) {
      level.playLocalSound(pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, 0.6F, false);
    } else if (mimic.oOpenness > 0.0F && mimic.openness == 0.0F) {
      level.playLocalSound(pos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, 0.6F, false);
    }
  }

  private static void emitFightParticles(Level level, BlockPos pos, RandomSource random) {

    double x = pos.getX() + 0.5;
    double y = pos.getY() + FLAME_HEIGHT;
    double z = pos.getZ() + 0.5;
    if (random.nextInt(2) == 0) {
      level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x + spread(random), y + random.nextDouble() * 0.4, z + spread(random), 0.0, 0.03, 0.0);
    }
    if (random.nextInt(2) == 0) {
      level.addParticle(ParticleTypes.FLAME, x + spread(random), y + random.nextDouble() * 0.4, z + spread(random), 0.0, 0.02, 0.0);
    }
    if (random.nextInt(3) == 0) {
      level.addParticle(ParticleTypes.SMOKE, x + spread(random), pos.getY() + 1.1, z + spread(random), 0.0, 0.04, 0.0);
    }
    for (int i = 0; i < ENCHANT_PER_TICK; i++) {
      level.addParticle(ParticleTypes.ENCHANT, x, y, z, (random.nextFloat() - 0.5F) * ENCHANT_REACH, random.nextFloat() * ENCHANT_REACH * 0.5,
          (random.nextFloat() - 0.5F) * ENCHANT_REACH);
    }
  }

  private static double spread(RandomSource random) {
    return (random.nextDouble() - 0.5) * 2.0 * FLAME_SPREAD;
  }

  public float getOpenness(float partialTicks) {
    return Mth.lerp(partialTicks, this.oOpenness, this.openness);
  }

  public ItemStack getItem() {
    return this.item;
  }

  public boolean isHolding() {
    return !this.item.isEmpty();
  }

  public void offer(ServerLevel level, @Nullable LivingEntity user, ItemStack held) {

    ItemStack previous = this.release(level, user, true);
    this.item = held.consumeAndReturn(1, user);
    this.markUpdated();
    level.playSound(null, this.worldPosition, SoundEvents.GENERIC_EAT.value(), SoundSource.BLOCKS, 0.8F, 0.6F);
    level.gameEvent(GameEvent.BLOCK_CHANGE, this.worldPosition, GameEvent.Context.of(user, this.getBlockState()));
    if (!previous.isEmpty() || this.isHolding()) {
      NetherrealmArena.onAltarChanged(level, this.worldPosition, this.item, previous, user instanceof Player player ? player : null);
    }
  }

  public ItemStack release(ServerLevel level, @Nullable LivingEntity user, boolean quietly) {

    if (this.item.isEmpty()) {
      return ItemStack.EMPTY;
    }
    ItemStack released = this.item;
    this.item = ItemStack.EMPTY;
    this.markUpdated();
    Direction facing = this.getBlockState().getValue(SoullessJackOMimicBlock.FACING);
    Block.popResourceFromFace(level, this.worldPosition, facing, released.copy());
    level.playSound(null, this.worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.8F, 0.6F);
    if (!quietly) {
      NetherrealmArena.onAltarChanged(level, this.worldPosition, ItemStack.EMPTY, released, user instanceof Player player ? player : null);
    }
    return released;
  }

  private void markUpdated() {

    this.setChanged();
    if (this.level != null) {
      this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL);
    }
  }

  @Override
  public void preRemoveSideEffects(BlockPos pos, BlockState state) {
    if (this.level != null && this.isHolding()) {
      Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), this.item);
      this.item = ItemStack.EMPTY;
    }
  }

  @Override
  protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    this.item = input.read("item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
  }

  @Override
  protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    if (!this.item.isEmpty()) {
      output.store("item", ItemStack.CODEC, this.item);
    }
  }

  @Override
  public ClientboundBlockEntityDataPacket getUpdatePacket() {
    return ClientboundBlockEntityDataPacket.create(this);
  }

  @Override
  public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
    return this.saveWithoutMetadata(registries);
  }
}
