package io.github.jason13official.spookiness.entity.boss;

import io.github.jason13official.spookiness.companion.Hallowing;
import io.github.jason13official.spookiness.companion.PlayerFollowers;
import io.github.jason13official.spookiness.effect.SoulBurst;
import io.github.jason13official.spookiness.entity.JackOMimic;
import io.github.jason13official.spookiness.registry.ModEntities;
import io.github.jason13official.spookiness.registry.ModItems;
import java.util.Comparator;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.BlockParticleOption;
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
import net.minecraft.world.entity.AnimationState;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class HallowedMother extends Monster {

  public static final float SCALE = 4.0F;

  private static final EntityDataAccessor<Integer> DATA_TETHER = SynchedEntityData.defineId(HallowedMother.class, EntityDataSerializers.INT);

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
  private static final float PROJECTILE_MULTIPLIER = 0.4F;
  private static final long DAY_LENGTH = 24000L;
  private static final long NOON = 6000L;
  private static final long DUSK = 12000L;

  private final ServerBossEvent bossEvent = new ServerBossEvent(UUID.randomUUID(), this.getDisplayName(), BossEvent.BossBarColor.PURPLE,
      BossEvent.BossBarOverlay.NOTCHED_10);

  private @Nullable UUID summoner;
  private int tetherTicks;
  private int reclaimCooldown = RECLAIM_INTERVAL;
  private int volleyCooldown = 60;
  private int volleyRemaining;
  private int spitTimer;

  public final AnimationState spitAnimationState = new AnimationState();

  public HallowedMother(EntityType<? extends HallowedMother> type, Level level) {
    super(type, level);
    this.xpReward = 120;
    this.setPersistenceRequired();
    this.bossEvent.setCreateWorldFog(true);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 400.0).add(Attributes.ARMOR, 10.0).add(Attributes.ATTACK_DAMAGE, 10.0)
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

    long dayTime = level.getOverworldClockTime() % DAY_LENGTH;
    if (dayTime >= NOON && dayTime < DUSK) {
      this.sink(level, "message.spookiness.mother_sinks_dawn");
      return;
    }

    if (this.tickCount % 20 == 0 && this.summoner != null && level.getServer().getPlayerList().getPlayer(this.summoner) instanceof ServerPlayer owner
        && Hallowing.count(owner) == 0 && this.getTethered() == null) {
      this.sink(level, "message.spookiness.mother_sinks_lost");
      return;
    }

    this.tickReclaim(level);

    LivingEntity target = this.getTarget();
    if (target != null) {
      this.faceTarget(target);
      this.tickVolley(level, target);
    }
  }

  public Vec3 mouthOffset(float bodyYRot) {

    float radians = bodyYRot * Mth.DEG_TO_RAD;
    return new Vec3(-Mth.sin(radians) * MOUTH_FORWARD, MOUTH_HEIGHT, Mth.cos(radians) * MOUTH_FORWARD);
  }

  private void faceTarget(LivingEntity target) {

    double dx = target.getX() - this.getX();
    double dz = target.getZ() - this.getZ();
    float wanted = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
    float yRot = Mth.approachDegrees(this.getYRot(), wanted, TURN_SPEED);
    this.setYRot(yRot);
    this.yBodyRot = yRot;
    this.yHeadRot = yRot;
  }

  private void tickVolley(ServerLevel level, LivingEntity target) {

    if (this.volleyRemaining > 0) {
      if (--this.spitTimer <= 0) {
        this.spit(level, target);
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

  private void spit(ServerLevel level, LivingEntity target) {

    level.broadcastEntityEvent(this, SPIT_EVENT);

    Vec3 mouth = this.position().add(this.mouthOffset(this.yBodyRot));
    Vec3 aim = target.position().subtract(mouth).multiply(1.0, 0.0, 1.0);
    Vec3 direction = (aim.lengthSqr() > 1.0E-4 ? aim.normalize() : Vec3.directionFromRotation(0.0F, this.yBodyRot))
        .yRot((this.random.nextFloat() - 0.5F) * 2.0F * FLING_SPREAD * Mth.DEG_TO_RAD);

    JackOMimic gourdling = ModEntities.JACK_O_MIMIC.create(level, EntitySpawnReason.MOB_SUMMONED);
    if (gourdling == null) {
      return;
    }
    gourdling.snapTo(mouth.x, mouth.y, mouth.z, this.yBodyRot, 0.0F);
    gourdling.setDeltaMovement(direction.scale(FLING_SPEED).add(0.0, FLING_LIFT, 0.0));
    gourdling.setTarget(target);
    level.addFreshEntity(gourdling);

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
            for (ServerPlayer player : this.bossEvent.getPlayers()) {
              player.sendOverlayMessage(Component.translatable("message.spookiness.mother_seizes", mob.getDisplayName()));
            }
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
      mob.setTarget(this.getTarget());
      this.heal(RECLAIM_HEAL);
      level.sendParticles(ParticleTypes.SCULK_SOUL, mob.getX(), mob.getY() + 1.0, mob.getZ(), 24, 0.4, 0.4, 0.4, 0.05);
      this.playSound(SoundEvents.SOUL_ESCAPE.value(), 2.0F, 0.4F);
      this.entityData.set(DATA_TETHER, NO_TETHER);
      this.tetherTicks = 0;
    }
  }

  private void sink(ServerLevel level, String message) {

    for (ServerPlayer player : this.bossEvent.getPlayers()) {
      player.sendOverlayMessage(Component.translatable(message));
    }
    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ROOTED_DIRT.defaultBlockState()), this.getX(), this.getY() + 0.5, this.getZ(), 120, 2.0,
        0.5, 2.0, 0.2);
    level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WARDEN_DIG, SoundSource.HOSTILE, 2.0F, 0.7F);
    this.discard();
  }

  @Override
  public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {

    ItemStack weapon = source.getWeaponItem();
    if (weapon != null && weapon.is(ModItems.PUMPKIN_MACE) && this.getTethered() != null) {
      this.releaseTether();
    }
    if (source.is(DamageTypeTags.IS_PROJECTILE)) {
      damage *= PROJECTILE_MULTIPLIER;
    }
    return super.hurtServer(level, source, damage);
  }

  @Override
  protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
    super.dropCustomDeathLoot(level, source, killedByPlayer);

    this.spawnAtLocation(level, new ItemStack(ModItems.PIECE_OF_LAMENT_TWO));
    this.spawnAtLocation(level, new ItemStack(Items.MAGMA_CREAM, 2));
    this.spawnAtLocation(level, new ItemStack(Items.PUMPKIN_PIE, 3));

    if (this.summoner != null && level.getServer().getPlayerList().getPlayer(this.summoner) instanceof ServerPlayer owner) {
      for (Mob ally : PlayerFollowers.followers(owner, Hallowing::isHallowed)) {
        Hallowing.bless(ally);
        SoulBurst.spawn(level, ally.getBoundingBox().getCenter(), 24, 0.3, 0.06);
      }
    }
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
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    this.summoner = input.read("summoner", UUIDUtil.CODEC).orElse(null);
    this.bossEvent.setName(this.getDisplayName());
  }
}
