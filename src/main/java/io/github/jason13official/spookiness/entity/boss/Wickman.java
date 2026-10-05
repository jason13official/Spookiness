package io.github.jason13official.spookiness.entity.boss;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.item.PumpkinMaceItem;
import io.github.jason13official.spookiness.registry.ModEntities;
import io.github.jason13official.spookiness.registry.ModItems;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class Wickman extends Monster {

  private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(Wickman.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(Wickman.class, EntityDataSerializers.INT);

  private static final Identifier FROST_WEAKNESS_ID = Spookiness.id("frostwick_weakness");
  private static final Identifier HEADLESS_SPEED_ID = Spookiness.id("wickman_headless_speed");
  private static final double FROST_WEAKNESS = -0.4;
  private static final double HEADLESS_SPEED = 0.3;
  private static final float CANDLE_CHOIR_THRESHOLD = 0.6F;
  private static final float HEADLESS_THRESHOLD = 0.25F;
  private static final float SPUTTER_MULTIPLIER = 0.8F;
  private static final int THROW_INTERVAL = 60;
  private static final int VIGIL_CANDLES = 4;
  private static final double VIGIL_RADIUS = 6.0;
  private static final int PUMPKIN_KILL_REWARD = 5;

  private final ServerBossEvent bossEvent = new ServerBossEvent(UUID.randomUUID(), this.getDisplayName(), BossEvent.BossBarColor.YELLOW,
      BossEvent.BossBarOverlay.PROGRESS);

  public Wickman(EntityType<? extends Wickman> type, Level level) {
    super(type, level);
    this.xpReward = 50;
    this.setPersistenceRequired();
    this.bossEvent.setCreateWorldFog(true);
    this.bossEvent.setDarkenScreen(true);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 220.0).add(Attributes.ARMOR, 8.0).add(Attributes.ATTACK_DAMAGE, 9.0)
        .add(Attributes.MOVEMENT_SPEED, 0.28).add(Attributes.FOLLOW_RANGE, 40.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.6).add(Attributes.STEP_HEIGHT, 1.5);
  }

  @Override
  protected void registerGoals() {

    this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.1, false));
    this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
    this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 16.0F));
    this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));

    this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
    this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder entityData) {
    super.defineSynchedData(entityData);
    entityData.define(DATA_VARIANT, Variant.WICK.ordinal());
    entityData.define(DATA_PHASE, Phase.STALKER.ordinal());
  }

  public static Wickman kindle(ServerLevel level, LivingEntity vessel, Variant variant, LivingEntity kindler) {

    Wickman wickman = ModEntities.WICKMAN.create(level, EntitySpawnReason.CONVERSION);
    if (wickman == null) {
      return null;
    }
    wickman.snapTo(vessel.getX(), vessel.getY(), vessel.getZ(), vessel.getYRot(), 0.0F);
    wickman.setVariant(variant);
    wickman.setTarget(kindler);
    level.addFreshEntity(wickman);
    return wickman;
  }

  @Override
  protected Component getTypeName() {
    return this.getVariant() == Variant.FROST ? Component.translatable("entity.spookiness.frostwick") : super.getTypeName();
  }

  public Variant getVariant() {
    return Variant.byOrdinal(this.entityData.get(DATA_VARIANT));
  }

  public void setVariant(Variant variant) {

    this.entityData.set(DATA_VARIANT, variant.ordinal());
    this.bossEvent.setColor(variant == Variant.FROST ? BossEvent.BossBarColor.BLUE : BossEvent.BossBarColor.YELLOW);
    this.bossEvent.setName(this.getDisplayName());
    if (variant == Variant.FROST) {
      this.applyModifier(Attributes.MAX_HEALTH, FROST_WEAKNESS_ID, FROST_WEAKNESS);
      this.applyModifier(Attributes.ATTACK_DAMAGE, FROST_WEAKNESS_ID, FROST_WEAKNESS);
      this.setHealth(this.getMaxHealth());
    }
  }

  public Phase getPhase() {
    return Phase.byOrdinal(this.entityData.get(DATA_PHASE));
  }

  private void setPhase(ServerLevel level, Phase phase) {

    this.entityData.set(DATA_PHASE, phase.ordinal());
    switch (phase) {
      case CANDLE_CHOIR -> this.plantVigilCandles(level);
      case HEADLESS -> this.applyModifier(Attributes.MOVEMENT_SPEED, HEADLESS_SPEED_ID, HEADLESS_SPEED);
      default -> {
      }
    }
  }

  private void applyModifier(Holder<Attribute> attribute, Identifier id, double amount) {

    AttributeInstance instance = this.getAttribute(attribute);
    if (instance != null && !instance.hasModifier(id)) {
      instance.addPermanentModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
  }

  private void plantVigilCandles(ServerLevel level) {

    double startAngle = this.random.nextDouble() * Math.PI * 2.0;
    for (int i = 0; i < VIGIL_CANDLES; i++) {
      double angle = startAngle + Math.PI * 2.0 * i / VIGIL_CANDLES;
      VigilCandle.plant(level, this, this.position().add(Math.cos(angle) * VIGIL_RADIUS, 0.0, Math.sin(angle) * VIGIL_RADIUS), this.getVariant() == Variant.FROST,
          false);
    }
    level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLAZE_AMBIENT, SoundSource.HOSTILE, 1.5F, 0.6F);
  }

  @Override
  protected void customServerAiStep(ServerLevel level) {
    super.customServerAiStep(level);

    float ratio = this.getHealth() / this.getMaxHealth();
    this.bossEvent.setProgress(ratio);

    Phase next = ratio < HEADLESS_THRESHOLD ? Phase.HEADLESS : ratio < CANDLE_CHOIR_THRESHOLD ? Phase.CANDLE_CHOIR : Phase.STALKER;
    if (next.ordinal() > this.getPhase().ordinal()) {
      this.setPhase(level, next);
    }

    LivingEntity target = this.getTarget();
    if (target != null && this.tickCount % THROW_INTERVAL == 0 && this.hasLineOfSight(target)) {
      this.throwAt(level, target);
    }
  }

  private void throwAt(ServerLevel level, LivingEntity target) {

    Vec3 eye = this.getEyePosition();
    Vec3 delta = target.getEyePosition().subtract(eye);
    if (this.getVariant() == Variant.FROST) {
      Snowball snowball = new Snowball(level, this, new ItemStack(Items.SNOWBALL));
      snowball.shoot(delta.x, delta.y + delta.horizontalDistance() * 0.2, delta.z, 1.6F, 6.0F);
      level.addFreshEntity(snowball);
      this.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.6F);
      return;
    }
    SmallFireball fireball = new SmallFireball(level, this, delta.normalize());
    fireball.setPos(eye.x, eye.y, eye.z);
    level.addFreshEntity(fireball);
    this.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 0.6F);
  }

  public static void onIncomingDamage(LivingIncomingDamageEvent event) {

    if (event.getSource().getEntity() instanceof Wickman wickman && wickman.getVariant() == Variant.WICK && wickman.isInWaterOrRain()) {
      event.setAmount(event.getAmount() * SPUTTER_MULTIPLIER);
    }
  }

  @Override
  protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
    super.dropCustomDeathLoot(level, source, killedByPlayer);

    this.spawnAtLocation(level, new ItemStack(ModItems.PIECE_OF_LAMENT_ONE));
    if (this.getVariant() == Variant.FROST) {
      this.spawnAtLocation(level, new ItemStack(Items.SNOWBALL, Mth.nextInt(this.random, 4, 12)));
      this.spawnAtLocation(level, new ItemStack(Items.PACKED_ICE, Mth.nextInt(this.random, 1, 4)));
    } else {
      this.spawnAtLocation(level, new ItemStack(Items.BLAZE_ROD, Mth.nextInt(this.random, 1, 3)));
      this.spawnAtLocation(level, new ItemStack(Items.HONEYCOMB, Mth.nextInt(this.random, 1, 4)));
    }

    ItemStack weapon = source.getWeaponItem();
    if (source.getEntity() instanceof Player player && weapon != null && weapon.is(ModItems.PUMPKIN_MACE)) {
      for (int i = 0; i < PUMPKIN_KILL_REWARD; i++) {
        PumpkinMaceItem.addPumpkinKill(level, player, weapon);
      }
    }
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
  public boolean removeWhenFarAway(double distSqr) {
    return false;
  }

  @Override
  protected void addAdditionalSaveData(ValueOutput output) {
    super.addAdditionalSaveData(output);
    output.putInt("variant", this.getVariant().ordinal());
    output.putInt("phase", this.getPhase().ordinal());
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    this.entityData.set(DATA_VARIANT, input.getIntOr("variant", 0));
    this.entityData.set(DATA_PHASE, input.getIntOr("phase", 0));
    this.bossEvent.setColor(this.getVariant() == Variant.FROST ? BossEvent.BossBarColor.BLUE : BossEvent.BossBarColor.YELLOW);
    this.bossEvent.setName(this.getDisplayName());
  }

  public enum Variant {
    WICK,
    FROST;

    public static Variant byOrdinal(int ordinal) {
      Variant[] values = values();
      return values[Mth.clamp(ordinal, 0, values.length - 1)];
    }
  }

  public enum Phase {
    STALKER,
    CANDLE_CHOIR,
    HEADLESS;

    public static Phase byOrdinal(int ordinal) {
      Phase[] values = values();
      return values[Mth.clamp(ordinal, 0, values.length - 1)];
    }
  }
}
