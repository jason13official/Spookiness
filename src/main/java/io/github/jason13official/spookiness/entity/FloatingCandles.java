package io.github.jason13official.spookiness.entity;

import io.github.jason13official.spookiness.lighting.LivingLights;
import java.util.List;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
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

public class FloatingCandles extends FloatingPathfinderMob {

  private static final EntityDataAccessor<Integer> DATA_CANDLES = SynchedEntityData.defineId(FloatingCandles.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Integer> DATA_COLOR = SynchedEntityData.defineId(FloatingCandles.class, EntityDataSerializers.INT);

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

  public FloatingCandles(EntityType<? extends FloatingCandles> type, Level level) {
    super(type, level);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return createFloatingAttributes().add(Attributes.MAX_HEALTH, 4.0F).add(Attributes.FLYING_SPEED, 0.12F);
  }

  @Override
  protected void registerGoals() {

    int goalPriority = 1;

    this.goalSelector.addGoal(goalPriority++, new WaterAvoidingRandomFlyingGoal(this, 1.0));
    this.goalSelector.addGoal(goalPriority++, new LookAtPlayerGoal(this, Player.class, 6.0F));
    this.goalSelector.addGoal(goalPriority++, new RandomLookAroundGoal(this));
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder entityData) {
    super.defineSynchedData(entityData);
    entityData.define(DATA_CANDLES, CandleBlock.MIN_CANDLES);
    entityData.define(DATA_COLOR, 0);
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
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    this.setCandles(input.getIntOr("candles", CandleBlock.MIN_CANDLES));
    this.setColor(input.getIntOr("color", 0));
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
  }

  @Override
  public void onRemovedFromLevel() {
    super.onRemovedFromLevel();
    LivingLights.remove(this);
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
}
