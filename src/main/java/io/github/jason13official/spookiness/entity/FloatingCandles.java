package io.github.jason13official.spookiness.entity;

import io.github.jason13official.spookiness.companion.PlayerFollower;
import io.github.jason13official.spookiness.companion.PlayerFollowers;
import io.github.jason13official.spookiness.companion.SpectralCompanions;
import io.github.jason13official.spookiness.effect.SoulBurst;
import io.github.jason13official.spookiness.lighting.LivingLights;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.util.AirRandomPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FloatingCandles extends FloatingPathfinderMob implements PlayerFollower {

  private static final EntityDataAccessor<Integer> DATA_CANDLES = SynchedEntityData.defineId(FloatingCandles.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(FloatingCandles.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Boolean> DATA_FOLLOWING = SynchedEntityData.defineId(FloatingCandles.class, EntityDataSerializers.BOOLEAN);

  public static final int MAX_LINE_LENGTH = 12;
  public static final int COMPANIONS_PER_LINE = 3;
  private static final double LINE_SEARCH_RADIUS = 64.0;
  private static final double PLAYER_SPACING = 1.5;
  private static final double CANDLE_SPACING = 0.8;
  private static final double TELEPORT_DISTANCE = 16.0;
  private static final double AVOID_DISTANCE = 6.0;

  private static final Block[] CANDLE_BLOCKS = {
      Blocks.CANDLE, Blocks.WHITE_CANDLE, Blocks.ORANGE_CANDLE, Blocks.MAGENTA_CANDLE, Blocks.LIGHT_BLUE_CANDLE, Blocks.YELLOW_CANDLE,
      Blocks.LIME_CANDLE, Blocks.PINK_CANDLE, Blocks.GRAY_CANDLE, Blocks.LIGHT_GRAY_CANDLE, Blocks.CYAN_CANDLE, Blocks.PURPLE_CANDLE,
      Blocks.BLUE_CANDLE, Blocks.BROWN_CANDLE, Blocks.GREEN_CANDLE, Blocks.RED_CANDLE, Blocks.BLACK_CANDLE
  };

  private static final List<List<Vec3>> FLAME_OFFSETS = List.of(
      List.of(new Vec3(8.0, 8.0, 8.0)),
      List.of(new Vec3(6.0, 7.0, 8.0), new Vec3(10.0, 8.0, 7.0)),
      List.of(new Vec3(8.0, 5.0, 10.0), new Vec3(6.0, 7.0, 8.0), new Vec3(9.0, 8.0, 7.0)),
      List.of(new Vec3(7.0, 5.0, 9.0), new Vec3(10.0, 7.0, 9.0), new Vec3(6.0, 7.0, 6.0), new Vec3(9.0, 8.0, 6.0))
  );

  private @Nullable EntityReference<Player> owner;
  private int lineIndex;

  public FloatingCandles(EntityType<? extends FloatingCandles> type, Level level) {
    super(type, level);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return createFloatingAttributes().add(Attributes.MAX_HEALTH, 4.0F).add(Attributes.FLYING_SPEED, 0.1F);
  }

  @Override
  protected void registerGoals() {

    int goalPriority = 1;

    this.goalSelector.addGoal(goalPriority++, new FollowLineGoal());
    this.goalSelector.addGoal(goalPriority++, new AvoidPlayerGoal());
    this.goalSelector.addGoal(goalPriority++, new WaterAvoidingRandomFlyingGoal(this, 1.0) {

      @Override
      public boolean canUse() {
        return !FloatingCandles.this.isFollowing() && super.canUse();
      }
    });
    this.goalSelector.addGoal(goalPriority++, new LookAtPlayerGoal(this, Player.class, 6.0F));
    this.goalSelector.addGoal(goalPriority++, new RandomLookAroundGoal(this));
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder entityData) {
    super.defineSynchedData(entityData);
    entityData.define(DATA_CANDLES, CandleBlock.MIN_CANDLES);
    entityData.define(DATA_COLOR, 0);
    entityData.define(DATA_FOLLOWING, false);
  }

  @Override
  public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
    RandomSource random = level.getRandom();
    this.setCandles(Mth.nextInt(random, CandleBlock.MIN_CANDLES, CandleBlock.MAX_CANDLES));
    this.setColor(random.nextInt(CANDLE_BLOCKS.length));
    return super.finalizeSpawn(level, difficulty, spawnReason, groupData);
  }

  @Override
  protected void addAdditionalSaveData(ValueOutput output) {
    super.addAdditionalSaveData(output);
    output.putInt("candles", this.getCandles());
    output.putInt("color", this.getColor());
    EntityReference.store(this.owner, output, "owner");
    output.putInt("line_index", this.lineIndex);
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    this.setCandles(input.getIntOr("candles", CandleBlock.MIN_CANDLES));
    this.setColor(input.getIntOr("color", 0));
    this.owner = EntityReference.read(input, "owner");
    this.lineIndex = input.getIntOr("line_index", 0);
    this.setFollowing(this.owner != null);
  }

  public int getCandles() {
    return this.entityData.get(DATA_CANDLES);
  }

  public void setCandles(int candles) {
    this.entityData.set(DATA_CANDLES, Mth.clamp(candles, CandleBlock.MIN_CANDLES, CandleBlock.MAX_CANDLES));
  }

  public int getColor() {
    return this.entityData.get(DATA_COLOR);
  }

  public void setColor(int color) {
    this.entityData.set(DATA_COLOR, Mth.clamp(color, 0, CANDLE_BLOCKS.length - 1));
  }

  public boolean isFollowing() {
    return this.entityData.get(DATA_FOLLOWING);
  }

  private void setFollowing(boolean following) {
    this.entityData.set(DATA_FOLLOWING, following);
  }

  @Override
  public @Nullable UUID getOwnerUUID() {
    return this.owner == null ? null : this.owner.getUUID();
  }

  public boolean isOwnedBy(Player player) {
    return this.owner != null && this.owner.matches(player);
  }

  private static List<FloatingCandles> getLine(ServerLevel level, Player player) {
    return level.getEntitiesOfClass(FloatingCandles.class, player.getBoundingBox().inflate(LINE_SEARCH_RADIUS), candles -> candles.isAlive() && candles.isOwnedBy(player));
  }

  private Entity getLeader(ServerLevel level, Player player) {
    FloatingCandles leader = null;
    for (FloatingCandles candles : getLine(level, player)) {
      if (candles.lineIndex < this.lineIndex && (leader == null || candles.lineIndex > leader.lineIndex)) {
        leader = candles;
      }
    }
    return leader != null ? leader : player;
  }

  public void joinLine(ServerLevel level, Player player) {
    if (this.isFollowing()) {
      return;
    }

    List<FloatingCandles> line = getLine(level, player);
    if (line.size() >= MAX_LINE_LENGTH) {
      List<FloatingCandles> consumed = new ArrayList<>(line);
      consumed.add(this);
      for (FloatingCandles candles : consumed) {
        SoulBurst.spawn(level, candles.getBoundingBox().getCenter(), 24, 0.3, 0.06);
        candles.dropCandles(level);
        candles.discard();
      }
      SpectralCompanions.summon(level, player, COMPANIONS_PER_LINE);
      return;
    }

    this.owner = EntityReference.of(player);
    this.lineIndex = line.stream().mapToInt(candles -> candles.lineIndex).max().orElse(0) + 1;
    this.setFollowing(true);
    this.setPersistenceRequired();
    this.getNavigation().stop();
    PlayerFollowers.track(this);
    this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 0.8F + line.size() * 0.1F);
  }

  @Override
  protected InteractionResult mobInteract(Player player, InteractionHand hand) {
    if (this.isFollowing()) {
      return super.mobInteract(player, hand);
    }
    if (this.level() instanceof ServerLevel level) {
      this.joinLine(level, player);
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  protected void customServerAiStep(ServerLevel level) {
    super.customServerAiStep(level);
    if (this.isFollowing()) {
      return;
    }
    List<Player> touching = level.getEntitiesOfClass(Player.class, this.getBoundingBox(), player -> player.isAlive() && !player.isSpectator());
    if (!touching.isEmpty()) {
      this.joinLine(level, touching.getFirst());
    }
  }

  public BlockState getCandleState() {
    return CANDLE_BLOCKS[this.getColor()].defaultBlockState().setValue(CandleBlock.CANDLES, this.getCandles()).setValue(CandleBlock.LIT, true);
  }

  private int getLightEmission() {
    return CandleBlock.LIGHT_PER_CANDLE * this.getCandles();
  }

  @Override
  public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
    super.onSyncedDataUpdated(accessor);
    if (DATA_CANDLES.equals(accessor) && this.isAddedToLevel()) {
      LivingLights.remove(this);
      LivingLights.add(this, this.getLightEmission());
    }
  }

  @Override
  public void onAddedToLevel() {
    super.onAddedToLevel();
    LivingLights.add(this, this.getLightEmission());
    PlayerFollowers.track(this);
  }

  @Override
  public void onRemovedFromLevel() {
    super.onRemovedFromLevel();
    LivingLights.remove(this);
    PlayerFollowers.untrack(this);
  }

  @Override
  public void tick() {
    super.tick();
    LivingLights.move(this);
    if (this.level().isClientSide() && this.isAlive()) {
      this.spawnFlameParticles();
    }
  }

  private void spawnFlameParticles() {
    for (Vec3 offset : FLAME_OFFSETS.get(this.getCandles() - 1)) {
      double x = this.getX() - 0.5 + offset.x / 16.0;
      double y = this.getY() + offset.y / 16.0;
      double z = this.getZ() - 0.5 + offset.z / 16.0;
      if (this.random.nextInt(4) == 0) {
        this.level().addParticle(ParticleTypes.SMALL_FLAME, x, y, z, 0.0, 0.0, 0.0);
      }
      if (this.random.nextInt(20) == 0) {
        this.level().addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
      }
    }
  }

  @Override
  public boolean isSensitiveToWater() {
    return true;
  }

  @Override
  protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
    super.dropCustomDeathLoot(level, source, killedByPlayer);
    this.dropCandles(level);
  }

  private void dropCandles(ServerLevel level) {
    this.spawnAtLocation(level, new ItemStack(CANDLE_BLOCKS[this.getColor()], this.getCandles()));
  }

  @Override
  public boolean canBeLeashed() {
    return false;
  }

  @Override
  protected SoundEvent getAmbientSound() {
    return SoundEvents.CANDLE_AMBIENT;
  }

  @Override
  protected SoundEvent getHurtSound(DamageSource source) {
    return SoundEvents.CANDLE_EXTINGUISH;
  }

  @Override
  protected SoundEvent getDeathSound() {
    return SoundEvents.CANDLE_EXTINGUISH;
  }

  private class FollowLineGoal extends Goal {

    private @Nullable Player player;

    FollowLineGoal() {
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
      FloatingCandles candles = FloatingCandles.this;
      if (!candles.isFollowing() || candles.owner == null) {
        return false;
      }
      this.player = EntityReference.getPlayer(candles.owner, candles.level());
      return this.player != null && !this.player.isSpectator();
    }

    @Override
    public boolean canContinueToUse() {
      return this.player != null && this.player.isAlive() && !this.player.isRemoved() && !this.player.isSpectator();
    }

    @Override
    public void stop() {
      this.player = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
      return true;
    }

    @Override
    public void tick() {
      FloatingCandles candles = FloatingCandles.this;
      if (this.player == null || !(candles.level() instanceof ServerLevel level)) {
        return;
      }

      Entity leader = candles.getLeader(level, this.player);
      boolean ledByPlayer = leader == this.player;
      Vec3 anchor = ledByPlayer ? leader.position().add(0.0, 1.2, 0.0) : leader.position();
      Vec3 offset = candles.position().subtract(anchor);
      double distance = offset.length();
      double spacing = ledByPlayer ? PLAYER_SPACING : CANDLE_SPACING;

      candles.getLookControl().setLookAt(leader);

      if (distance > TELEPORT_DISTANCE) {
        candles.teleportTo(anchor.x, anchor.y, anchor.z);
        candles.setDeltaMovement(Vec3.ZERO);
        return;
      }

      if (distance <= spacing) {
        return;
      }

      Vec3 target = anchor.add(offset.scale(spacing / distance));
      double speed = Mth.clamp((distance - spacing) * 2.0, 0.5, 8.0);
      candles.getMoveControl().setWantedPosition(target.x, target.y, target.z, speed);
    }
  }

  private class AvoidPlayerGoal extends Goal {

    private @Nullable Vec3 fleeTo;

    AvoidPlayerGoal() {
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
      FloatingCandles candles = FloatingCandles.this;
      if (candles.isFollowing()) {
        return false;
      }

      Player player = candles.level().getNearestPlayer(candles, AVOID_DISTANCE);
      if (player == null || player.isSpectator()) {
        return false;
      }

      Vec3 away = candles.position().subtract(player.position()).normalize().scale(AVOID_DISTANCE);
      this.fleeTo = AirRandomPos.getPosTowards(candles, 8, 4, 0, candles.position().add(away), Math.PI / 2.0);
      return this.fleeTo != null;
    }

    @Override
    public boolean canContinueToUse() {
      return !FloatingCandles.this.isFollowing() && !FloatingCandles.this.getNavigation().isDone();
    }

    @Override
    public void start() {
      if (this.fleeTo != null) {
        FloatingCandles.this.getNavigation().moveTo(this.fleeTo.x, this.fleeTo.y, this.fleeTo.z, 2.0);
      }
    }

    @Override
    public void stop() {
      this.fleeTo = null;
    }
  }
}
