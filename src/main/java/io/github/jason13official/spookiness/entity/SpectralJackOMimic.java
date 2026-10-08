package io.github.jason13official.spookiness.entity;

import io.github.jason13official.spookiness.util.SpookyMath;
import io.github.jason13official.spookiness.companion.Allies;
import io.github.jason13official.spookiness.companion.DefendOwnerGoal;
import io.github.jason13official.spookiness.companion.FollowOwnerGoal;
import io.github.jason13official.living_lights.api.common.lighting.LightEmitter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SpectralJackOMimic extends FloatingCompanion implements LightEmitter {

  private static final double CROWD_DISTANCE = 1.5;
  private static final double SLOT_RADIUS = 2.5;
  private static final double SLOT_HEIGHT = 1.2;
  private static final double SLOT_TOLERANCE = 0.75;

  private static final int LIGHT_EMISSION = 10;
  private static final byte ATTACK_EVENT = 4;
  private static final double FOLLOW_START_DISTANCE = 6.0;
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
    this.goalSelector.addGoal(goalPriority++, new FollowGoal());
    this.goalSelector.addGoal(goalPriority++, new WaterAvoidingRandomFlyingGoal(this, 0.6));
    this.goalSelector.addGoal(goalPriority++, new LookAtPlayerGoal(this, Player.class, 8.0F));
    this.goalSelector.addGoal(goalPriority++, new RandomLookAroundGoal(this));

    int targetPriority = 1;

    this.targetSelector.addGoal(targetPriority++, new DefendOwnerGoal(this, true, this::wantsToAttack));
    this.targetSelector.addGoal(targetPriority++, new DefendOwnerGoal(this, false, this::wantsToAttack));
    this.targetSelector.addGoal(targetPriority++, new HurtByTargetGoal(this));
    this.targetSelector.addGoal(targetPriority++, new NearestAttackableTargetGoal<>(this, Mob.class, true,
        (target, level) -> target instanceof Enemy && this.wantsToAttack(target)));
  }

  @Override
  public boolean befriend(ServerLevel level, Player player) {
    this.setOwner(player);
    return true;
  }

  @Override
  public void push(Entity entity) {
    if (!this.isOwnedBy(entity)) {
      super.push(entity);
    }
  }

  @Override
  protected void doPush(Entity entity) {
    if (!this.isOwnedBy(entity)) {
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
    return Allies.isAlly(this, other) || super.considersEntityAsAlly(other);
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
  public boolean shouldShowName() {
    return this.hasCustomName() && super.shouldShowName();
  }

  @Override
  public float getLightLevelDependentMagicValue() {
    return 1.0F;
  }

  @Override
  public int getLightEmission() {
    return LIGHT_EMISSION;
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

  private class FollowGoal extends FollowOwnerGoal {

    FollowGoal() {
      super(SpectralJackOMimic.this, TELEPORT_DISTANCE);
    }

    @Override
    protected boolean canStart(Player owner) {
      double distance = this.mob.distanceTo(owner);
      return distance >= FOLLOW_START_DISTANCE || distance <= CROWD_DISTANCE;
    }

    @Override
    protected boolean canKeepFollowing(Player owner) {
      return this.mob.position().distanceTo(this.anchor(owner)) > SLOT_TOLERANCE;
    }

    @Override
    protected Vec3 anchor(Entity leader) {
      return SpookyMath.onRing(leader.position(), this.mob.getId() * SpookyMath.GOLDEN_ANGLE, SLOT_RADIUS).add(0.0, SLOT_HEIGHT, 0.0);
    }

    @Override
    protected void approach(Entity leader, Vec3 anchor, double distance) {
      this.mob.getMoveControl().setWantedPosition(anchor.x, anchor.y, anchor.z, Mth.clamp(distance * 0.5, 0.6, 2.0));
    }
  }
}
