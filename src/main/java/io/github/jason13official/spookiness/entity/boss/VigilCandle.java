package io.github.jason13official.spookiness.entity.boss;

import io.github.jason13official.spookiness.lighting.LightEmitter;
import io.github.jason13official.spookiness.registry.ModEntities;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class VigilCandle extends Entity implements LightEmitter {

  private static final EntityDataAccessor<Boolean> DATA_FROST = SynchedEntityData.defineId(VigilCandle.class, EntityDataSerializers.BOOLEAN);
  private static final EntityDataAccessor<Boolean> DATA_GREAT = SynchedEntityData.defineId(VigilCandle.class, EntityDataSerializers.BOOLEAN);

  private static final int HEAL_INTERVAL = 20;
  private static final float HEAL_AMOUNT = 2.0F;
  private static final double HEAL_RANGE = 24.0;
  private static final double GREAT_HEAL_RANGE = 96.0;
  public static final float GREAT_SCALE = 2.5F;
  public static final float BASE_HEIGHT = 0.4F;
  public static final int CANDLES = 3;
  public static final int GREAT_CANDLES = 4;

  private static final List<Vec3> WICKS = List.of(new Vec3(8.0, 5.0, 10.0).scale(0.0625), new Vec3(6.0, 7.0, 8.0).scale(0.0625),
      new Vec3(9.0, 8.0, 7.0).scale(0.0625));
  private static final List<Vec3> GREAT_WICKS = List.of(new Vec3(7.0, 5.0, 9.0).scale(0.0625), new Vec3(10.0, 7.0, 9.0).scale(0.0625),
      new Vec3(6.0, 7.0, 6.0).scale(0.0625), new Vec3(9.0, 8.0, 6.0).scale(0.0625));

  private @Nullable UUID keeper;

  public VigilCandle(EntityType<? extends VigilCandle> type, Level level) {
    super(type, level);
  }

  public static @Nullable VigilCandle plant(ServerLevel level, LivingEntity keeper, Vec3 pos, boolean frost, boolean great) {

    VigilCandle candle = ModEntities.VIGIL_CANDLE.create(level, EntitySpawnReason.MOB_SUMMONED);
    if (candle == null) {
      return null;
    }
    candle.keeper = keeper.getUUID();
    candle.entityData.set(DATA_FROST, frost);
    candle.entityData.set(DATA_GREAT, great);
    candle.refreshDimensions();
    candle.snapTo(pos.x, pos.y, pos.z, 0.0F, 0.0F);
    level.addFreshEntity(candle);
    level.sendParticles(frost ? ParticleTypes.SNOWFLAKE : ParticleTypes.FLAME, pos.x, pos.y + 0.5, pos.z, 12, 0.2, 0.3, 0.2, 0.02);
    return candle;
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder entityData) {
    entityData.define(DATA_FROST, false);
    entityData.define(DATA_GREAT, false);
  }

  public boolean isFrost() {
    return this.entityData.get(DATA_FROST);
  }

  public boolean isGreat() {
    return this.entityData.get(DATA_GREAT);
  }

  @Override
  public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
    super.onSyncedDataUpdated(accessor);
    if (DATA_GREAT.equals(accessor)) {
      this.refreshDimensions();
    }
  }

  public int getCandles() {
    return this.isGreat() ? GREAT_CANDLES : CANDLES;
  }

  public float getRenderScale() {
    return this.isGreat() ? GREAT_SCALE : 1.0F;
  }

  @Override
  public int getLightEmission() {
    return CandleBlock.LIGHT_PER_CANDLE * this.getCandles();
  }

  private void flicker() {

    float scale = this.getRenderScale();
    for (Vec3 wick : this.isGreat() ? GREAT_WICKS : WICKS) {
      double x = this.getX() + (wick.x - 0.5) * scale;
      double y = this.getY() + BASE_HEIGHT * scale + wick.y * scale;
      double z = this.getZ() + (wick.z - 0.5) * scale;
      if (this.random.nextInt(3) == 0) {
        this.level().addParticle(this.isFrost() ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME, x, y, z, 0.0, 0.0, 0.0);
      }
      if (this.random.nextInt(20) == 0) {
        this.level().addParticle(ParticleTypes.SMOKE, x, y, z, 0.0, 0.0, 0.0);
      }
    }
  }

  @Override
  public EntityDimensions getDimensions(Pose pose) {
    EntityDimensions dimensions = super.getDimensions(pose);
    return this.isGreat() ? dimensions.scale(GREAT_SCALE) : dimensions;
  }

  @Override
  public void tick() {
    super.tick();

    if (this.level().isClientSide()) {
      this.flicker();
      return;
    }

    if (this.tickCount % HEAL_INTERVAL != 0 || !(this.level() instanceof ServerLevel level)) {
      return;
    }
    Entity keeper = this.keeper == null ? null : level.getEntity(this.keeper);
    if (!(keeper instanceof LivingEntity living) || !living.isAlive()) {
      this.snuff(level);
      return;
    }
    double range = this.isGreat() ? GREAT_HEAL_RANGE : HEAL_RANGE;
    if (living.distanceToSqr(this) <= range * range && living.getHealth() < living.getMaxHealth()) {
      living.heal(HEAL_AMOUNT);
      level.sendParticles(this.isFrost() ? ParticleTypes.SNOWFLAKE : ParticleTypes.FLAME, this.getX(), this.getBoundingBox().maxY, this.getZ(), 4, 0.1, 0.1, 0.1,
          0.02);
    }
  }

  public void snuff(ServerLevel level) {

    level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getBoundingBox().maxY, this.getZ(), 12, 0.2, 0.2, 0.2, 0.02);
    level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.CANDLE_EXTINGUISH, SoundSource.HOSTILE, 1.5F, 0.8F);
    this.discard();
  }

  @Override
  public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
    if (this.isInvulnerableToBase(source)) {
      return false;
    }
    this.snuff(level);
    return true;
  }

  @Override
  public boolean isPickable() {
    return true;
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    this.keeper = input.read("keeper", UUIDUtil.CODEC).orElse(null);
    this.entityData.set(DATA_FROST, input.getBooleanOr("frost", false));
    this.entityData.set(DATA_GREAT, input.getBooleanOr("great", false));
  }

  @Override
  protected void addAdditionalSaveData(ValueOutput output) {
    output.storeNullable("keeper", UUIDUtil.CODEC, this.keeper);
    output.putBoolean("frost", this.isFrost());
    output.putBoolean("great", this.isGreat());
  }
}
