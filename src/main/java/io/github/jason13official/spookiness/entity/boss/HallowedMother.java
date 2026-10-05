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
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
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

  private static final EntityDataAccessor<Integer> DATA_TETHER = SynchedEntityData.defineId(HallowedMother.class, EntityDataSerializers.INT);

  private static final int NO_TETHER = -1;
  private static final int RECLAIM_INTERVAL = 160;
  private static final int RECLAIM_TICKS = 80;
  private static final double RECLAIM_RANGE = 16.0;
  private static final double TETHER_PULL = 0.15;
  private static final float RECLAIM_HEAL = 40.0F;
  private static final int SEED_INTERVAL = 200;
  private static final int SEEDS = 2;
  private static final float PROJECTILE_MULTIPLIER = 0.4F;
  private static final long DAY_LENGTH = 24000L;
  private static final long NOON = 6000L;
  private static final long DUSK = 12000L;

  private final ServerBossEvent bossEvent = new ServerBossEvent(UUID.randomUUID(), this.getDisplayName(), BossEvent.BossBarColor.PURPLE,
      BossEvent.BossBarOverlay.NOTCHED_10);

  private @Nullable UUID summoner;
  private int tetherTicks;
  private int reclaimCooldown = RECLAIM_INTERVAL;

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
    if (target != null && this.tickCount % SEED_INTERVAL == 0) {
      this.spitSeeds(level, target);
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
            this.playSound(SoundEvents.VINE_STEP, 2.0F, 0.5F);
          });
      return;
    }

    if (!(tethered instanceof Mob mob) || !mob.isAlive() || !Hallowing.isHallowed(mob)) {
      this.releaseTether();
      return;
    }

    Vec3 pull = this.position().subtract(mob.position()).normalize().scale(TETHER_PULL);
    mob.setDeltaMovement(pull.x, Math.max(mob.getDeltaMovement().y, 0.02), pull.z);
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

  private void spitSeeds(ServerLevel level, LivingEntity target) {

    for (int i = 0; i < SEEDS; i++) {
      JackOMimic gourdling = ModEntities.JACK_O_MIMIC.create(level, EntitySpawnReason.MOB_SUMMONED);
      if (gourdling == null) {
        continue;
      }
      double angle = this.random.nextDouble() * Math.PI * 2.0;
      gourdling.snapTo(this.getX() + Math.cos(angle) * 3.0, this.getY() + 1.0, this.getZ() + Math.sin(angle) * 3.0, this.random.nextFloat() * 360.0F, 0.0F);
      gourdling.setTarget(target);
      level.addFreshEntity(gourdling);
    }
    this.playSound(SoundEvents.LLAMA_SPIT, 2.0F, 0.5F);
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
