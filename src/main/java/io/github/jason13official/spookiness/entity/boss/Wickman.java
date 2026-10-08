package io.github.jason13official.spookiness.entity.boss;

import io.github.jason13official.spookiness.util.Spawning;
import io.github.jason13official.spookiness.util.SpookyMath;
import com.mojang.serialization.Codec;
import io.github.jason13official.spookiness.entity.projectile.FrostVolley;
import java.util.function.IntFunction;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import io.github.jason13official.spookiness.registry.ModSounds;
import io.github.jason13official.living_lights.api.common.lighting.LightEmission;
import io.github.jason13official.living_lights.api.common.lighting.LightEmitter;
import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.item.PumpkinMaceItem;
import io.github.jason13official.spookiness.registry.ModEntities;
import io.github.jason13official.spookiness.registry.ModItems;
import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import io.github.jason13official.spookiness.entity.projectile.PumpkinBomb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

public class Wickman extends SpookyBoss implements LightEmitter {

  private static final int VIGIL_SEARCH_UP = 3;
  private static final int VIGIL_SEARCH_DOWN = 4;
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
  private static final int VOLLEY_SIZE = 4;
  private static final float VOLLEY_SPREAD = 12.0F;
  private static final int VIGIL_CANDLES = 4;
  private static final double VIGIL_RADIUS = 6.0;
  private static final int PUMPKIN_KILL_REWARD = 5;
  private static final int HEAD_CHECK_INTERVAL = 40;
  private static final float SNOW_TRAIL_SPREAD = 0.4F;
  private static final double GUARD_LEASH = 8.0;
  private static final double GUARD_CLOSE = 4.0;

  private @Nullable UUID head;

  public Wickman(EntityType<? extends Wickman> type, Level level) {
    super(type, level, BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.PROGRESS);
    this.xpReward = 50;
    this.bossEvent.setDarkenScreen(true);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 220.0).add(Attributes.ARMOR, 8.0).add(Attributes.ATTACK_DAMAGE, 9.0)
        .add(Attributes.MOVEMENT_SPEED, 0.28).add(Attributes.FOLLOW_RANGE, 40.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.6).add(Attributes.STEP_HEIGHT, 1.5);
  }

  @Override
  protected void registerGoals() {

    this.goalSelector.addGoal(0, new GuardHeadGoal());
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
    entityData.define(DATA_VARIANT, Variant.WICK.getId());
    entityData.define(DATA_PHASE, Phase.STALKER.getId());
  }

  public static @Nullable Wickman kindle(ServerLevel level, LivingEntity vessel, Variant variant, @Nullable LivingEntity kindler) {

    return Spawning.spawn(level, ModEntities.WICKMAN, EntitySpawnReason.CONVERSION, vessel.position(), vessel.getYRot(), spawned -> {
      spawned.setVariant(variant);
      spawned.setTarget(kindler);
    });
  }

  @Override
  protected Component getTypeName() {
    return this.getVariant() == Variant.FROST ? Component.translatable("entity.spookiness.frostwick") : super.getTypeName();
  }

  public Variant getVariant() {
    return Variant.byId(this.entityData.get(DATA_VARIANT));
  }

  public void setVariant(Variant variant) {

    this.entityData.set(DATA_VARIANT, variant.getId());
    this.bossEvent.setColor(variant == Variant.FROST ? BossEvent.BossBarColor.BLUE : BossEvent.BossBarColor.YELLOW);
    this.bossEvent.setName(this.getDisplayName());
    if (variant == Variant.FROST) {
      this.applyModifier(Attributes.MAX_HEALTH, FROST_WEAKNESS_ID, FROST_WEAKNESS);
      this.applyModifier(Attributes.ATTACK_DAMAGE, FROST_WEAKNESS_ID, FROST_WEAKNESS);
      this.setHealth(this.getMaxHealth());
    }
  }

  public Phase getPhase() {
    return Phase.byId(this.entityData.get(DATA_PHASE));
  }

  private void setPhase(ServerLevel level, Phase phase) {

    this.entityData.set(DATA_PHASE, phase.getId());
    switch (phase) {
      case CANDLE_CHOIR -> this.plantVigilCandles(level);
      case HEADLESS -> {
        this.applyModifier(Attributes.MOVEMENT_SPEED, HEADLESS_SPEED_ID, HEADLESS_SPEED);
        this.ensureHead(level);
      }
      default -> {
      }
    }
  }

  @Override
  public int getLightEmission() {
    return this.getVariant() == Variant.WICK && this.getPhase() != Phase.HEADLESS ? LightEmission.MAX : 0;
  }

  private void applyModifier(Holder<Attribute> attribute, Identifier id, double amount) {

    AttributeInstance instance = this.getAttribute(attribute);
    if (instance != null && !instance.hasModifier(id)) {
      instance.addPermanentModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }
  }

  private void plantVigilCandles(ServerLevel level) {

    double startAngle = SpookyMath.randomAngle(this.random);
    for (int i = 0; i < VIGIL_CANDLES; i++) {
      double angle = SpookyMath.ringAngle(startAngle, i, VIGIL_CANDLES);
      VigilCandle.plant(level, this, this.findVigilSpot(level, angle), this.getVariant() == Variant.FROST, false);
    }
    level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BLAZE_AMBIENT, SoundSource.HOSTILE, 1.5F, 0.6F);
  }

  private Vec3 findVigilSpot(ServerLevel level, double angle) {

    for (double reach = VIGIL_RADIUS; reach >= 1.0; reach -= 1.5) {
      for (int dy = VIGIL_SEARCH_UP; dy >= -VIGIL_SEARCH_DOWN; dy--) {
        BlockPos pos = BlockPos.containing(this.getX() + Math.cos(angle) * reach, this.getY() + dy, this.getZ() + Math.sin(angle) * reach);
        Vec3 spot = Vec3.atBottomCenterOf(pos);
        if (level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP) && level.getFluidState(pos).isEmpty()
            && level.noCollision(ModEntities.VIGIL_CANDLE.getDimensions().makeBoundingBox(spot))) {
          return spot;
        }
      }
    }
    return this.position();
  }

  @Override
  public void aiStep() {
    super.aiStep();
    if (this.getVariant() == Variant.FROST && this.level() instanceof ServerLevel level && EventHooks.canEntityGrief(level, this)) {
      this.leaveSnowTrail(level);
    }
  }

  private void leaveSnowTrail(ServerLevel level) {

    BlockState snow = Blocks.SNOW.defaultBlockState();
    for (int i = 0; i < 4; i++) {
      int x = Mth.floor(this.getX() + (i % 2 * 2 - 1) * SNOW_TRAIL_SPREAD);
      int y = Mth.floor(this.getY());
      int z = Mth.floor(this.getZ() + (i / 2 % 2 * 2 - 1) * SNOW_TRAIL_SPREAD);
      BlockPos pos = new BlockPos(x, y, z);
      if (level.getBlockState(pos).isAir() && snow.canSurvive(level, pos)) {
        level.setBlockAndUpdate(pos, snow);
        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(this, snow));
      }
    }
  }

  @Override
  protected void customServerAiStep(ServerLevel level) {
    super.customServerAiStep(level);

    float ratio = this.getHealth() / this.getMaxHealth();

    Phase next = ratio < HEADLESS_THRESHOLD ? Phase.HEADLESS : ratio < CANDLE_CHOIR_THRESHOLD ? Phase.CANDLE_CHOIR : Phase.STALKER;
    if (next.getId() > this.getPhase().getId()) {
      this.setPhase(level, next);
    }

    if (this.getPhase() == Phase.HEADLESS) {
      if (this.tickCount % HEAD_CHECK_INTERVAL == 0) {
        this.ensureHead(level);
      }
      return;
    }

    LivingEntity target = this.getTarget();
    if (target != null && this.tickCount % THROW_INTERVAL == 0 && this.hasLineOfSight(target)) {
      this.throwAt(level, target);
    }
  }

  private @Nullable WickmanHead getHead(ServerLevel level) {
    return this.head != null && level.getEntity(this.head) instanceof WickmanHead found && found.isAlive() ? found : null;
  }

  private void ensureHead(ServerLevel level) {

    if (this.getHead(level) != null) {
      return;
    }
    WickmanHead detached = WickmanHead.detach(level, this);
    this.head = detached == null ? null : detached.getUUID();
  }

  @Override
  public void die(DamageSource source) {
    super.die(source);
    if (this.level() instanceof ServerLevel level) {
      WickmanHead found = this.getHead(level);
      if (found != null) {
        found.burnOut(level);
      }
    }
  }

  private void throwAt(ServerLevel level, LivingEntity target) {

    Vec3 eye = this.getEyePosition();
    Vec3 delta = target.getEyePosition().subtract(eye);
    if (this.getVariant() == Variant.FROST) {
      for (int i = 0; i < VOLLEY_SIZE; i++) {
        FrostVolley snowball = new FrostVolley(level, this);
        snowball.shoot(delta.x, delta.y + delta.horizontalDistance() * 0.2, delta.z, 1.4F, VOLLEY_SPREAD);
        level.addFreshEntity(snowball);
      }
      this.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.5F, 0.5F);
      return;
    }
    PumpkinBomb bomb = new PumpkinBomb(level, this);
    bomb.shoot(delta.x, delta.y + delta.horizontalDistance() * 0.35, delta.z, 1.0F, 4.0F);
    level.addFreshEntity(bomb);
    this.playSound(SoundEvents.WITCH_THROW, 1.0F, 0.5F);
  }

  public static void onIncomingDamage(LivingIncomingDamageEvent event) {

    if (event.getSource().getEntity() instanceof Wickman wickman && wickman.getVariant() == Variant.WICK && wickman.isInWaterOrRain()) {
      event.setAmount(event.getAmount() * SPUTTER_MULTIPLIER);
    }
  }

  @Override
  protected SoundEvent getAmbientSound() {
    return ModSounds.WICKMAN_AMBIENT;
  }

  @Override
  public int getAmbientSoundInterval() {
    return 160;
  }

  @Override
  protected SoundEvent getHurtSound(DamageSource source) {
    return ModSounds.WICKMAN_HURT;
  }

  @Override
  protected SoundEvent getDeathSound() {
    return ModSounds.WICKMAN_DEATH;
  }

  @Override
  protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
    super.dropCustomDeathLoot(level, source, killedByPlayer);

    ItemStack weapon = source.getWeaponItem();
    if (source.getEntity() instanceof Player player && weapon != null && weapon.is(ModItems.PUMPKIN_MACE)) {
      for (int i = 0; i < PUMPKIN_KILL_REWARD; i++) {
        PumpkinMaceItem.addPumpkinKill(level, player, weapon);
      }
    }
  }

  @Override
  protected void addAdditionalSaveData(ValueOutput output) {
    super.addAdditionalSaveData(output);
    output.store("variant", Variant.CODEC, this.getVariant());
    output.store("phase", Phase.CODEC, this.getPhase());
    output.storeNullable("head", UUIDUtil.CODEC, this.head);
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    this.entityData.set(DATA_VARIANT, input.read("variant", Variant.CODEC).orElse(Variant.WICK).getId());
    this.entityData.set(DATA_PHASE, input.read("phase", Phase.CODEC).orElse(Phase.STALKER).getId());
    this.head = input.read("head", UUIDUtil.CODEC).orElse(null);
    this.bossEvent.setColor(this.getVariant() == Variant.FROST ? BossEvent.BossBarColor.BLUE : BossEvent.BossBarColor.YELLOW);
    this.bossEvent.setName(this.getDisplayName());
  }

  private final class GuardHeadGoal extends Goal {

    private @Nullable WickmanHead guarded;

    GuardHeadGoal() {
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    private @Nullable WickmanHead head() {
      return Wickman.this.getPhase() == Phase.HEADLESS && Wickman.this.level() instanceof ServerLevel level ? Wickman.this.getHead(level) : null;
    }

    @Override
    public boolean canUse() {
      this.guarded = this.head();
      return this.guarded != null && Wickman.this.distanceTo(this.guarded) > GUARD_LEASH;
    }

    @Override
    public boolean canContinueToUse() {
      return this.guarded != null && this.guarded.isAlive() && Wickman.this.distanceTo(this.guarded) > GUARD_CLOSE;
    }

    @Override
    public void tick() {
      if (this.guarded != null && Wickman.this.tickCount % 10 == 0) {
        Wickman.this.getNavigation().moveTo(this.guarded.getX(), this.guarded.getY(), this.guarded.getZ(), 1.3);
      }
    }

    @Override
    public void stop() {
      this.guarded = null;
      Wickman.this.getNavigation().stop();
    }
  }

  public enum Variant implements StringRepresentable {
    WICK(0, "wick"),
    FROST(1, "frost");

    public static final Codec<Variant> CODEC = StringRepresentable.fromEnum(Variant::values);
    private static final IntFunction<Variant> BY_ID = ByIdMap.continuous(Variant::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);

    private final int id;
    private final String name;

    Variant(int id, String name) {
      this.id = id;
      this.name = name;
    }

    public static Variant byId(int id) {
      return BY_ID.apply(id);
    }

    public int getId() {
      return this.id;
    }

    @Override
    public String getSerializedName() {
      return this.name;
    }
  }

  public enum Phase implements StringRepresentable {
    STALKER(0, "stalker"),
    CANDLE_CHOIR(1, "candle_choir"),
    HEADLESS(2, "headless");

    public static final Codec<Phase> CODEC = StringRepresentable.fromEnum(Phase::values);
    private static final IntFunction<Phase> BY_ID = ByIdMap.continuous(Phase::getId, values(), ByIdMap.OutOfBoundsStrategy.CLAMP);

    private final int id;
    private final String name;

    Phase(int id, String name) {
      this.id = id;
      this.name = name;
    }

    public static Phase byId(int id) {
      return BY_ID.apply(id);
    }

    public int getId() {
      return this.id;
    }

    @Override
    public String getSerializedName() {
      return this.name;
    }
  }
}
