package io.github.jason13official.spookiness.entity.book;

import com.mojang.serialization.Codec;
import io.github.jason13official.spookiness.registry.ModAttachments;
import io.github.jason13official.spookiness.util.BlockEntities;
import java.util.Comparator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

final class BookShelfHome {

  static final int HOME_RADIUS = 12;

  private static final int MIN_STAY_TICKS = 600;
  private static final int MAX_STAY_TICKS = 1200;
  private static final int ADOPT_RADIUS = 8;

  private final FloatingBook book;
  private ItemStack heldBook = ItemStack.EMPTY;
  private Kind kind = Kind.NONE;
  private @Nullable BlockPos pos;
  private int slot = -1;
  private int ticksOutside;
  private int stayTicks;

  BookShelfHome(FloatingBook book) {
    this.book = book;
  }

  ItemStack heldBook() {
    return this.heldBook;
  }

  boolean isShelf() {
    return this.kind == Kind.SHELF;
  }

  boolean isRestless() {
    return this.isShelf() && this.ticksOutside >= this.stayTicks;
  }

  void setEnchantingTable(BlockPos pos) {
    this.kind = Kind.ENCHANTING_TABLE;
    this.pos = pos.immutable();
    this.book.setPersistenceRequired();
  }

  void setShelf(BlockPos pos, int slot, ItemStack held) {
    this.heldBook = held.copy();
    this.book.setNeutral(true);
    this.kind = Kind.SHELF;
    this.pos = pos.immutable();
    this.slot = slot;
    this.ticksOutside = 0;
    this.stayTicks = Mth.nextInt(this.book.getRandom(), MIN_STAY_TICKS, MAX_STAY_TICKS);
    this.book.setHomeTo(this.pos, HOME_RADIUS);
    this.book.setPersistenceRequired();
  }

  void countTimeOutside() {
    if (this.isShelf()) {
      this.ticksOutside++;
    }
  }

  void lose() {
    this.kind = Kind.NONE;
    this.pos = null;
    this.slot = -1;
    this.book.clearHome();
  }

  @Nullable ChiseledBookShelfBlockEntity findShelf() {

    if (this.pos != null && this.book.level().getBlockEntity(this.pos) instanceof ChiseledBookShelfBlockEntity shelf && freeSlot(shelf, this.slot) >= 0) {
      return shelf;
    }
    ChiseledBookShelfBlockEntity adopted = BlockEntities.near(this.book.level(), this.book.position(), ADOPT_RADIUS, ChiseledBookShelfBlockEntity.class).stream()
        .filter(shelf -> freeSlot(shelf, -1) >= 0)
        .min(Comparator.comparingDouble(shelf -> shelf.getBlockPos().distToCenterSqr(this.book.position())))
        .orElse(null);
    if (adopted != null) {
      this.pos = adopted.getBlockPos().immutable();
      this.slot = -1;
      this.book.setHomeTo(this.pos, HOME_RADIUS);
    }
    return adopted;
  }

  void enter(ChiseledBookShelfBlockEntity shelf) {
    int free = freeSlot(shelf, this.slot);
    if (free < 0) {
      return;
    }
    shelf.setItem(free, this.heldBook.copy());
    this.heldBook = ItemStack.EMPTY;
    this.book.level().playSound(null, shelf.getBlockPos(), SoundEvents.CHISELED_BOOKSHELF_INSERT, SoundSource.BLOCKS, 1.0F, 1.0F);
    this.book.discard();
  }

  void onDeath(ServerLevel level) {
    if (this.kind == Kind.ENCHANTING_TABLE && this.pos != null && level.getBlockEntity(this.pos) instanceof EnchantingTableBlockEntity table) {
      table.setData(ModAttachments.BOOK_AWAKENED, false);
      table.setChanged();
      level.playSound(null, this.pos, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
  }

  void save(ValueOutput output) {
    if (!this.heldBook.isEmpty()) {
      output.store("held_book", ItemStack.CODEC, this.heldBook);
    }
    output.store("home_kind", Kind.CODEC, this.kind);
    output.storeNullable("home_pos", BlockPos.CODEC, this.pos);
    output.putInt("home_slot", this.slot);
    output.putInt("ticks_outside", this.ticksOutside);
    output.putInt("stay_ticks", this.stayTicks);
  }

  void load(ValueInput input) {
    this.heldBook = input.read("held_book", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    this.book.setNeutral(!this.heldBook.isEmpty());
    this.kind = input.read("home_kind", Kind.CODEC).orElse(Kind.NONE);
    this.pos = input.read("home_pos", BlockPos.CODEC).orElse(null);
    this.slot = input.getIntOr("home_slot", -1);
    this.ticksOutside = input.getIntOr("ticks_outside", 0);
    this.stayTicks = input.getIntOr("stay_ticks", MIN_STAY_TICKS);
    if (this.pos == null) {
      this.lose();
    } else if (this.kind == Kind.SHELF) {
      this.book.setHomeTo(this.pos, HOME_RADIUS);
    }
  }

  private static int freeSlot(ChiseledBookShelfBlockEntity shelf, int preferred) {
    if (preferred >= 0 && preferred < shelf.getContainerSize() && shelf.getItem(preferred).isEmpty()) {
      return preferred;
    }
    for (int slot = 0; slot < shelf.getContainerSize(); slot++) {
      if (shelf.getItem(slot).isEmpty()) {
        return slot;
      }
    }
    return -1;
  }

  private enum Kind implements StringRepresentable {
    NONE("none"),
    ENCHANTING_TABLE("enchanting_table"),
    SHELF("bookshelf");

    static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

    private final String name;

    Kind(String name) {
      this.name = name;
    }

    @Override
    public String getSerializedName() {
      return this.name;
    }
  }
}
