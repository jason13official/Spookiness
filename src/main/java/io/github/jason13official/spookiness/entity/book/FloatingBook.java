package io.github.jason13official.spookiness.entity.book;

import io.github.jason13official.spookiness.entity.FloatingPathfinderMob;
import io.github.jason13official.spookiness.registry.ModEntities;
import net.minecraft.world.entity.EntityDimensions;
import com.mojang.serialization.Codec;
import io.github.jason13official.spookiness.registry.ModAttachments;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.entity.EnchantingTableBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FloatingBook extends FloatingPathfinderMob {

  private static final EntityDataAccessor<Boolean> DATA_NEUTRAL = SynchedEntityData.defineId(FloatingBook.class, EntityDataSerializers.BOOLEAN);
  private static final EntityDataAccessor<Boolean> DATA_STAYS_STILL = SynchedEntityData.defineId(FloatingBook.class, EntityDataSerializers.BOOLEAN);
  private static final EntityDataAccessor<Boolean> DATA_PHASING = SynchedEntityData.defineId(FloatingBook.class, EntityDataSerializers.BOOLEAN);

  private static final double READING_DISTANCE = 4.0;
  static final int MAX_STILL_TIMEOUT = 10;
  private static final float PHASE_ALPHA = 0.5F;
  private static final float PHASE_FADE_PER_TICK = 0.1F;
  private static final float LAPIS_BLOCK_CHANCE = 0.25F;
  private static final int MIN_LAPIS = 4;
  private static final int MAX_LAPIS = 10;

  private static final int HOME_RADIUS = 12;
  private static final int MIN_STAY_TICKS = 600;
  private static final int MAX_STAY_TICKS = 1200;
  private static final int MAX_RETURN_TICKS = 600;
  private static final double ENTER_DISTANCE = 0.8;
  private static final double SHELF_CLEARANCE = 0.1;
  private static final int ADOPT_RADIUS = 8;

  public float open;
  public float oOpen;
  public float flip;
  public float oFlip;
  private float flipT;
  private float flipA;

  private ItemStack heldBook = ItemStack.EMPTY;
  private Home homeKind = Home.NONE;
  private @Nullable BlockPos homePos;
  private int homeSlot = -1;
  private int ticksOutside;
  private int stayTicks;

  private int serverStillTimeout;
  private boolean supportingPlayer;
  private @Nullable Player guidedPlayer;
  private @Nullable FloatingBook guidePlatform;
  private float alpha = 1.0F;
  private float oAlpha = 1.0F;

  public FloatingBook(EntityType<? extends FloatingBook> type, Level level) {
    super(type, level);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return createFloatingAttributes().add(Attributes.MAX_HEALTH, 10.0F).add(Attributes.ATTACK_DAMAGE, 2.0F).add(Attributes.ATTACK_KNOCKBACK, 1.0F);
  }

  @Override
  protected void registerGoals() {

    int goalPriority = 1;

    this.goalSelector.addGoal(goalPriority++, new MeleeAttackGoal(this, 1.4, true));
    this.goalSelector.addGoal(goalPriority++, new BookStepGoal(this));
    this.goalSelector.addGoal(goalPriority++, new ReturnToShelfGoal());
    this.goalSelector.addGoal(goalPriority++, new WaterAvoidingRandomFlyingGoal(this, 1.0) {

      @Override
      public boolean canUse() {
        return !FloatingBook.this.isOnStillTimeout() && FloatingBook.this.guidedPlayer == null && super.canUse();
      }
    });
    this.goalSelector.addGoal(goalPriority++, new LookAtPlayerGoal(this, Player.class, 8.0F));
    this.goalSelector.addGoal(goalPriority++, new RandomLookAroundGoal(this));

    int targetPriority = 1;

    this.targetSelector.addGoal(targetPriority++, new HurtByTargetGoal(this).setAlertOthers());
    this.targetSelector.addGoal(targetPriority++, new NearestAttackableTargetGoal<>(this, Player.class, true) {

      @Override
      public boolean canUse() {
        return !FloatingBook.this.isNeutral() && super.canUse();
      }
    });
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder entityData) {
    super.defineSynchedData(entityData);
    entityData.define(DATA_NEUTRAL, false);
    entityData.define(DATA_STAYS_STILL, false);
    entityData.define(DATA_PHASING, false);
  }

  public boolean isNeutral() {
    return this.entityData.get(DATA_NEUTRAL);
  }

  public boolean staysStill() {
    return this.entityData.get(DATA_STAYS_STILL);
  }

  private boolean isOnStillTimeout() {
    return this.staysStill() || this.serverStillTimeout > 0;
  }

  void setServerStillTimeout(int timeout) {
    if (this.serverStillTimeout <= 0 && timeout > 0 && this.level() instanceof ServerLevel level) {
      this.syncPacketPositionCodec(this.getX(), this.getY(), this.getZ());
      level.getChunkSource().chunkMap.sendToTrackingPlayers(this, ClientboundEntityPositionSyncPacket.of(this));
    }
    this.serverStillTimeout = timeout;
    this.entityData.set(DATA_STAYS_STILL, timeout > 0);
  }

  boolean isCalmStep() {
    return this.isAlive() && this.isNeutral() && !this.isAggressive();
  }

  private @Nullable Player scanPlayerAbove() {
    AABB box = this.getBoundingBox();
    AABB detection = new AABB(box.minX - 0.5, box.maxY - 1.0E-5F, box.minZ - 0.5, box.maxX + 0.5, box.maxY + box.getYsize(), box.maxZ + 0.5);
    for (Player player : this.level().players()) {
      if (!player.isSpectator() && player.onGround() && player.getVehicle() == null && detection.contains(player.position())) {
        return player;
      }
    }
    return null;
  }

  public boolean canGuide() {
    return this.isCalmStep() && this.getTarget() == null && !this.supportingPlayer && this.guidedPlayer == null;
  }

  @Nullable Player guidedPlayer() {
    return this.guidedPlayer;
  }

  @Nullable FloatingBook guidePlatform() {
    return this.guidePlatform;
  }

  boolean isSupportingPlayer() {
    return this.supportingPlayer;
  }

  public boolean isGuiding(Player player) {
    return this.guidedPlayer == player;
  }

  public void startGuiding(Player player, FloatingBook platform) {
    this.guidedPlayer = player;
    this.guidePlatform = platform;
  }

  public void setGuidePlatform(FloatingBook platform) {
    this.guidePlatform = platform;
  }

  public void stopGuiding() {
    this.guidedPlayer = null;
    this.guidePlatform = null;
    this.setPhasing(false);
  }

  public boolean isPhasing() {
    return this.entityData.get(DATA_PHASING);
  }

  void setPhasing(boolean phasing) {
    this.entityData.set(DATA_PHASING, phasing);
  }

  public float getAlpha(float partialTicks) {
    return Mth.lerp(partialTicks, this.oAlpha, this.alpha);
  }

  boolean canPhase() {
    return this.isNeutral() && !this.supportingPlayer && !this.isBelowAnyPlayer();
  }

  private boolean isBelowAnyPlayer() {
    AABB box = this.getBoundingBox();
    AABB column = new AABB(box.minX - 0.3, box.maxY - 0.01, box.minZ - 0.3, box.maxX + 0.3, box.maxY + 2.5, box.maxZ + 0.3);
    for (Player player : this.level().players()) {
      if (!player.isSpectator() && column.contains(player.position())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean canBeCollidedWith(@Nullable Entity other) {
    if (!(other instanceof Player) || !this.isCalmStep() || this.isPhasing()) {
      return false;
    }
    if (this.level().isClientSide() && other instanceof Player && other.position().y >= this.getBoundingBox().maxY) {
      return true;
    }
    return this.isOnStillTimeout();
  }

  @Override
  public void push(Entity entity) {
    if (!(entity instanceof Player)) {
      super.push(entity);
    }
  }

  @Override
  protected void doPush(Entity entity) {
    if (!(entity instanceof Player)) {
      super.doPush(entity);
    }
  }

  @Override
  public boolean isPushable() {
    return !this.isOnStillTimeout() && !this.isPhasing() && super.isPushable();
  }

  @Override
  public void aiStep() {
    if (!this.level().isClientSide()) {
      this.setRequiresPrecisePosition(this.isOnStillTimeout());
    }
    super.aiStep();
  }

  @Override
  public void onRemovedFromLevel() {
    super.onRemovedFromLevel();
    BookStairs.forget(this);
  }

  public void setEnchantingTableHome(BlockPos pos) {
    this.homeKind = Home.ENCHANTING_TABLE;
    this.homePos = pos.immutable();
    this.setPersistenceRequired();
  }

  public void setShelfHome(BlockPos pos, int slot, ItemStack book) {
    this.heldBook = book.copy();
    this.entityData.set(DATA_NEUTRAL, true);
    this.homeKind = Home.BOOKSHELF;
    this.homePos = pos.immutable();
    this.homeSlot = slot;
    this.ticksOutside = 0;
    this.stayTicks = Mth.nextInt(this.random, MIN_STAY_TICKS, MAX_STAY_TICKS);
    this.setHomeTo(this.homePos, HOME_RADIUS);
    this.setPersistenceRequired();
  }

  private void loseHome() {
    this.homeKind = Home.NONE;
    this.homePos = null;
    this.homeSlot = -1;
    this.clearHome();
  }

  @Override
  protected void addAdditionalSaveData(ValueOutput output) {
    super.addAdditionalSaveData(output);
    if (!this.heldBook.isEmpty()) {
      output.store("held_book", ItemStack.CODEC, this.heldBook);
    }
    output.store("home_kind", Home.CODEC, this.homeKind);
    output.storeNullable("home_pos", BlockPos.CODEC, this.homePos);
    output.putInt("home_slot", this.homeSlot);
    output.putInt("ticks_outside", this.ticksOutside);
    output.putInt("stay_ticks", this.stayTicks);
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    this.heldBook = input.read("held_book", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    this.entityData.set(DATA_NEUTRAL, !this.heldBook.isEmpty());
    this.homeKind = input.read("home_kind", Home.CODEC).orElse(Home.NONE);
    this.homePos = input.read("home_pos", BlockPos.CODEC).orElse(null);
    this.homeSlot = input.getIntOr("home_slot", -1);
    this.ticksOutside = input.getIntOr("ticks_outside", 0);
    this.stayTicks = input.getIntOr("stay_ticks", MIN_STAY_TICKS);
    if (this.homePos == null) {
      this.loseHome();
    } else if (this.homeKind == Home.BOOKSHELF) {
      this.setHomeTo(this.homePos, HOME_RADIUS);
    }
  }

  @Override
  protected void customServerAiStep(ServerLevel level) {
    super.customServerAiStep(level);

    if (this.serverStillTimeout > 0) {
      this.setServerStillTimeout(this.serverStillTimeout - 1);
    }

    Player rider = this.isCalmStep() && this.getTarget() == null ? this.scanPlayerAbove() : null;
    this.supportingPlayer = rider != null;
    if (rider != null) {
      this.setServerStillTimeout(MAX_STILL_TIMEOUT);
      BookStairs.requestStep(rider, this);
    }

    if (this.isOnStillTimeout()) {
      this.getNavigation().stop();
      this.getMoveControl().setWait();
      this.setDeltaMovement(Vec3.ZERO);
    }

    if (this.homeKind == Home.BOOKSHELF && this.getTarget() == null && !this.supportingPlayer && this.guidedPlayer == null) {
      this.ticksOutside++;
    }
  }

  @Override
  public void die(DamageSource source) {
    super.die(source);
    if (this.homeKind == Home.ENCHANTING_TABLE && this.homePos != null && this.level() instanceof ServerLevel level
        && level.getBlockEntity(this.homePos) instanceof EnchantingTableBlockEntity table) {
      table.setData(ModAttachments.BOOK_AWAKENED, false);
      table.setChanged();
      level.playSound(null, this.homePos, SoundEvents.BOOK_PUT, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
  }

  private static int findShelfSlot(ChiseledBookShelfBlockEntity shelf, int preferred) {
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

  public static Vec3 shelfFront(ChiseledBookShelfBlockEntity shelf) {
    Direction facing = shelf.getBlockState().getValue(ChiseledBookShelfBlock.FACING);
    EntityDimensions size = ModEntities.FLOATING_BOOK.getDimensions();
    double out = 0.5 + size.width() / 2.0 + SHELF_CLEARANCE;
    return Vec3.atCenterOf(shelf.getBlockPos()).add(facing.getStepX() * out, -size.height() / 2.0, facing.getStepZ() * out);
  }

  private @Nullable ChiseledBookShelfBlockEntity getHomeShelf() {
    if (this.homePos != null && this.level().getBlockEntity(this.homePos) instanceof ChiseledBookShelfBlockEntity shelf
        && findShelfSlot(shelf, this.homeSlot) >= 0) {
      return shelf;
    }
    return null;
  }

  private @Nullable ChiseledBookShelfBlockEntity adoptNearbyShelf() {
    ChiseledBookShelfBlockEntity closest = null;
    double closestDistance = Double.MAX_VALUE;
    int minX = SectionPos.blockToSectionCoord(this.getX() - ADOPT_RADIUS);
    int maxX = SectionPos.blockToSectionCoord(this.getX() + ADOPT_RADIUS);
    int minZ = SectionPos.blockToSectionCoord(this.getZ() - ADOPT_RADIUS);
    int maxZ = SectionPos.blockToSectionCoord(this.getZ() + ADOPT_RADIUS);
    for (int x = minX; x <= maxX; x++) {
      for (int z = minZ; z <= maxZ; z++) {
        for (BlockEntity blockEntity : this.level().getChunk(x, z).getBlockEntities().values()) {
          if (blockEntity instanceof ChiseledBookShelfBlockEntity shelf && findShelfSlot(shelf, -1) >= 0) {
            double distance = shelf.getBlockPos().distToCenterSqr(this.position());
            if (distance <= ADOPT_RADIUS * ADOPT_RADIUS && distance < closestDistance) {
              closest = shelf;
              closestDistance = distance;
            }
          }
        }
      }
    }
    if (closest != null) {
      this.homePos = closest.getBlockPos().immutable();
      this.homeSlot = -1;
      this.setHomeTo(this.homePos, HOME_RADIUS);
    }
    return closest;
  }

  private void enterShelf(ChiseledBookShelfBlockEntity shelf) {
    int slot = findShelfSlot(shelf, this.homeSlot);
    if (slot < 0) {
      return;
    }
    shelf.setItem(slot, this.heldBook.copy());
    this.heldBook = ItemStack.EMPTY;
    this.level().playSound(null, shelf.getBlockPos(), SoundEvents.CHISELED_BOOKSHELF_INSERT, SoundSource.BLOCKS, 1.0F, 1.0F);
    this.discard();
  }

  @Override
  public void tick() {
    super.tick();
    if (this.level().isClientSide()) {
      this.bookAnimationTick();
      this.oAlpha = this.alpha;
      this.alpha = Mth.approach(this.alpha, this.isPhasing() ? PHASE_ALPHA : 1.0F, PHASE_FADE_PER_TICK);
    }
  }

  private void bookAnimationTick() {
    this.oOpen = this.open;
    this.oFlip = this.flip;

    boolean reading = this.staysStill() || (!this.isAggressive() && this.level().getNearestPlayer(this, READING_DISTANCE) != null);
    if (reading) {
      this.open += 0.1F;
      if (this.open < 0.5F || this.random.nextInt(40) == 0) {
        float old = this.flipT;
        do {
          this.flipT += this.random.nextInt(4) - this.random.nextInt(4);
        } while (old == this.flipT);
      }
    } else {
      this.open -= 0.1F;
    }

    this.open = Mth.clamp(this.open, 0.0F, 1.0F);
    float diff = Mth.clamp((this.flipT - this.flip) * 0.4F, -0.2F, 0.2F);
    this.flipA += (diff - this.flipA) * 0.9F;
    this.flip += this.flipA;
  }

  @Override
  protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
    super.dropCustomDeathLoot(level, source, killedByPlayer);
    if (!this.heldBook.isEmpty()) {
      this.spawnAtLocation(level, this.heldBook);
    } else if (this.random.nextFloat() < LAPIS_BLOCK_CHANCE) {
      this.spawnAtLocation(level, Items.LAPIS_BLOCK);
    } else {
      this.spawnAtLocation(level, new ItemStack(Items.LAPIS_LAZULI, Mth.nextInt(this.random, MIN_LAPIS, MAX_LAPIS)));
    }
  }

  @Override
  public boolean canBeLeashed() {
    return false;
  }

  @Override
  protected SoundEvent getAmbientSound() {
    return SoundEvents.BOOK_PAGE_TURN;
  }

  @Override
  protected SoundEvent getHurtSound(DamageSource source) {
    return SoundEvents.BOOK_PAGE_TURN;
  }

  @Override
  protected SoundEvent getDeathSound() {
    return SoundEvents.BOOK_PUT;
  }

  private enum Home implements StringRepresentable {
    NONE("none"),
    ENCHANTING_TABLE("enchanting_table"),
    BOOKSHELF("bookshelf");

    static final Codec<Home> CODEC = StringRepresentable.fromEnum(Home::values);

    private final String name;

    Home(String name) {
      this.name = name;
    }

    @Override
    public String getSerializedName() {
      return this.name;
    }
  }

  private class ReturnToShelfGoal extends Goal {

    private int returnTicks;

    ReturnToShelfGoal() {
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
      FloatingBook book = FloatingBook.this;
      return book.homeKind == Home.BOOKSHELF && book.getTarget() == null && book.ticksOutside >= book.stayTicks && book.guidedPlayer == null
          && !book.isOnStillTimeout();
    }

    @Override
    public boolean canContinueToUse() {
      return FloatingBook.this.homeKind == Home.BOOKSHELF && FloatingBook.this.getTarget() == null && FloatingBook.this.guidedPlayer == null
          && !FloatingBook.this.supportingPlayer;
    }

    @Override
    public void start() {
      this.returnTicks = 0;
    }

    @Override
    public void stop() {
      FloatingBook.this.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
      return true;
    }

    @Override
    public void tick() {
      FloatingBook book = FloatingBook.this;
      ChiseledBookShelfBlockEntity shelf = book.getHomeShelf();
      if (shelf == null) {
        shelf = book.adoptNearbyShelf();
      }
      if (shelf == null) {
        book.loseHome();
        return;
      }

      Vec3 front = shelfFront(shelf);
      if (book.position().distanceTo(front) < ENTER_DISTANCE || ++this.returnTicks > MAX_RETURN_TICKS) {
        book.enterShelf(shelf);
        return;
      }

      if (book.position().distanceTo(front) < 3.0) {
        book.getNavigation().stop();
        book.getMoveControl().setWantedPosition(front.x, front.y, front.z, 0.8);
      } else if (this.returnTicks % 10 == 1) {
        book.getNavigation().moveTo(front.x, front.y, front.z, 1.0);
      }
    }
  }
}
