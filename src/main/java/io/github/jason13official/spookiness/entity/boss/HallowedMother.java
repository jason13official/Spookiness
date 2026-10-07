package io.github.jason13official.spookiness.entity.boss;

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
import io.github.jason13official.spookiness.entity.SpectralJackOMimic;
import io.github.jason13official.spookiness.registry.ModEntities;
import io.github.jason13official.spookiness.registry.ModItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
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
import net.minecraft.world.entity.animal.golem.IronGolem;
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

public class HallowedMother extends Monster implements LightEmitter {

  public static final float SCALE = 4.0F;

  private static final EntityDataAccessor<Integer> DATA_TETHER = SynchedEntityData.defineId(HallowedMother.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Boolean> DATA_GAPING = SynchedEntityData.defineId(HallowedMother.class, EntityDataSerializers.BOOLEAN);

  private static final int NO_TETHER = -1;
  private static final int RECLAIM_INTERVAL = 160;
  private static final int RECLAIM_TICKS = 80;
  private static final double RECLAIM_RANGE = 24.0;
  private static final double TETHER_PULL = 0.15;
  private static final double HOLD_DISTANCE = 4.5;
  private static final double HOLD_TOLERANCE = 0.4;
  private static final float RECLAIM_HEAL = 40.0F;
  private static final byte SPIT_EVENT = 4;
  private static final int VOLLEY_INTERVAL = 140;
  private static final int VOLLEY_SIZE = 3;
  private static final int SPIT_GAP = 12;
  private static final int MAX_GOURDLINGS = 8;
  private static final double GOURDLING_RANGE = 32.0;
  private static final double FLING_SPEED = 0.9;
  private static final double FLING_LIFT = 0.55;
  private static final float FLING_SPREAD = 30.0F;
  private static final double MOUTH_FORWARD = 2.0;
  private static final double MOUTH_HEIGHT = 1.0;
  private static final float TURN_SPEED = 6.0F;
  private static final int BROOD_INTERVAL = 10;
  private static final int RETALIATE_TICKS = 100;
  private static final double CROWD_RANGE = 5.0;
  private static final int CROWD_COUNT = 4;
  private static final int BURST_COOLDOWN = 500;
  private static final int BURST_SPIT_GAP = 4;
  private static final int BURST_MAX_SPITS = MAX_GOURDLINGS;
  private static final int GATHER_TICKS = 40;
  private static final double GATHER_PULL = 0.35;
  private static final double GATHER_STOP = 1.5;
  private static final double BLAST_RADIUS = 10.0;
  private static final double BLAST_SPEED = 1.0;
  private static final double BLAST_LIFT = 0.75;
  private static final float BLAST_DAMAGE = 6.0F;
  private static final float GAPE_SPEED = 0.15F;
  private static final int FEAST_COOLDOWN = 400;
  private static final double FEAST_RANGE = 16.0;
  private static final float FEAST_MAX_WIDTH = 1.5F;
  private static final int INHALE_TICKS = 40;
  private static final double INHALE_PULL = 0.5;
  private static final double SWALLOW_DISTANCE = 1.5;
  private static final int DIGEST_TICKS = 120;
  private static final int DIGEST_INTERVAL = 30;
  private static final float DIGEST_DAMAGE = 2.0F;
  private static final int DIGEST_POISON_TICKS = 60;
  private static final double BELLY_FORWARD = 0.6;
  private static final double BELLY_HEIGHT = 1.0;
  private static final double SPIT_OUT_SPEED = 1.4;
  private static final double SPIT_OUT_LIFT = 0.5;
  private static final int CLOUD_INTERVAL = 160;
  private static final float CLOUD_RADIUS = 3.0F;
  private static final int CLOUD_DURATION = 140;
  private static final double CLOUD_MIN_DISTANCE = 4.0;
  private static final double CLOUD_MAX_DISTANCE = 10.0;
  private static final int CLOUD_ORANGE = 0xFF8A1E;
  private static final int CLOUD_GREEN = 0x6BCB3A;
  private static final float PROJECTILE_MULTIPLIER = 0.4F;
  private static final float MACE_MULTIPLIER = 2.0F;
  private static final int BROOD_PARTICLE_INTERVAL = 4;

  private final ServerBossEvent bossEvent = new ServerBossEvent(UUID.randomUUID(), this.getDisplayName(), BossEvent.BossBarColor.PURPLE,
      BossEvent.BossBarOverlay.NOTCHED_10);

  private @Nullable UUID summoner;
  private int tetherTicks;
  private int reclaimCooldown = RECLAIM_INTERVAL;
  private int volleyCooldown = 60;
  private int volleyRemaining;
  private int spitTimer;

  private final Set<UUID> brood = new HashSet<>();
  private Burst burst = Burst.NONE;
  private int burstTicks;
  private int burstSpits;
  private int burstCooldown = BURST_COOLDOWN / 5;
  private float gape;
  private Feast feast = Feast.NONE;
  private int feastTicks;
  private int feastCooldown = FEAST_COOLDOWN / 2;
  private @Nullable UUID meal;
  private boolean spitMealEarly;
  private float oGape;

  public final AnimationState spitAnimationState = new AnimationState();

  public HallowedMother(EntityType<? extends HallowedMother> type, Level level) {
    super(type, level);
    this.xpReward = 120;
    this.setPersistenceRequired();
    this.bossEvent.setCreateWorldFog(true);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1600.0).add(Attributes.ARMOR, 10.0).add(Attributes.ATTACK_DAMAGE, 10.0)
        .add(Attributes.MOVEMENT_SPEED, 0.0).add(Attributes.FOLLOW_RANGE, 32.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
  }

  public static @Nullable HallowedMother erupt(ServerLevel level, ServerPlayer summoner, Vec3 pos) {

    HallowedMother mother = ModEntities.HALLOWED_MOTHER.create(level, EntitySpawnReason.EVENT);
    if (mother == null) {
      return null;
    }
    mother.summoner = summoner.getUUID();
    mother.snapTo(pos.x, pos.y, pos.z, mother.random.nextFloat() * 360.0F, 0.0F);
    level.addFreshEntity(mother);
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

  public boolean isEnraged() {
    return this.getHealth() < this.getMaxHealth() * 0.5F;
  }

  public void releaseTether() {

    Entity tethered = this.getTethered();
    if (tethered != null && this.level() instanceof ServerLevel level) {
      level.sendParticles(ParticleTypes.SOUL, tethered.getX(), tethered.getY() + 1.0, tethered.getZ(), 16, 0.3, 0.3, 0.3, 0.05);
    }
    this.entityData.set(DATA_TETHER, NO_TETHER);
    this.tetherTicks = 0;
  }

  @Override
  protected void customServerAiStep(ServerLevel level) {
    super.customServerAiStep(level);
    this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

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
      this.commandBrood(level);
    }
    if (this.tickCount % BROOD_PARTICLE_INTERVAL == 0) {
      for (Mob mimic : this.livingBrood(level)) {
        level.sendParticles(this.random.nextBoolean() ? ParticleTypes.SMALL_FLAME : ParticleTypes.SMOKE, mimic.getX(), mimic.getY() + mimic.getBbHeight(), mimic.getZ(),
            1, 0.2, 0.1, 0.2, 0.01);
      }
    }

    LivingEntity target = this.getTarget();
    if (target != null) {
      this.faceTarget(target);
    }

    if (target != null && this.tickCount % CLOUD_INTERVAL == 0) {
      this.spawnCloud(level);
    }

    if (this.burst != Burst.NONE) {
      this.tickBurst(level, target);
      return;
    }
    if (this.feast != Feast.NONE) {
      this.tickFeast(level);
      return;
    }
    if (--this.burstCooldown <= 0 && this.tickCount % BROOD_INTERVAL == 0 && this.isCrowded(level)) {
      this.startBurst();
      return;
    }
    if (--this.feastCooldown <= 0 && this.tickCount % BROOD_INTERVAL == 0 && this.startFeast(level)) {
      return;
    }

    this.tickReclaim(level);
    if (target != null) {
      this.tickVolley(level, target);
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

  private List<Mob> livingBrood(ServerLevel level) {

    List<Mob> living = new ArrayList<>();
    this.brood.removeIf(id -> {
      if (level.getEntity(id) instanceof Mob mimic && mimic.isAlive()) {
        living.add(mimic);
        return false;
      }
      return true;
    });
    return living;
  }

  private boolean isBrood(Entity entity) {
    return this.brood.contains(entity.getUUID());
  }

  private @Nullable LivingEntity broodTarget() {

    LivingEntity attacker = this.getLastHurtByMob();
    if (attacker != null && attacker.isAlive() && !this.isBrood(attacker) && this.tickCount - this.getLastHurtByMobTimestamp() < RETALIATE_TICKS) {
      return attacker;
    }
    return this.getTarget();
  }

  private void commandBrood(ServerLevel level) {

    LivingEntity target = this.broodTarget();
    if (target == null) {
      return;
    }
    for (Mob mimic : this.livingBrood(level)) {
      if (mimic.getTarget() != target) {
        mimic.setTarget(target);
      }
    }
  }

  private boolean isCrowded(ServerLevel level) {

    return level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(CROWD_RANGE),
        entity -> entity != this && entity.isAlive() && !(entity instanceof JackOMimic) && !this.isBrood(entity)
            && !(entity instanceof Player player && (player.isSpectator() || player.isCreative())))
        .size() >= CROWD_COUNT;
  }

  private Vec3 bellyPosition() {

    float radians = this.yBodyRot * Mth.DEG_TO_RAD;
    return this.position().add(-Mth.sin(radians) * BELLY_FORWARD, BELLY_HEIGHT, Mth.cos(radians) * BELLY_FORWARD);
  }

  private boolean isEnemy(LivingEntity entity) {

    if (entity == this || !entity.isAlive() || entity instanceof JackOMimic || this.isBrood(entity) || entity.getBbWidth() > FEAST_MAX_WIDTH) {
      return false;
    }
    if (entity instanceof Player player) {
      return !player.isSpectator() && !player.isCreative();
    }
    return Hallowing.isHallowed(entity) || entity instanceof IronGolem || entity instanceof SpectralJackOMimic
        || entity instanceof Mob mob && mob.getTarget() == this;
  }

  private boolean startFeast(ServerLevel level) {

    LivingEntity prey = level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(FEAST_RANGE), this::isEnemy).stream()
        .min(Comparator.comparingDouble((LivingEntity entity) -> -entity.getHealth()).thenComparingDouble(this::distanceToSqr))
        .orElse(null);
    if (prey == null) {
      return false;
    }
    this.releaseTether();
    this.volleyRemaining = 0;
    this.meal = prey.getUUID();
    this.feast = Feast.INHALING;
    this.feastTicks = 0;
    this.spitMealEarly = false;
    this.entityData.set(DATA_GAPING, true);
    this.playSound(SoundEvents.WARDEN_SNIFF, 3.0F, 0.5F);
    this.playSound(SoundEvents.BREEZE_IDLE_GROUND, 1.5F, 0.6F);
    return true;
  }

  private @Nullable LivingEntity getMeal(ServerLevel level) {
    return this.meal != null && level.getEntity(this.meal) instanceof LivingEntity prey && prey.isAlive() ? prey : null;
  }

  private void tickFeast(ServerLevel level) {

    this.feastTicks++;
    LivingEntity prey = this.getMeal(level);
    if (prey == null || prey instanceof Player player && (player.isSpectator() || player.isCreative())) {
      this.endFeast();
      return;
    }

    Vec3 belly = this.bellyPosition();
    if (this.feast == Feast.INHALING) {
      Vec3 toBelly = belly.subtract(prey.position());
      if (toBelly.length() > FEAST_RANGE + 4.0) {
        this.endFeast();
        return;
      }
      Vec3 pull = toBelly.normalize().scale(Math.min(INHALE_PULL, toBelly.length()));
      prey.setDeltaMovement(pull);
      prey.hurtMarked = true;
      level.sendParticles(ParticleTypes.CLOUD, prey.getX(), prey.getY() + prey.getBbHeight() * 0.5, prey.getZ(), 3, 0.3, 0.3, 0.3, 0.02);
      if (toBelly.length() < SWALLOW_DISTANCE || this.feastTicks >= INHALE_TICKS) {
        this.feast = Feast.DIGESTING;
        this.feastTicks = 0;
        this.playSound(SoundEvents.GENERIC_EAT.value(), 3.0F, 0.5F);
      }
      return;
    }

    prey.setDeltaMovement(Vec3.ZERO);
    prey.teleportTo(belly.x, belly.y, belly.z);
    prey.fallDistance = 0.0;
    if (this.feastTicks % DIGEST_INTERVAL == 0) {
      prey.addEffect(new MobEffectInstance(MobEffects.POISON, DIGEST_POISON_TICKS, 1), this);
      prey.hurtServer(level, this.damageSources().indirectMagic(this, this), DIGEST_DAMAGE);
      level.broadcastEntityEvent(this, SPIT_EVENT);
      level.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, CLOUD_GREEN), belly.x, belly.y + 1.0, belly.z, 16, 0.6, 0.4, 0.6, 0.0);
      this.playSound(SoundEvents.GENERIC_EAT.value(), 2.0F, 0.6F);
    }
    if (this.feastTicks >= DIGEST_TICKS || this.spitMealEarly) {
      this.spitOut(level, prey);
    }
  }

  private void spitOut(ServerLevel level, LivingEntity prey) {

    float radians = this.yBodyRot * Mth.DEG_TO_RAD;
    Vec3 forward = new Vec3(-Mth.sin(radians), 0.0, Mth.cos(radians));
    Vec3 mouth = this.position().add(this.mouthOffset(this.yBodyRot));
    prey.teleportTo(mouth.x, mouth.y, mouth.z);
    prey.setDeltaMovement(forward.x * SPIT_OUT_SPEED, SPIT_OUT_LIFT, forward.z * SPIT_OUT_SPEED);
    prey.hurtMarked = true;
    level.sendParticles(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, CLOUD_ORANGE), mouth.x, mouth.y, mouth.z, 24, 0.5, 0.5, 0.5, 0.0);
    this.playSound(SoundEvents.PLAYER_BURP, 3.0F, 0.5F);
    this.endFeast();
  }

  private void endFeast() {

    this.feast = Feast.NONE;
    this.feastTicks = 0;
    this.meal = null;
    this.spitMealEarly = false;
    this.feastCooldown = FEAST_COOLDOWN;
    this.entityData.set(DATA_GAPING, false);
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

  private void startBurst() {

    this.releaseTether();
    this.volleyRemaining = 0;
    this.burst = Burst.SPITTING;
    this.burstTicks = 0;
    this.burstSpits = 0;
    this.playSound(SoundEvents.RAVAGER_ROAR, 3.0F, 0.6F);
  }

  private void tickBurst(ServerLevel level, @Nullable LivingEntity target) {

    this.burstTicks++;
    switch (this.burst) {
      case SPITTING -> {
        if (this.burstTicks % BURST_SPIT_GAP != 0) {
          return;
        }
        if (this.livingBrood(level).stream().filter(JackOMimic.class::isInstance).count() >= MAX_GOURDLINGS || this.burstSpits >= BURST_MAX_SPITS) {
          this.burst = Burst.GATHERING;
          this.burstTicks = 0;
          this.entityData.set(DATA_GAPING, true);
          this.playSound(SoundEvents.WARDEN_ROAR, 3.0F, 0.8F);
          return;
        }
        this.spit(level, target, 180.0F);
        this.burstSpits++;
      }
      case GATHERING -> {
        Vec3 mouth = this.position().add(this.mouthOffset(this.yBodyRot));
        for (Mob mimic : this.livingBrood(level)) {
          Vec3 toMouth = mouth.subtract(mimic.position());
          if (toMouth.lengthSqr() > GATHER_STOP * GATHER_STOP) {
            Vec3 pull = toMouth.normalize().scale(GATHER_PULL);
            mimic.setDeltaMovement(pull.x, Math.max(pull.y, mimic.getDeltaMovement().y), pull.z);
            mimic.hurtMarked = true;
          }
          if (this.burstTicks % 4 == 0) {
            level.sendParticles(ParticleTypes.SOUL, mimic.getX(), mimic.getY() + 0.5, mimic.getZ(), 2, 0.2, 0.2, 0.2, 0.01);
          }
        }
        if (this.burstTicks % 5 == 0) {
          level.sendParticles(ParticleTypes.FLAME, mouth.x, mouth.y, mouth.z, 10, 0.6, 0.4, 0.6, 0.02);
        }
        if (this.burstTicks >= GATHER_TICKS) {
          this.blast(level);
          this.entityData.set(DATA_GAPING, false);
          this.burst = Burst.NONE;
          this.burstCooldown = BURST_COOLDOWN;
        }
      }
      default -> this.burst = Burst.NONE;
    }
  }

  private void blast(ServerLevel level) {

    Vec3 center = this.position();
    for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(BLAST_RADIUS), entity -> entity != this && entity.isAlive())) {
      if (entity instanceof Player player && (player.isSpectator() || player.isCreative())) {
        continue;
      }
      if (!this.isBrood(entity)) {
        entity.hurtServer(level, this.damageSources().mobAttack(this), BLAST_DAMAGE);
      }
      Vec3 away = entity.position().subtract(center).multiply(1.0, 0.0, 1.0);
      Vec3 direction = away.lengthSqr() > 1.0E-4 ? away.normalize() : Vec3.directionFromRotation(0.0F, this.random.nextFloat() * 360.0F);
      entity.setDeltaMovement(direction.x * BLAST_SPEED, BLAST_LIFT, direction.z * BLAST_SPEED);
      entity.hurtMarked = true;
    }

    level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y + 1.5, center.z, 1, 0.0, 0.0, 0.0, 0.0);
    level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y + 1.0, center.z, 120, 3.0, 1.0, 3.0, 0.3);
    level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 3.0F, 0.6F);
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

  private void tickVolley(ServerLevel level, LivingEntity target) {

    if (this.volleyRemaining > 0) {
      if (--this.spitTimer <= 0) {
        this.spit(level, target, FLING_SPREAD);
        this.volleyRemaining--;
        this.spitTimer = SPIT_GAP;
      }
      return;
    }
    if (--this.volleyCooldown > 0) {
      return;
    }
    this.volleyCooldown = VOLLEY_INTERVAL;
    if (level.getEntitiesOfClass(JackOMimic.class, this.getBoundingBox().inflate(GOURDLING_RANGE)).size() < MAX_GOURDLINGS) {
      this.volleyRemaining = VOLLEY_SIZE;
      this.spitTimer = 0;
    }
  }

  private void spit(ServerLevel level, @Nullable LivingEntity target, float spread) {

    level.broadcastEntityEvent(this, SPIT_EVENT);

    Vec3 mouth = this.position().add(this.mouthOffset(this.yBodyRot));
    Vec3 aim = target == null ? Vec3.ZERO : target.position().subtract(mouth).multiply(1.0, 0.0, 1.0);
    Vec3 direction = (aim.lengthSqr() > 1.0E-4 ? aim.normalize() : Vec3.directionFromRotation(0.0F, this.yBodyRot))
        .yRot((this.random.nextFloat() - 0.5F) * 2.0F * spread * Mth.DEG_TO_RAD);

    JackOMimic gourdling = ModEntities.JACK_O_MIMIC.create(level, EntitySpawnReason.MOB_SUMMONED);
    if (gourdling == null) {
      return;
    }
    gourdling.snapTo(mouth.x, mouth.y, mouth.z, this.yBodyRot, 0.0F);
    gourdling.setDeltaMovement(direction.scale(FLING_SPEED).add(0.0, FLING_LIFT, 0.0));
    MotherBrood.adopt(gourdling);
    gourdling.setTarget(this.broodTarget());
    level.addFreshEntity(gourdling);
    this.brood.add(gourdling.getUUID());

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

  private void tickReclaim(ServerLevel level) {

    Entity tethered = this.getTethered();
    if (tethered == null) {
      if (this.entityData.get(DATA_TETHER) != NO_TETHER) {
        this.releaseTether();
      }
      if (--this.reclaimCooldown > 0) {
        return;
      }
      this.reclaimCooldown = RECLAIM_INTERVAL;
      level.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(RECLAIM_RANGE), Hallowing::isHallowed).stream()
          .min(Comparator.comparingDouble(this::distanceToSqr))
          .ifPresent(mob -> {
            this.entityData.set(DATA_TETHER, mob.getId());
            this.tetherTicks = 0;
            this.playSound(SoundEvents.VINE_STEP, 3.0F, 0.5F);
            this.playSound(SoundEvents.EVOKER_PREPARE_ATTACK, 2.0F, 0.6F);
          });
      return;
    }

    if (!(tethered instanceof Mob mob) || !mob.isAlive() || !Hallowing.isHallowed(mob)) {
      this.releaseTether();
      return;
    }

    Vec3 mouth = this.position().add(this.mouthOffset(this.yBodyRot));
    if (this.tetherTicks % 3 == 0) {
      Vec3 along = mob.position().add(0.0, mob.getBbHeight() * 0.6, 0.0).subtract(mouth);
      for (int i = 1; i < 8; i++) {
        Vec3 point = mouth.add(along.scale(i / 8.0));
        level.sendParticles(ParticleTypes.COMPOSTER, point.x, point.y, point.z, 1, 0.05, 0.05, 0.05, 0.0);
      }
    }
    float radians = this.yBodyRot * Mth.DEG_TO_RAD;
    Vec3 hold = new Vec3(this.getX() - Mth.sin(radians) * HOLD_DISTANCE, mob.getY(), this.getZ() + Mth.cos(radians) * HOLD_DISTANCE);
    Vec3 toHold = hold.subtract(mob.position());
    Vec3 pull = toHold.lengthSqr() > HOLD_TOLERANCE * HOLD_TOLERANCE ? toHold.normalize().scale(Math.min(TETHER_PULL, toHold.length())) : Vec3.ZERO;
    mob.setDeltaMovement(pull.x, mob.getDeltaMovement().y, pull.z);
    mob.getNavigation().stop();
    mob.hurtMarked = true;

    if (++this.tetherTicks >= RECLAIM_TICKS) {
      Hallowing.unhallow(mob);
      MotherBrood.adopt(mob);
      this.brood.add(mob.getUUID());
      mob.setTarget(this.broodTarget());
      this.heal(RECLAIM_HEAL);
      level.sendParticles(ParticleTypes.SCULK_SOUL, mob.getX(), mob.getY() + 1.0, mob.getZ(), 24, 0.4, 0.4, 0.4, 0.05);
      this.playSound(SoundEvents.SOUL_ESCAPE.value(), 2.0F, 0.4F);
      this.entityData.set(DATA_TETHER, NO_TETHER);
      this.tetherTicks = 0;
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
      if (this.feast == Feast.DIGESTING) {
        this.spitMealEarly = true;
      }
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
  public boolean isPushable() {
    return false;
  }

  @Override
  public void push(Entity entity) {
  }

  @Override
  public void knockback(double power, double xd, double zd) {
  }

  @Override
  public boolean removeWhenFarAway(double distSqr) {
    return false;
  }

  @Override
  public void startSeenByPlayer(ServerPlayer player) {
    super.startSeenByPlayer(player);
    this.bossEvent.addPlayer(player);
  }

  @Override
  public void stopSeenByPlayer(ServerPlayer player) {
    super.stopSeenByPlayer(player);
    this.bossEvent.removePlayer(player);
  }

  @Override
  protected void addAdditionalSaveData(ValueOutput output) {
    super.addAdditionalSaveData(output);
    output.storeNullable("summoner", UUIDUtil.CODEC, this.summoner);
    output.store("brood", UUIDUtil.CODEC.listOf(), List.copyOf(this.brood));
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    this.summoner = input.read("summoner", UUIDUtil.CODEC).orElse(null);
    this.brood.clear();
    this.brood.addAll(input.read("brood", UUIDUtil.CODEC.listOf()).orElse(List.of()));
    this.bossEvent.setName(this.getDisplayName());
  }

  private enum Burst {
    NONE,
    SPITTING,
    GATHERING
  }

  private enum Feast {
    NONE,
    INHALING,
    DIGESTING
  }
}
