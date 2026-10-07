package io.github.jason13official.spookiness.entity.boss.gourdwyrm;

import io.github.jason13official.spookiness.entity.boss.SpookyBoss;
import io.github.jason13official.spookiness.util.Spawning;
import io.github.jason13official.spookiness.util.SpookyMath;
import net.minecraft.sounds.SoundEvent;
import io.github.jason13official.spookiness.registry.ModSounds;
import io.github.jason13official.spookiness.registry.ModEntities;
import io.github.jason13official.spookiness.world.NetherrealmArena;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.jspecify.annotations.Nullable;

public class Gourdwyrm extends SpookyBoss {

  public static final int SEGMENTS = 24;

  private static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(Gourdwyrm.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Integer> DATA_LIT = SynchedEntityData.defineId(Gourdwyrm.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Integer> DATA_LENGTH = SynchedEntityData.defineId(Gourdwyrm.class, EntityDataSerializers.INT);

  private static final float UNLIT_MULTIPLIER = 0.25F;
  private static final int LIT_INTERVAL = 200;
  private static final int LIT_COUNT = 3;
  static final double CIRCLE_HEIGHT = 14.0;
  static final double FLY_SPEED = 0.6;
  static final double SHED_SPEED_MULTIPLIER = 1.35;
  private static final int SHED_LENGTH = 16;
  private static final float SHED_HEALTH = 0.3F;
  private static final double ARENA_RANGE = 64.0;
  private static final float CONTACT_DAMAGE = 10.0F;
  private static final double HEAD_REACH = 1.5;
  private static final double SEGMENT_REACH = 0.4;
  private static final double DEATH_HEIGHT = 2.5;

  private final GourdwyrmBody body = new GourdwyrmBody(this);

  private @Nullable BlockPos anchor;
  private final Map<Phase, GourdwyrmPhase> phases = new EnumMap<>(Phase.class);

  public Gourdwyrm(EntityType<? extends Gourdwyrm> type, Level level) {
    super(type, level, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_12);
    this.setId(ENTITY_COUNTER.getAndAdd(SEGMENTS + 1) + 1);
    this.noPhysics = true;
    this.setNoGravity(true);
    this.xpReward = 500;
    this.bossEvent.setDarkenScreen(true);
    this.bossEvent.setPlayBossMusic(true);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 600.0).add(Attributes.ARMOR, 6.0).add(Attributes.ATTACK_DAMAGE, 12.0)
        .add(Attributes.FOLLOW_RANGE, 96.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
  }

  public static @Nullable Gourdwyrm awaken(ServerLevel level, BlockPos anchor, Vec3 pos) {

    Gourdwyrm wyrm = Spawning.spawn(level, ModEntities.GOURDWYRM, EntitySpawnReason.EVENT, pos, 0.0F, spawned -> spawned.anchor = anchor);
    if (wyrm == null) {
      return null;
    }
    level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENDER_DRAGON_GROWL, wyrm.getSoundSource(), 4.0F, 0.6F);
    return wyrm;
  }

  @Override
  public void setId(int id) {
    super.setId(id);
    this.body.assignIds(id + 1);
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder entityData) {
    super.defineSynchedData(entityData);
    entityData.define(DATA_PHASE, Phase.CIRCLING.getId());
    entityData.define(DATA_LIT, 0);
    entityData.define(DATA_LENGTH, SEGMENTS);
  }

  @Override
  public boolean isMultipartEntity() {
    return true;
  }

  @Override
  public PartEntity<?>[] getParts() {
    return this.body.segments();
  }

  public GourdwyrmPart[] getSegments() {
    return this.body.segments();
  }

  public Phase getPhase() {
    return Phase.byId(this.entityData.get(DATA_PHASE));
  }

  public int getLength() {
    return this.entityData.get(DATA_LENGTH);
  }

  public boolean isSegmentAlive(int index) {
    return index < this.getLength();
  }

  public boolean isSegmentLit(int index) {
    return (this.entityData.get(DATA_LIT) & (1 << index)) != 0;
  }

  public @Nullable BlockPos getAnchor() {
    return this.anchor;
  }

  @Override
  public void tick() {
    super.tick();
    this.body.tick();
  }

  @Override
  public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
    super.onSyncedDataUpdated(accessor);
    if (DATA_LIT.equals(accessor) || DATA_LENGTH.equals(accessor)) {
      this.body.refreshDimensions();
    }
  }

  @Override
  public void onRemovedFromLevel() {
    super.onRemovedFromLevel();
    this.body.removeLights();
  }

  @Override
  public void die(DamageSource source) {
    if (this.anchor != null && !this.level().isClientSide()) {
      Vec3 altar = Vec3.atBottomCenterOf(NetherrealmArena.altarPos(this.anchor)).add(0.0, DEATH_HEIGHT, 0.0);
      this.snapTo(altar.x, altar.y, altar.z, this.getYRot(), this.getXRot());
      this.setDeltaMovement(Vec3.ZERO);
    }
    super.die(source);
  }

  @Override
  protected void customServerAiStep(ServerLevel level) {
    super.customServerAiStep(level);

    if (this.anchor == null) {
      this.anchor = this.blockPosition().below((int) CIRCLE_HEIGHT);
    }

    if (this.getLength() > SHED_LENGTH && this.getHealth() < this.getMaxHealth() * SHED_HEALTH) {
      this.shed(level);
    }

    this.phase(this.getPhase()).serverTick(level);

    if (this.getPhase() != Phase.LANTERN_RINGS && this.tickCount % LIT_INTERVAL == 0) {
      this.relightLanterns();
    }
  }

  boolean isShed() {
    return this.getLength() <= SHED_LENGTH;
  }

  double speed() {
    return this.isShed() ? FLY_SPEED * SHED_SPEED_MULTIPLIER : FLY_SPEED;
  }

  private GourdwyrmPhase phase(Phase phase) {
    return this.phases.computeIfAbsent(phase, key -> key.factory.apply(this));
  }

  void setPhase(Phase phase) {
    this.entityData.set(DATA_PHASE, phase.getId());
    this.phase(phase).begin();
  }

  @Nullable Player pickTarget(ServerLevel level) {
    if (this.anchor == null) {
      return null;
    }
    Vec3 center = Vec3.atBottomCenterOf(this.anchor);
    List<Player> players = level.getEntitiesOfClass(Player.class, new AABB(center, center).inflate(ARENA_RANGE),
        player -> player.isAlive() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player));
    return players.isEmpty() ? null : players.get(this.random.nextInt(players.size()));
  }

  void contactDamage(ServerLevel level) {
    this.ram(level, this.getBoundingBox().inflate(HEAD_REACH));
    for (GourdwyrmPart segment : this.body.segments()) {
      if (this.isSegmentAlive(segment.index)) {
        this.ram(level, segment.getBoundingBox().inflate(SEGMENT_REACH));
      }
    }
  }

  private void ram(ServerLevel level, AABB area) {
    for (Player victim : level.getEntitiesOfClass(Player.class, area, player -> player.isAlive() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player))) {
      if (victim.hurtServer(level, this.damageSources().mobAttack(this), CONTACT_DAMAGE)) {
        Vec3 push = victim.position().subtract(area.getCenter()).normalize().scale(1.5);
        victim.setDeltaMovement(push.x, 0.6, push.z);
        victim.hurtMarked = true;
      }
    }
  }

  private void shed(ServerLevel level) {

    for (int i = SHED_LENGTH; i < SEGMENTS; i++) {
      GourdwyrmPart segment = this.body.segments()[i];
      level.sendParticles(ParticleTypes.EXPLOSION, segment.getX(), segment.getY() + 1.0, segment.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
      Spawning.spawn(level, ModEntities.JACK_O_MIMIC, EntitySpawnReason.EVENT, segment.position(), SpookyMath.randomYaw(this.random));
    }
    this.entityData.set(DATA_LENGTH, SHED_LENGTH);
    this.entityData.set(DATA_LIT, this.entityData.get(DATA_LIT) & ((1 << SHED_LENGTH) - 1));
    this.playSound(SoundEvents.ENDER_DRAGON_HURT, 4.0F, 0.6F);
    this.playSound(SoundEvents.WOOD_BREAK, 4.0F, 0.5F);
  }

  void faceToward(Vec3 direction) {
    if (direction.horizontalDistanceSqr() > 1.0E-4) {
      float yRot = SpookyMath.yawToward(direction);
      this.setYRot(yRot);
      this.yBodyRot = yRot;
      this.yHeadRot = yRot;
    }
  }

  void flyToward(Vec3 wanted, double speed) {

    Vec3 delta = wanted.subtract(this.position());
    Vec3 velocity = delta.lengthSqr() > speed * speed ? delta.normalize().scale(speed) : delta;
    this.setDeltaMovement(velocity);
    if (velocity.horizontalDistanceSqr() > 1.0E-4) {
      float yRot = SpookyMath.yawToward(velocity);
      this.setYRot(yRot);
      this.yBodyRot = yRot;
      this.yHeadRot = yRot;
    }
  }

  @Override
  public void travel(Vec3 input) {
    this.move(MoverType.SELF, this.getDeltaMovement());
  }

  void relightLanterns() {

    int lit = 0;
    int length = this.getLength();
    for (int i = 0; i < LIT_COUNT && length > 0; i++) {
      lit |= 1 << this.random.nextInt(length);
    }
    this.entityData.set(DATA_LIT, lit);
  }

  public boolean hurtSegment(ServerLevel level, GourdwyrmPart segment, DamageSource source, float damage) {

    if (!this.isSegmentAlive(segment.index)) {
      return false;
    }
    boolean lit = this.isSegmentLit(segment.index);
    if (lit) {
      int remaining = this.entityData.get(DATA_LIT) & ~(1 << segment.index);
      this.entityData.set(DATA_LIT, remaining);
      this.playSound(SoundEvents.CANDLE_EXTINGUISH, 3.0F, 0.6F);
      if (remaining == 0 && this.getPhase() != Phase.LANTERN_RINGS) {
        this.setPhase(Phase.LANTERN_RINGS);
      }
    }
    boolean exposed = lit || this.getPhase() == Phase.LANTERN_RINGS;
    return this.reallyHurt(level, source, exposed ? damage : damage * UNLIT_MULTIPLIER);
  }

  @Override
  public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
    return this.reallyHurt(level, source, damage);
  }

  private boolean reallyHurt(ServerLevel level, DamageSource source, float damage) {
    if (!(source.getEntity() instanceof Player) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
      damage *= 0.5F;
    }
    return super.hurtServer(level, source, damage);
  }

  @Override
  protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
    super.dropCustomDeathLoot(level, source, killedByPlayer);
    this.xpFountain(level);
    if (this.anchor != null) {
      NetherrealmArena.onWyrmDefeated(level, this.anchor);
    }
  }

  private void xpFountain(ServerLevel level) {

    int remaining = this.xpReward;
    this.xpReward = 0;
    Vec3 center = this.position().add(0.0, this.getBbHeight() * 0.5, 0.0);
    while (remaining > 0) {
      int value = Math.min(remaining, ExperienceOrb.getExperienceValue(remaining));
      remaining -= value;
      ExperienceOrb orb = new ExperienceOrb(level, center.x, center.y, center.z, value);
      orb.setDeltaMovement((this.random.nextDouble() - 0.5) * 0.6, 0.4 + this.random.nextDouble() * 0.6, (this.random.nextDouble() - 0.5) * 0.6);
      level.addFreshEntity(orb);
    }
    level.playSound(null, center.x, center.y, center.z, SoundEvents.PLAYER_LEVELUP, this.getSoundSource(), 2.0F, 0.6F);
  }

  @Override
  protected SoundEvent getAmbientSound() {
    return ModSounds.GOURDWYRM_AMBIENT;
  }

  @Override
  public int getAmbientSoundInterval() {
    return 240;
  }

  @Override
  protected SoundEvent getHurtSound(DamageSource source) {
    return ModSounds.GOURDWYRM_HURT;
  }

  @Override
  protected SoundEvent getDeathSound() {
    return ModSounds.GOURDWYRM_DEATH;
  }

  @Override
  protected boolean isAnchored() {
    return true;
  }

  @Override
  public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
    return false;
  }

  @Override
  protected void addAdditionalSaveData(ValueOutput output) {
    super.addAdditionalSaveData(output);
    output.storeNullable("anchor", BlockPos.CODEC, this.anchor);
    output.putInt("length", this.getLength());
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    this.anchor = input.read("anchor", BlockPos.CODEC).orElse(null);
    this.entityData.set(DATA_LENGTH, input.getIntOr("length", SEGMENTS));
  }

  public enum Phase {
    CIRCLING(0, CirclingPhase::new),
    BURROW(1, BurrowPhase::new),
    LANTERN_RINGS(2, StunnedPhase::new),
    FINAL(3, ChargePhase::new);

    private static final IntFunction<Phase> BY_ID = ByIdMap.continuous(Phase::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);

    private final int id;
    private final Function<Gourdwyrm, GourdwyrmPhase> factory;

    Phase(int id, Function<Gourdwyrm, GourdwyrmPhase> factory) {
      this.id = id;
      this.factory = factory;
    }

    public static Phase byId(int id) {
      return BY_ID.apply(id);
    }

    public int getId() {
      return this.id;
    }
  }
}
