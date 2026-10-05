package io.github.jason13official.spookiness.block.entity;

import io.github.jason13official.spookiness.block.SoullessJackOMimicBlock;
import io.github.jason13official.spookiness.registry.ModBlockEntities;
import io.github.jason13official.spookiness.world.netherrealm.NetherrealmArena;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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

  private ItemStack item = ItemStack.EMPTY;
  private float openness;
  private float oOpenness;

  public SoullessJackOMimicBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.SOULLESS_JACK_O_MIMIC, pos, state);
  }

  public static void clientTick(Level level, BlockPos pos, BlockState state, SoullessJackOMimicBlockEntity mimic) {

    boolean near = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, OPEN_RANGE, false) != null;
    mimic.oOpenness = mimic.openness;
    mimic.openness = Mth.approach(mimic.openness, near ? 1.0F : 0.0F, OPEN_SPEED);
    if (mimic.oOpenness == 0.0F && mimic.openness > 0.0F) {
      level.playLocalSound(pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5F, 0.6F, false);
    } else if (mimic.oOpenness > 0.0F && mimic.openness == 0.0F) {
      level.playLocalSound(pos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, 0.6F, false);
    }
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
