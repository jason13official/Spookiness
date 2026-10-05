package io.github.jason13official.spookiness.entity;

import io.github.jason13official.spookiness.companion.PlayerFollower;
import io.github.jason13official.spookiness.companion.PlayerFollowers;
import io.github.jason13official.spookiness.lighting.LivingLights;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SpectralJackOMimic extends FloatingPathfinderMob implements OwnableEntity, PlayerFollower {

  private static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> DATA_OWNER = SynchedEntityData.defineId(SpectralJackOMimic.class,
      EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);

  private static final int LIGHT_EMISSION = 10;
  private static final byte ATTACK_EVENT = 4;
  private static final double FOLLOW_START_DISTANCE = 6.0;
  private static final double FOLLOW_STOP_DISTANCE = 3.0;
  private static final double TELEPORT_DISTANCE = 20.0;

  public final AnimationState yapAnimationState = new AnimationState();

  public SpectralJackOMimic(EntityType<? extends SpectralJackOMimic> type, Level level) {
    super(type, level);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return createFloatingAttributes().add(Attributes.MAX_HEALTH, 12.0F).add(Attributes.ATTACK_DAMAGE, 4.0F).add(Attributes.FLYING_SPEED, 0.3F)
        .add(Attributes.FOLLOW_RANGE, 16.0F);
  }

  @Override
  protected void registerGoals() {

    int goalPriority = 1;

    this.goalSelector.addGoal(goalPriority++, new MeleeAttackGoal(this, 1.4, true));
    this.goalSelector.addGoal(goalPriority++, new FollowOwnerGoal());
    this.goalSelector.addGoal(goalPriority++, new WaterAvoidingRandomFlyingGoal(this, 0.6));
    this.goalSelector.addGoal(goalPriority++, new LookAtPlayerGoal(this, Player.class, 8.0F));
    this.goalSelector.addGoal(goalPriority++, new RandomLookAroundGoal(this));

    int targetPriority = 1;

    this.targetSelector.addGoal(targetPriority++, new DefendOwnerGoal(true));
    this.targetSelector.addGoal(targetPriority++, new DefendOwnerGoal(false));
    this.targetSelector.addGoal(targetPriority++, new HurtByTargetGoal(this));
    this.targetSelector.addGoal(targetPriority++, new NearestAttackableTargetGoal<>(this, Mob.class, true,
        (target, level) -> target instanceof Enemy && this.wantsToAttack(target)));
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder entityData) {
    super.defineSynchedData(entityData);
    entityData.define(DATA_OWNER, Optional.empty());
  }

  @Override
  public @Nullable EntityReference<LivingEntity> getOwnerReference() {
    return this.entityData.get(DATA_OWNER).orElse(null);
  }

  @Override
  public @Nullable UUID getOwnerUUID() {
    EntityReference<LivingEntity> owner = this.getOwnerReference();
    return owner == null ? null : owner.getUUID();
  }

  private void setOwnerReference(@Nullable EntityReference<LivingEntity> owner) {
    this.entityData.set(DATA_OWNER, Optional.ofNullable(owner));
    PlayerFollowers.track(this);
  }

  public void setOwner(LivingEntity owner) {
    this.setOwnerReference(EntityReference.of(owner));
    this.setPersistenceRequired();
  }

  private boolean isOwner(Entity entity) {
    EntityReference<LivingEntity> owner = this.getOwnerReference();
    return owner != null && owner.getUUID().equals(entity.getUUID());
  }

  @Override
  public void push(Entity entity) {
    if (!this.isOwner(entity)) {
      super.push(entity);
    }
  }

  @Override
  protected void doPush(Entity entity) {
    if (!this.isOwner(entity)) {
      super.doPush(entity);
    }
  }

  public boolean wantsToAttack(LivingEntity target) {
    if (target instanceof Creeper || target instanceof Ghast || target instanceof ArmorStand) {
      return false;
    }
    return !this.considersEntityAsAlly(target);
  }

  @Override
  protected boolean considersEntityAsAlly(Entity other) {
    UUID ownerId = this.getOwnerUUID();
    if (ownerId != null) {
      if (ownerId.equals(other.getUUID())) {
        return true;
      }
      if (other instanceof SpectralJackOMimic companion && ownerId.equals(companion.getOwnerUUID())) {
        return true;
      }
    }
    return super.considersEntityAsAlly(other);
  }

  @Override
  protected void addAdditionalSaveData(ValueOutput output) {
    super.addAdditionalSaveData(output);
    EntityReference.store(this.getOwnerReference(), output, "owner");
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    this.setOwnerReference(EntityReference.read(input, "owner"));
  }

  @Override
  public boolean doHurtTarget(ServerLevel level, Entity target) {
    level.broadcastEntityEvent(this, ATTACK_EVENT);
    return super.doHurtTarget(level, target);
  }

  @Override
  public void handleEntityEvent(byte id) {
    if (id == ATTACK_EVENT) {
      this.yapAnimationState.start(this.tickCount);
    } else {
      super.handleEntityEvent(id);
    }
  }

  @Override
  public boolean removeWhenFarAway(double distSqr) {
    return false;
  }

  @Override
  public boolean canBeLeashed() {
    return false;
  }

  @Override
  public boolean shouldShowName() {
    return this.hasCustomName() && super.shouldShowName();
  }

  @Override
  public float getLightLevelDependentMagicValue() {
    return 1.0F;
  }

  @Override
  public void onAddedToLevel() {
    super.onAddedToLevel();
    LivingLights.add(this, LIGHT_EMISSION);
    PlayerFollowers.track(this);
  }

  @Override
  public void onRemovedFromLevel() {
    super.onRemovedFromLevel();
    LivingLights.remove(this);
    PlayerFollowers.untrack(this);
  }

  @Override
  public void tick() {
    super.tick();
    LivingLights.move(this);
  }

  @Override
  protected SoundEvent getAmbientSound() {
    return SoundEvents.VEX_AMBIENT;
  }

  @Override
  protected SoundEvent getHurtSound(DamageSource source) {
    return SoundEvents.VEX_HURT;
  }

  @Override
  protected SoundEvent getDeathSound() {
    return SoundEvents.VEX_DEATH;
  }

  @Override
  public float getVoicePitch() {
    return 0.6F;
  }

  private class FollowOwnerGoal extends Goal {

    private @Nullable LivingEntity followed;

    FollowOwnerGoal() {
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
      LivingEntity owner = SpectralJackOMimic.this.getOwner();
      if (owner == null || owner.isSpectator() || SpectralJackOMimic.this.distanceTo(owner) < FOLLOW_START_DISTANCE) {
        return false;
      }
      this.followed = owner;
      return true;
    }

    @Override
    public boolean canContinueToUse() {
      return this.followed != null && this.followed.isAlive() && !this.followed.isSpectator()
          && SpectralJackOMimic.this.distanceTo(this.followed) > FOLLOW_STOP_DISTANCE;
    }

    @Override
    public void stop() {
      this.followed = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
      return true;
    }

    @Override
    public void tick() {
      if (this.followed == null) {
        return;
      }

      SpectralJackOMimic mimic = SpectralJackOMimic.this;
      Vec3 anchor = this.followed.position().add(0.0, 1.0, 0.0);
      mimic.getLookControl().setLookAt(this.followed, 10.0F, mimic.getMaxHeadXRot());

      if (mimic.distanceTo(this.followed) > TELEPORT_DISTANCE) {
        mimic.teleportTo(anchor.x, anchor.y, anchor.z);
        mimic.setDeltaMovement(Vec3.ZERO);
        mimic.getNavigation().stop();
        return;
      }

      mimic.getMoveControl().setWantedPosition(anchor.x, anchor.y, anchor.z, 1.2);
    }
  }

  private class DefendOwnerGoal extends TargetGoal {

    private final boolean retaliate;
    private @Nullable LivingEntity candidate;
    private int timestamp;

    DefendOwnerGoal(boolean retaliate) {
      super(SpectralJackOMimic.this, false);
      this.retaliate = retaliate;
      this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    public boolean canUse() {
      LivingEntity owner = SpectralJackOMimic.this.getOwner();
      if (owner == null) {
        return false;
      }
      this.candidate = this.retaliate ? owner.getLastHurtByMob() : owner.getLastHurtMob();
      int ts = this.retaliate ? owner.getLastHurtByMobTimestamp() : owner.getLastHurtMobTimestamp();
      return ts != this.timestamp && this.candidate != null && this.canAttack(this.candidate, TargetingConditions.DEFAULT)
          && SpectralJackOMimic.this.wantsToAttack(this.candidate);
    }

    @Override
    public void start() {
      this.mob.setTarget(this.candidate);
      LivingEntity owner = SpectralJackOMimic.this.getOwner();
      if (owner != null) {
        this.timestamp = this.retaliate ? owner.getLastHurtByMobTimestamp() : owner.getLastHurtMobTimestamp();
      }
      super.start();
    }
  }
}
