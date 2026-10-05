package io.github.jason13official.spookiness.entity.boss;

import io.github.jason13official.spookiness.registry.ModEntities;
import io.github.jason13official.spookiness.world.netherrealm.GourdwyrmFight;
import java.util.UUID;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

  private final GourdwyrmPart[] segments = new GourdwyrmPart[SEGMENTS];
  private final Vec3[] path = new Vec3[HISTORY];
  private int pathHead = -1;

  private final ServerBossEvent bossEvent = new ServerBossEvent(UUID.randomUUID(), this.getDisplayName(), BossEvent.BossBarColor.RED,
      BossEvent.BossBarOverlay.NOTCHED_12);

  private @Nullable BlockPos anchor;
  private double circleAngle;

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

    switch (this.getPhase()) {
      case CIRCLING -> this.tickCircling();
      default -> this.tickCircling();
    }

    if (this.tickCount % LIT_INTERVAL == 0) {
      this.relightLanterns();
    }
  }

  private void tickCircling() {

    this.circleAngle += CIRCLE_SPEED;
    Vec3 center = Vec3.atBottomCenterOf(this.anchor);
    Vec3 wanted = center.add(Math.cos(this.circleAngle) * CIRCLE_RADIUS, CIRCLE_HEIGHT + Math.sin(this.circleAngle * 3.0) * CIRCLE_BOB,
        Math.sin(this.circleAngle) * CIRCLE_RADIUS);
    this.flyToward(wanted);
  }

  private void flyToward(Vec3 wanted) {

    Vec3 delta = wanted.subtract(this.position());
    Vec3 velocity = delta.lengthSqr() > FLY_SPEED * FLY_SPEED ? delta.normalize().scale(FLY_SPEED) : delta;
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
      this.entityData.set(DATA_LIT, this.entityData.get(DATA_LIT) & ~(1 << segment.index));
      this.playSound(SoundEvents.CANDLE_EXTINGUISH, 3.0F, 0.6F);
    }
    return this.reallyHurt(level, source, lit ? damage : damage * UNLIT_MULTIPLIER);
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
    this.spawnAtLocation(level, new ItemStack(Items.JACK_O_LANTERN, 8));
    if (this.anchor != null) {
      GourdwyrmFight.get(level).markDefeated(this.anchor);
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
    output.putInt("phase", this.getPhase().ordinal());
    output.putInt("length", this.getLength());
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    this.anchor = input.read("anchor", BlockPos.CODEC).orElse(null);
    this.entityData.set(DATA_PHASE, input.getIntOr("phase", 0));
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
