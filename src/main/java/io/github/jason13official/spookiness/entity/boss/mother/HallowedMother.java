package io.github.jason13official.spookiness.entity.boss.mother;

import io.github.jason13official.spookiness.entity.boss.SpookyBoss;
import io.github.jason13official.spookiness.util.Spawning;
import io.github.jason13official.spookiness.world.SpookyTime;
import io.github.jason13official.spookiness.util.SpookyMath;
import net.minecraft.sounds.SoundEvent;
import io.github.jason13official.spookiness.registry.ModSounds;
import io.github.jason13official.spookiness.lighting.LanternHeads;
import io.github.jason13official.spookiness.lighting.LightEmitter;
import net.minecraft.ChatFormatting;
import io.github.jason13official.spookiness.companion.Hallowing;
import io.github.jason13official.spookiness.companion.PlayerFollowers;
import io.github.jason13official.spookiness.effect.Particles;
import io.github.jason13official.spookiness.entity.JackOMimic;
import io.github.jason13official.spookiness.registry.ModEntities;
import io.github.jason13official.spookiness.registry.ModItems;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class HallowedMother extends SpookyBoss implements LightEmitter {

  public static final float SCALE = 4.0F;

  private static final EntityDataAccessor<Integer> DATA_TETHER = SynchedEntityData.defineId(HallowedMother.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Boolean> DATA_GAPING = SynchedEntityData.defineId(HallowedMother.class, EntityDataSerializers.BOOLEAN);

  private static final int NO_TETHER = -1;
  static final byte SPIT_EVENT = 4;
  static final int MAX_GOURDLINGS = 8;
  private static final double FLING_SPEED = 0.9;
  private static final double FLING_LIFT = 0.55;
  private static final double MOUTH_FORWARD = 2.0;
  private static final double MOUTH_HEIGHT = 1.0;
  private static final float TURN_SPEED = 6.0F;
  private static final int BROOD_INTERVAL = 10;
  private static final float GAPE_SPEED = 0.15F;
  private static final int CLOUD_INTERVAL = 160;
  private static final float CLOUD_RADIUS = 3.0F;
  private static final int CLOUD_DURATION = 140;
  private static final double CLOUD_MIN_DISTANCE = 4.0;
  private static final double CLOUD_MAX_DISTANCE = 10.0;
  static final int CLOUD_ORANGE = 0xFF8A1E;
  static final int CLOUD_GREEN = 0x6BCB3A;
  private static final float PROJECTILE_MULTIPLIER = 0.4F;
  private static final float MACE_MULTIPLIER = 2.0F;
  private static final int BROOD_PARTICLE_INTERVAL = 4;

  private @Nullable UUID summoner;
  private final BroodTracker brood = new BroodTracker(this);
  private final MotherTether tether = new MotherTether(this);
  private final MotherVolley volley = new MotherVolley(this);
  private final MotherBurst burst = new MotherBurst(this);
  private final MotherFeast feast = new MotherFeast(this);
  private float gape;
  private float oGape;

  public final AnimationState spitAnimationState = new AnimationState();

  public HallowedMother(EntityType<? extends HallowedMother> type, Level level) {
    super(type, level, BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_10);
    this.xpReward = 120;
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1600.0).add(Attributes.ARMOR, 10.0).add(Attributes.ATTACK_DAMAGE, 10.0)
        .add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
  }

  public static @Nullable HallowedMother erupt(ServerLevel level, ServerPlayer summoner, Vec3 pos) {

    HallowedMother mother = Spawning.spawn(level, ModEntities.HALLOWED_MOTHER, EntitySpawnReason.EVENT, pos, SpookyMath.randomYaw(level.getRandom()),
        spawned -> spawned.summoner = summoner.getUUID());
    if (mother == null) {
      return null;
    }
    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ROOTED_DIRT.defaultBlockState()), pos.x, pos.y + 0.5, pos.z, 120, 2.0, 0.5, 2.0, 0.2);
    level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.WARDEN_EMERGE, SoundSource.HOSTILE, 2.0F, 0.7F);
    return mother;
  }

  @Override
  protected void registerGoals() {

    this.goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 24.0F));

    this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, false));
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder entityData) {
    super.defineSynchedData(entityData);
    entityData.define(DATA_TETHER, NO_TETHER);
    entityData.define(DATA_GAPING, false);
  }

  @Override
  public int getLightEmission() {
    return LanternHeads.EMISSION;
  }

  public @Nullable Entity getTethered() {
    int id = this.entityData.get(DATA_TETHER);
    return id == NO_TETHER ? null : this.level().getEntity(id);
  }

  void setGaping(boolean gaping) {
    this.entityData.set(DATA_GAPING, gaping);
  }

  boolean hasTether() {
    return this.entityData.get(DATA_TETHER) != NO_TETHER;
  }

  void setTethered(@Nullable Entity entity) {
    this.entityData.set(DATA_TETHER, entity == null ? NO_TETHER : entity.getId());
  }

  BroodTracker brood() {
    return this.brood;
  }

  boolean isBrood(Entity entity) {
    return this.brood.contains(entity);
  }

  void cancelVolley() {
    this.volley.cancel();
  }

  void releaseTether() {
    this.tether.release();
  }

  @Override
  protected void customServerAiStep(ServerLevel level) {
    super.customServerAiStep(level);

    if (SpookyTime.isAfternoon(SpookyTime.timeOfDay(level))) {
      this.sink(level, "message.spookiness.mother_sinks_dawn");
      return;
    }

    if (this.tickCount % 20 == 0 && this.summoner != null && level.getServer().getPlayerList().getPlayer(this.summoner) instanceof ServerPlayer owner
        && Hallowing.count(owner) == 0 && this.getTethered() == null) {
      this.sink(level, "message.spookiness.mother_sinks_lost");
      return;
    }

    if (this.tickCount % BROOD_INTERVAL == 0) {
      this.brood.command(level);
    }
    if (this.tickCount % BROOD_PARTICLE_INTERVAL == 0) {
      this.brood.emitParticles(level);
    }

    LivingEntity target = this.getTarget();
    if (target != null) {
      this.faceTarget(target);
    }

    if (target != null && this.tickCount % CLOUD_INTERVAL == 0) {
      this.spawnCloud(level);
    }

    if (this.burst.isActive()) {
      this.burst.tick(level, target);
      return;
    }
    if (this.feast.isActive()) {
      this.feast.tick(level);
      return;
    }
    if (this.burst.tryStart(level) || this.feast.tryStart(level)) {
      return;
    }

    this.tether.tick(level);
    if (target != null) {
      this.volley.tick(level, target);
    }
  }

  @Override
  public void aiStep() {
    super.aiStep();
    if (this.level().isClientSide()) {
      this.oGape = this.gape;
      this.gape = Mth.approach(this.gape, this.entityData.get(DATA_GAPING) ? 1.0F : 0.0F, GAPE_SPEED);
    }
  }

  public float getGape(float partialTicks) {
    return Mth.lerp(partialTicks, this.oGape, this.gape);
  }

  private void spawnCloud(ServerLevel level) {

    double distance = CLOUD_MIN_DISTANCE + this.random.nextDouble() * (CLOUD_MAX_DISTANCE - CLOUD_MIN_DISTANCE);
    Vec3 spot = SpookyMath.onRing(this.position(), SpookyMath.randomAngle(this.random), distance);
    AreaEffectCloud cloud = new AreaEffectCloud(level, spot.x, spot.y, spot.z);
    cloud.setOwner(this);
    cloud.setRadius(CLOUD_RADIUS);
    cloud.setDuration(CLOUD_DURATION);
    cloud.setRadiusPerTick(-CLOUD_RADIUS / CLOUD_DURATION);
    cloud.setWaitTime(10);
    cloud.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0));
    cloud.setCustomParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, this.random.nextBoolean() ? CLOUD_ORANGE : CLOUD_GREEN));
    level.addFreshEntity(cloud);
  }

  public Vec3 mouthOffset(float bodyYRot) {

    float radians = bodyYRot * Mth.DEG_TO_RAD;
    return new Vec3(-Mth.sin(radians) * MOUTH_FORWARD, MOUTH_HEIGHT, Mth.cos(radians) * MOUTH_FORWARD);
  }

  private void faceTarget(LivingEntity target) {

    float wanted = SpookyMath.yawToward(target.position().subtract(this.position()));
    float yRot = Mth.approachDegrees(this.getYRot(), wanted, TURN_SPEED);
    this.setYRot(yRot);
    this.yBodyRot = yRot;
    this.yHeadRot = yRot;
  }

  void spit(ServerLevel level, @Nullable LivingEntity target, float spread) {

    level.broadcastEntityEvent(this, SPIT_EVENT);

    Vec3 mouth = this.position().add(this.mouthOffset(this.yBodyRot));
    Vec3 aim = target == null ? Vec3.ZERO : target.position().subtract(mouth).multiply(1.0, 0.0, 1.0);
    Vec3 direction = (aim.lengthSqr() > 1.0E-4 ? aim.normalize() : Vec3.directionFromRotation(0.0F, this.yBodyRot))
        .yRot((this.random.nextFloat() - 0.5F) * 2.0F * spread * Mth.DEG_TO_RAD);

    JackOMimic gourdling = Spawning.spawn(level, ModEntities.JACK_O_MIMIC, EntitySpawnReason.MOB_SUMMONED, mouth, this.yBodyRot, spawned -> {
      spawned.setDeltaMovement(direction.scale(FLING_SPEED).add(0.0, FLING_LIFT, 0.0));
      MotherBrood.adopt(spawned);
      spawned.setTarget(this.brood.target());
    });
    if (gourdling == null) {
      return;
    }
    this.brood.add(gourdling);

    level.sendParticles(ParticleTypes.FLAME, mouth.x, mouth.y, mouth.z, 12, 0.3, 0.3, 0.3, 0.05);
    this.playSound(SoundEvents.LLAMA_SPIT, 2.0F, 0.4F);
    this.playSound(SoundEvents.SLIME_JUMP, 2.0F, 0.5F);
  }

  @Override
  public void handleEntityEvent(byte id) {
    if (id == SPIT_EVENT) {
      this.spitAnimationState.start(this.tickCount);
    } else {
      super.handleEntityEvent(id);
    }
  }

  private void sink(ServerLevel level, String message) {

    for (ServerPlayer player : this.bossEvent.getPlayers()) {
      player.sendSystemMessage(Component.translatable(message).withStyle(ChatFormatting.GOLD, ChatFormatting.ITALIC));
    }
    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ROOTED_DIRT.defaultBlockState()), this.getX(), this.getY() + 0.5, this.getZ(), 120, 2.0,
        0.5, 2.0, 0.2);
    level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WARDEN_DIG, SoundSource.HOSTILE, 2.0F, 0.7F);
    this.discard();
  }

  @Override
  public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {

    ItemStack weapon = source.getWeaponItem();
    if (weapon != null && weapon.is(ModItems.PUMPKIN_MACE)) {
      damage *= MACE_MULTIPLIER;
      this.feast.spitEarly();
      if (this.getTethered() != null) {
        this.releaseTether();
      }
    }
    if (source.is(DamageTypeTags.IS_PROJECTILE)) {
      damage *= PROJECTILE_MULTIPLIER;
    }
    return super.hurtServer(level, source, damage);
  }

  @Override
  protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
    super.dropCustomDeathLoot(level, source, killedByPlayer);

    if (this.summoner != null && level.getServer().getPlayerList().getPlayer(this.summoner) instanceof ServerPlayer owner) {
      for (Mob ally : PlayerFollowers.followers(owner, Hallowing::isHallowed)) {
        Hallowing.bless(ally);
        Particles.soulBurst(level, ally.getBoundingBox().getCenter(), 24, 0.3, 0.06);
      }
    }
  }

  @Override
  protected SoundEvent getAmbientSound() {
    return ModSounds.HALLOWED_MOTHER_AMBIENT;
  }

  @Override
  public int getAmbientSoundInterval() {
    return 200;
  }

  @Override
  protected SoundEvent getHurtSound(DamageSource source) {
    return ModSounds.HALLOWED_MOTHER_HURT;
  }

  @Override
  protected SoundEvent getDeathSound() {
    return ModSounds.HALLOWED_MOTHER_DEATH;
  }

  @Override
  protected boolean isAnchored() {
    return true;
  }

  @Override
  protected void addAdditionalSaveData(ValueOutput output) {
    super.addAdditionalSaveData(output);
    output.storeNullable("summoner", UUIDUtil.CODEC, this.summoner);
    this.brood.save(output);
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    this.summoner = input.read("summoner", UUIDUtil.CODEC).orElse(null);
    this.brood.load(input);
  }
}
