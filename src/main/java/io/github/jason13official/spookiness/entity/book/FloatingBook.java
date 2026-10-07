package io.github.jason13official.spookiness.entity.book;

import io.github.jason13official.spookiness.entity.FloatingPathfinderMob;
import io.github.jason13official.spookiness.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundEntityPositionSyncPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FloatingBook extends FloatingPathfinderMob {

  private static final EntityDataAccessor<Boolean> DATA_NEUTRAL = SynchedEntityData.defineId(FloatingBook.class, EntityDataSerializers.BOOLEAN);
  private static final EntityDataAccessor<Boolean> DATA_STAYS_STILL = SynchedEntityData.defineId(FloatingBook.class, EntityDataSerializers.BOOLEAN);
  private static final EntityDataAccessor<Boolean> DATA_PHASING = SynchedEntityData.defineId(FloatingBook.class, EntityDataSerializers.BOOLEAN);

  static final int MAX_STILL_TIMEOUT = 10;

  private static final double READING_DISTANCE = 4.0;
  private static final double SHELF_CLEARANCE = 0.1;
  private static final float LAPIS_BLOCK_CHANCE = 0.25F;
  private static final int MIN_LAPIS = 4;
  private static final int MAX_LAPIS = 10;

  public final BookAnimation animation = new BookAnimation();
  private final BookShelfHome home = new BookShelfHome(this);

  private int serverStillTimeout;
  private boolean supportingPlayer;
  private @Nullable Player guidedPlayer;
  private @Nullable FloatingBook guidePlatform;

  public FloatingBook(EntityType<? extends FloatingBook> type, Level level) {
    super(type, level);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return createFloatingAttributes().add(Attributes.MAX_HEALTH, 10.0F).add(Attributes.ATTACK_DAMAGE, 2.0F).add(Attributes.ATTACK_KNOCKBACK, 1.0F);
  }

  public static Vec3 shelfFront(ChiseledBookShelfBlockEntity shelf) {
    Direction facing = shelf.getBlockState().getValue(ChiseledBookShelfBlock.FACING);
    EntityDimensions size = ModEntities.FLOATING_BOOK.getDimensions();
    double out = 0.5 + size.width() / 2.0 + SHELF_CLEARANCE;
    return Vec3.atCenterOf(shelf.getBlockPos()).add(facing.getStepX() * out, -size.height() / 2.0, facing.getStepZ() * out);
  }

  @Override
  protected void registerGoals() {

    int goalPriority = 1;

    this.goalSelector.addGoal(goalPriority++, new MeleeAttackGoal(this, 1.4, true));
    this.goalSelector.addGoal(goalPriority++, new BookStepGoal(this));
    this.goalSelector.addGoal(goalPriority++, new ReturnToShelfGoal(this));
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

  BookShelfHome home() {
    return this.home;
  }

  public boolean isNeutral() {
    return this.entityData.get(DATA_NEUTRAL);
  }

  void setNeutral(boolean neutral) {
    this.entityData.set(DATA_NEUTRAL, neutral);
  }

  public boolean staysStill() {
    return this.entityData.get(DATA_STAYS_STILL);
  }

  boolean isOnStillTimeout() {
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
    if (this.level().isClientSide() && other.position().y >= this.getBoundingBox().maxY) {
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
    this.home.setEnchantingTable(pos);
  }

  public void setShelfHome(BlockPos pos, int slot, ItemStack book) {
    this.home.setShelf(pos, slot, book);
  }

  @Override
  protected void addAdditionalSaveData(ValueOutput output) {
    super.addAdditionalSaveData(output);
    this.home.save(output);
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    this.home.load(input);
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

    if (this.getTarget() == null && !this.supportingPlayer && this.guidedPlayer == null) {
      this.home.countTimeOutside();
    }
  }

  @Override
  public void die(DamageSource source) {
    super.die(source);
    if (this.level() instanceof ServerLevel level) {
      this.home.onDeath(level);
    }
  }

  @Override
  public void tick() {
    super.tick();
    if (this.level().isClientSide()) {
      boolean reading = this.staysStill() || (!this.isAggressive() && this.level().getNearestPlayer(this, READING_DISTANCE) != null);
      this.animation.tick(this.random, reading, this.isPhasing());
    }
  }

  @Override
  protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
    super.dropCustomDeathLoot(level, source, killedByPlayer);
    ItemStack held = this.home.heldBook();
    if (!held.isEmpty()) {
      this.spawnAtLocation(level, held);
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
}
