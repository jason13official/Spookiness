package io.github.jason13official.spookiness.entity.boss;

import io.github.jason13official.spookiness.lighting.LivingLights;
import net.minecraft.sounds.SoundEvent;
import io.github.jason13official.spookiness.registry.ModSounds;
import io.github.jason13official.spookiness.registry.ModEntities;
import io.github.jason13official.spookiness.world.netherrealm.NetherrealmArena;
import java.util.UUID;
import io.github.jason13official.spookiness.entity.JackOMimic;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.jspecify.annotations.Nullable;

public class Gourdwyrm extends Mob implements Enemy {

  public static final int SEGMENTS = 24;

  private static final EntityDataAccessor<Integer> DATA_PHASE = SynchedEntityData.defineId(Gourdwyrm.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Integer> DATA_LIT = SynchedEntityData.defineId(Gourdwyrm.class, EntityDataSerializers.INT);
  private static final EntityDataAccessor<Integer> DATA_LENGTH = SynchedEntityData.defineId(Gourdwyrm.class, EntityDataSerializers.INT);

  private static final int SPACING = 3;
  private static final int HISTORY = SEGMENTS * SPACING + 1;
  private static final float SEGMENT_SIZE = 2.0F;
  private static final float UNLIT_MULTIPLIER = 0.25F;
  private static final int LIT_INTERVAL = 200;
  private static final int LIT_COUNT = 3;
  private static final double CIRCLE_RADIUS = 28.0;
  private static final double CIRCLE_HEIGHT = 14.0;
  private static final double CIRCLE_BOB = 4.0;
  private static final double CIRCLE_SPEED = 0.012;
  private static final double FLY_SPEED = 0.6;
  private static final double SHED_SPEED_MULTIPLIER = 1.35;
  private static final int SHED_LENGTH = 16;
  private static final float SHED_HEALTH = 0.3F;
  private static final double ARENA_RANGE = 64.0;
  private static final int CIRCLE_TICKS = 240;
  private static final int SHED_CIRCLE_TICKS = 160;
  private static final int EMBER_INTERVAL = 50;
  private static final double BURROW_DEPTH = 10.0;
  private static final double DIVE_SPEED = 1.0;
  private static final int DIVE_TICKS = 80;
  private static final int RIPPLE_TICKS = 40;
  private static final double GEYSER_SPEED = 1.6;
  private static final double GEYSER_RADIUS = 3.0;
  private static final float GEYSER_DAMAGE = 10.0F;
  private static final int STUN_TICKS = 120;
  private static final double STUN_HEIGHT = 4.0;
  private static final int WINDUP_TICKS = 20;
  private static final int CHARGE_TICKS = 50;
  private static final double CHARGE_SPEED = 1.4;
  private static final double CHARGE_OVERSHOOT = 12.0;
  private static final float CONTACT_DAMAGE = 10.0F;
  private static final double HEAD_REACH = 1.5;
  private static final double SEGMENT_REACH = 0.4;
  private static final int SEGMENT_LIGHT = 15;
  private static final double DEATH_HEIGHT = 2.5;

  private final GourdwyrmPart[] segments = new GourdwyrmPart[SEGMENTS];
  private final Vec3[] path = new Vec3[HISTORY];
  private int pathHead = -1;

  private final ServerBossEvent bossEvent = new ServerBossEvent(UUID.randomUUID(), this.getDisplayName(), BossEvent.BossBarColor.RED,
      BossEvent.BossBarOverlay.NOTCHED_12);

  private @Nullable BlockPos anchor;
  private double circleAngle;
  private int phaseTicks;
  private int stage;
  private @Nullable Vec3 strikePoint;
  private boolean burrowNext = true;

  public Gourdwyrm(EntityType<? extends Gourdwyrm> type, Level level) {
    super(type, level);
    for (int i = 0; i < SEGMENTS; i++) {
      this.segments[i] = new GourdwyrmPart(this, i, SEGMENT_SIZE);
    }
    this.setId(ENTITY_COUNTER.getAndAdd(SEGMENTS + 1) + 1);
    this.noPhysics = true;
    this.setNoGravity(true);
    this.xpReward = 500;
    this.setPersistenceRequired();
    this.bossEvent.setCreateWorldFog(true);
    this.bossEvent.setDarkenScreen(true);
    this.bossEvent.setPlayBossMusic(true);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 600.0).add(Attributes.ARMOR, 6.0).add(Attributes.ATTACK_DAMAGE, 12.0)
        .add(Attributes.FOLLOW_RANGE, 96.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
  }

  public static @Nullable Gourdwyrm awaken(ServerLevel level, BlockPos anchor, Vec3 pos) {

    Gourdwyrm wyrm = ModEntities.GOURDWYRM.create(level, EntitySpawnReason.EVENT);
    if (wyrm == null) {
      return null;
    }
    wyrm.anchor = anchor;
    wyrm.snapTo(pos.x, pos.y, pos.z, 0.0F, 0.0F);
    level.addFreshEntity(wyrm);
    level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENDER_DRAGON_GROWL, wyrm.getSoundSource(), 4.0F, 0.6F);
    return wyrm;
  }

  @Override
  public void setId(int id) {
    super.setId(id);
    for (int i = 0; i < SEGMENTS; i++) {
      this.segments[i].setId(id + i + 1);
    }
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder entityData) {
    super.defineSynchedData(entityData);
    entityData.define(DATA_PHASE, Phase.CIRCLING.ordinal());
    entityData.define(DATA_LIT, 0);
    entityData.define(DATA_LENGTH, SEGMENTS);
  }

  @Override
  public boolean isMultipartEntity() {
    return true;
  }

  @Override
  public PartEntity<?>[] getParts() {
    return this.segments;
  }

  public GourdwyrmPart[] getSegments() {
    return this.segments;
  }

  public Phase getPhase() {
    return Phase.values()[Mth.clamp(this.entityData.get(DATA_PHASE), 0, Phase.values().length - 1)];
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
    this.recordPath();
    this.positionSegments();
    this.updateSegmentLights();
  }

  @Override
  public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
    super.onSyncedDataUpdated(accessor);
    if (DATA_LIT.equals(accessor) || DATA_LENGTH.equals(accessor)) {
      for (GourdwyrmPart segment : this.segments) {
        segment.refreshDimensions();
      }
    }
  }

  private void updateSegmentLights() {
    for (GourdwyrmPart segment : this.segments) {
      boolean shouldLight = this.isAlive() && this.isSegmentAlive(segment.index) && this.isSegmentLit(segment.index);
      boolean lit = LivingLights.has(segment);
      if (shouldLight && !lit) {
        LivingLights.add(segment, SEGMENT_LIGHT);
      } else if (!shouldLight && lit) {
        LivingLights.remove(segment);
      } else if (lit) {
        LivingLights.move(segment);
      }
    }
  }

  @Override
  public void onRemovedFromLevel() {
    super.onRemovedFromLevel();
    for (GourdwyrmPart segment : this.segments) {
      LivingLights.remove(segment);
    }
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

  private void recordPath() {

    Vec3 pos = this.position();
    if (this.pathHead < 0) {
      for (int i = 0; i < HISTORY; i++) {
        this.path[i] = pos;
      }
      this.pathHead = 0;
      return;
    }
    this.pathHead = (this.pathHead + 1) % HISTORY;
    this.path[this.pathHead] = pos;
  }

  private Vec3 pathSample(int ticksAgo) {
    return this.path[Math.floorMod(this.pathHead - ticksAgo, HISTORY)];
  }

  private void positionSegments() {

    float centerOffset = (this.getBbHeight() - SEGMENT_SIZE) * 0.5F;
    for (int i = 0; i < SEGMENTS; i++) {
      GourdwyrmPart segment = this.segments[i];
      Vec3 sample = this.pathSample((i + 1) * SPACING);
      segment.xo = segment.getX();
      segment.yo = segment.getY();
      segment.zo = segment.getZ();
      segment.xOld = segment.xo;
      segment.yOld = segment.yo;
      segment.zOld = segment.zo;
      segment.setPos(sample.x, sample.y + centerOffset, sample.z);
    }
  }

  @Override
  protected void customServerAiStep(ServerLevel level) {
    super.customServerAiStep(level);
    this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

    if (this.anchor == null) {
      this.anchor = this.blockPosition().below((int) CIRCLE_HEIGHT);
    }

    if (this.getLength() > SHED_LENGTH && this.getHealth() < this.getMaxHealth() * SHED_HEALTH) {
      this.shed(level);
    }

    this.phaseTicks++;
    switch (this.getPhase()) {
      case BURROW -> this.tickBurrow(level);
      case LANTERN_RINGS -> this.tickStunned(level);
      case FINAL -> this.tickCharge(level);
      default -> this.tickCircling(level);
    }

    if (this.getPhase() != Phase.LANTERN_RINGS && this.tickCount % LIT_INTERVAL == 0) {
      this.relightLanterns();
    }
  }

  private boolean isShed() {
    return this.getLength() <= SHED_LENGTH;
  }

  private double speed() {
    return this.isShed() ? FLY_SPEED * SHED_SPEED_MULTIPLIER : FLY_SPEED;
  }

  private void setPhase(Phase phase) {
    this.entityData.set(DATA_PHASE, phase.ordinal());
    this.phaseTicks = 0;
    this.stage = 0;
    this.strikePoint = null;
  }

  private @Nullable Player pickTarget(ServerLevel level) {
    if (this.anchor == null) {
      return null;
    }
    Vec3 center = Vec3.atBottomCenterOf(this.anchor);
    List<Player> players = level.getEntitiesOfClass(Player.class, new AABB(center, center).inflate(ARENA_RANGE),
        player -> player.isAlive() && EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(player));
    return players.isEmpty() ? null : players.get(this.random.nextInt(players.size()));
  }

  private void tickCircling(ServerLevel level) {

    this.circleAngle += CIRCLE_SPEED * (this.isShed() ? SHED_SPEED_MULTIPLIER : 1.0);
    Vec3 center = Vec3.atBottomCenterOf(this.anchor);
    Vec3 wanted = center.add(Math.cos(this.circleAngle) * CIRCLE_RADIUS, CIRCLE_HEIGHT + Math.sin(this.circleAngle * 3.0) * CIRCLE_BOB,
        Math.sin(this.circleAngle) * CIRCLE_RADIUS);
    this.flyToward(wanted, this.speed());

    if (this.phaseTicks % EMBER_INTERVAL == 0) {
      Player target = this.pickTarget(level);
      if (target != null) {
        this.spitEmber(level, target);
      }
    }

    if (this.phaseTicks >= (this.isShed() ? SHED_CIRCLE_TICKS : CIRCLE_TICKS) && this.pickTarget(level) != null) {
      boolean burrow = !this.isShed() || this.burrowNext;
      this.burrowNext = !this.burrowNext;
      this.setPhase(burrow ? Phase.BURROW : Phase.FINAL);
    }
  }

  private void spitEmber(ServerLevel level, Player target) {
    Vec3 mouth = this.getEyePosition();
    Vec3 aim = target.getEyePosition().subtract(mouth).normalize();
    SmallFireball ember = new SmallFireball(level, this, aim);
    ember.setPos(mouth.x, mouth.y, mouth.z);
    level.addFreshEntity(ember);
    this.playSound(SoundEvents.BLAZE_SHOOT, 3.0F, 0.5F);
  }

  private void tickBurrow(ServerLevel level) {

    if (this.stage == 0) {
      if (this.strikePoint == null) {
        Player target = this.pickTarget(level);
        if (target == null) {
          this.setPhase(Phase.CIRCLING);
          return;
        }
        this.strikePoint = new Vec3(target.getX(), Math.floor(target.getY()), target.getZ());
        this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 4.0F, 0.8F);
      }
      Vec3 below = this.strikePoint.subtract(0.0, BURROW_DEPTH, 0.0);
      this.flyToward(below, DIVE_SPEED);
      this.contactDamage(level);
      if (this.position().distanceToSqr(below) < 4.0 || this.phaseTicks >= DIVE_TICKS) {
        this.stage = 1;
        this.phaseTicks = 0;
        Player target = this.pickTarget(level);
        if (target != null && target.position().distanceToSqr(this.strikePoint) < 24.0 * 24.0) {
          this.strikePoint = new Vec3(target.getX(), Math.floor(target.getY()), target.getZ());
        }
      }
      return;
    }

    Vec3 point = this.strikePoint;
    if (this.stage == 1) {
      this.setDeltaMovement(Vec3.ZERO);
      double radius = GEYSER_RADIUS * this.phaseTicks / RIPPLE_TICKS;
      BlockState ground = level.getBlockState(BlockPos.containing(point).below());
      for (int i = 0; i < 12; i++) {
        double angle = Math.PI * 2.0 * i / 12.0 + this.phaseTicks * 0.2;
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ground), point.x + Math.cos(angle) * radius, point.y + 0.1,
            point.z + Math.sin(angle) * radius, 1, 0.05, 0.0, 0.05, 0.0);
      }
      if (this.phaseTicks % 10 == 0) {
        level.playSound(null, point.x, point.y, point.z, SoundEvents.WARDEN_DIG, SoundSource.HOSTILE, 2.0F, 0.6F + this.phaseTicks / (float) RIPPLE_TICKS * 0.4F);
      }
      if (this.phaseTicks >= RIPPLE_TICKS) {
        this.stage = 2;
        this.phaseTicks = 0;
        this.snapTo(point.x, point.y - BURROW_DEPTH * 0.5, point.z, this.getYRot(), this.getXRot());
        this.erupt(level, point);
      }
      return;
    }

    this.setDeltaMovement(0.0, GEYSER_SPEED, 0.0);
    this.contactDamage(level);
    if (this.getY() >= this.anchor.getY() + CIRCLE_HEIGHT) {
      this.setPhase(Phase.CIRCLING);
    }
  }

  private void erupt(ServerLevel level, Vec3 point) {

    level.playSound(null, point.x, point.y, point.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 3.0F, 0.7F);
    level.sendParticles(ParticleTypes.LAVA, point.x, point.y + 0.5, point.z, 40, GEYSER_RADIUS * 0.5, 0.5, GEYSER_RADIUS * 0.5, 0.2);
    level.sendParticles(ParticleTypes.FLAME, point.x, point.y + 1.0, point.z, 80, GEYSER_RADIUS * 0.5, 2.0, GEYSER_RADIUS * 0.5, 0.1);
    level.sendParticles(ParticleTypes.EXPLOSION, point.x, point.y + 1.0, point.z, 3, 1.0, 0.5, 1.0, 0.0);

    AABB area = new AABB(point, point).inflate(GEYSER_RADIUS, 2.0, GEYSER_RADIUS).expandTowards(0.0, 4.0, 0.0);
    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, area, entity -> entity != this && entity.isAlive())) {
      victim.hurtServer(level, this.damageSources().mobAttack(this), GEYSER_DAMAGE);
      victim.setDeltaMovement(victim.getDeltaMovement().add(0.0, 1.2, 0.0));
      victim.hurtMarked = true;
    }
  }

  private void tickStunned(ServerLevel level) {

    Vec3 rest = Vec3.atBottomCenterOf(this.anchor).add(0.0, STUN_HEIGHT, 0.0);
    this.flyToward(rest, FLY_SPEED * 0.4);
    if (this.phaseTicks % 5 == 0) {
      level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + this.getBbHeight(), this.getZ(), 4, 0.6, 0.3, 0.6, 0.02);
    }
    if (this.phaseTicks >= STUN_TICKS) {
      this.relightLanterns();
      this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 4.0F, 0.7F);
      this.setPhase(Phase.CIRCLING);
    }
  }

  private void stun() {
    this.setPhase(Phase.LANTERN_RINGS);
    this.playSound(SoundEvents.ENDER_DRAGON_HURT, 4.0F, 0.5F);
    this.playSound(SoundEvents.FIRE_EXTINGUISH, 4.0F, 0.5F);
  }

  private void tickCharge(ServerLevel level) {

    if (this.stage == 0) {
      Player target = this.pickTarget(level);
      if (target == null) {
        this.setPhase(Phase.CIRCLING);
        return;
      }
      this.setDeltaMovement(this.getDeltaMovement().scale(0.8));
      this.faceToward(target.position().subtract(this.position()));
      if (this.phaseTicks >= WINDUP_TICKS) {
        Vec3 aim = target.getEyePosition().subtract(this.position()).normalize();
        this.strikePoint = target.getEyePosition().add(aim.scale(CHARGE_OVERSHOOT));
        this.stage = 1;
        this.phaseTicks = 0;
        this.playSound(SoundEvents.ENDER_DRAGON_GROWL, 4.0F, 1.2F);
        level.sendParticles(ParticleTypes.FLAME, this.getX(), this.getY() + 1.0, this.getZ(), 60, 2.5, 2.5, 2.5, 0.2);
      }
      return;
    }

    this.flyToward(this.strikePoint, CHARGE_SPEED);
    this.contactDamage(level);
    if (this.phaseTicks >= CHARGE_TICKS || this.position().distanceToSqr(this.strikePoint) < 4.0) {
      this.setPhase(Phase.CIRCLING);
    }
  }

  private void contactDamage(ServerLevel level) {
    this.ram(level, this.getBoundingBox().inflate(HEAD_REACH));
    for (GourdwyrmPart segment : this.segments) {
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
      GourdwyrmPart segment = this.segments[i];
      level.sendParticles(ParticleTypes.EXPLOSION, segment.getX(), segment.getY() + 1.0, segment.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
      JackOMimic husk = ModEntities.JACK_O_MIMIC.create(level, EntitySpawnReason.EVENT);
      if (husk != null) {
        husk.snapTo(segment.getX(), segment.getY(), segment.getZ(), this.random.nextFloat() * 360.0F, 0.0F);
        level.addFreshEntity(husk);
      }
    }
    this.entityData.set(DATA_LENGTH, SHED_LENGTH);
    this.entityData.set(DATA_LIT, this.entityData.get(DATA_LIT) & ((1 << SHED_LENGTH) - 1));
    this.playSound(SoundEvents.ENDER_DRAGON_HURT, 4.0F, 0.6F);
    this.playSound(SoundEvents.WOOD_BREAK, 4.0F, 0.5F);
  }

  private void faceToward(Vec3 direction) {
    if (direction.horizontalDistanceSqr() > 1.0E-4) {
      float yRot = (float) (Mth.atan2(direction.z, direction.x) * Mth.RAD_TO_DEG) - 90.0F;
      this.setYRot(yRot);
      this.yBodyRot = yRot;
      this.yHeadRot = yRot;
    }
  }

  private void flyToward(Vec3 wanted, double speed) {

    Vec3 delta = wanted.subtract(this.position());
    Vec3 velocity = delta.lengthSqr() > speed * speed ? delta.normalize().scale(speed) : delta;
    this.setDeltaMovement(velocity);
    if (velocity.horizontalDistanceSqr() > 1.0E-4) {
      float yRot = (float) (Mth.atan2(velocity.z, velocity.x) * Mth.RAD_TO_DEG) - 90.0F;
      this.setYRot(yRot);
      this.yBodyRot = yRot;
      this.yHeadRot = yRot;
    }
  }

  @Override
  public void travel(Vec3 input) {
    this.move(MoverType.SELF, this.getDeltaMovement());
  }

  private void relightLanterns() {

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
        this.stun();
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
  public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource damageSource) {
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
    output.storeNullable("anchor", BlockPos.CODEC, this.anchor);
    output.putInt("length", this.getLength());
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    this.anchor = input.read("anchor", BlockPos.CODEC).orElse(null);
    this.entityData.set(DATA_LENGTH, input.getIntOr("length", SEGMENTS));
    this.bossEvent.setName(this.getDisplayName());
  }

  public enum Phase {
    CIRCLING,
    BURROW,
    LANTERN_RINGS,
    SHED,
    FINAL
  }
}
