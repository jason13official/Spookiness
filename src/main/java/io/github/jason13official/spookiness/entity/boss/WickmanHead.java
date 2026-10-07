package io.github.jason13official.spookiness.entity.boss;

import io.github.jason13official.spookiness.entity.FloatingPathfinderMob;
import io.github.jason13official.spookiness.lighting.LanternHeads;
import io.github.jason13official.spookiness.lighting.LightEmitter;
import io.github.jason13official.spookiness.registry.ModEntities;
import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class WickmanHead extends FloatingPathfinderMob implements Enemy, LightEmitter {

  private static final EntityDataAccessor<Boolean> DATA_FROST = SynchedEntityData.defineId(WickmanHead.class, EntityDataSerializers.BOOLEAN);

  private static final float DAMAGE_MULTIPLIER = 1.5F;
  private static final int THROW_INTERVAL = 40;
  private static final int ORPHAN_TICKS = 40;
  private static final double LEASH_MAX = 10.0;
  private static final double LEASH_RETURN = 6.0;

  private @Nullable UUID body;
  private int orphanTicks;

  public WickmanHead(EntityType<? extends WickmanHead> type, Level level) {
    super(type, level);
    this.setPersistenceRequired();
  }

  public static AttributeSupplier.Builder createAttributes() {
    return createFloatingAttributes().add(Attributes.MAX_HEALTH, 1024.0).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.FLYING_SPEED, 0.45)
        .add(Attributes.FOLLOW_RANGE, 40.0);
  }

  public static @Nullable WickmanHead detach(ServerLevel level, Wickman body) {

    WickmanHead head = ModEntities.WICKMAN_HEAD.create(level, EntitySpawnReason.MOB_SUMMONED);
    if (head == null) {
      return null;
    }
    Vec3 eye = body.getEyePosition();
    head.body = body.getUUID();
    head.entityData.set(DATA_FROST, body.getVariant() == Wickman.Variant.FROST);
    head.snapTo(eye.x, eye.y, eye.z, body.getYHeadRot(), 0.0F);
    head.setDeltaMovement(0.0, 0.6, 0.0);
    head.setTarget(body.getTarget());
    level.addFreshEntity(head);
    level.sendParticles(head.isFrost() ? ParticleTypes.SNOWFLAKE : ParticleTypes.FLAME, eye.x, eye.y, eye.z, 40, 0.3, 0.3, 0.3, 0.1);
    level.playSound(null, eye.x, eye.y, eye.z, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, head.getSoundSource(), 1.5F, 0.6F);
    return head;
  }

  @Override
  protected void registerGoals() {

    this.goalSelector.addGoal(0, new ReturnToBodyGoal());
    this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.3, true));
    this.goalSelector.addGoal(2, new WaterAvoidingRandomFlyingGoal(this, 0.8));
    this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 16.0F));
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder entityData) {
    super.defineSynchedData(entityData);
    entityData.define(DATA_FROST, false);
  }

  public boolean isFrost() {
    return this.entityData.get(DATA_FROST);
  }

  @Override
  public int getLightEmission() {
    return this.isFrost() ? 0 : LanternHeads.EMISSION;
  }

  @Override
  protected Component getTypeName() {
    return Component.translatable(this.isFrost() ? "entity.spookiness.frostwick" : "entity.spookiness.wickman");
  }

  public @Nullable Wickman getBody() {
    return this.body != null && this.level() instanceof ServerLevel level && level.getEntity(this.body) instanceof Wickman wickman ? wickman : null;
  }

  @Override
  protected void customServerAiStep(ServerLevel level) {
    super.customServerAiStep(level);

    Wickman body = this.getBody();
    if (body == null || !body.isAlive()) {
      if (++this.orphanTicks > ORPHAN_TICKS) {
        this.burnOut(level);
      }
      return;
    }
    this.orphanTicks = 0;

    LivingEntity target = body.getTarget();
    if (target != null && target != this.getTarget()) {
      this.setTarget(target);
    }
    target = this.getTarget();
    if (target != null && this.tickCount % THROW_INTERVAL == 0 && this.hasLineOfSight(target)) {
      this.throwAt(level, target);
    }
  }

  private void throwAt(ServerLevel level, LivingEntity target) {

    Vec3 eye = this.getEyePosition();
    Vec3 delta = target.getEyePosition().subtract(eye);
    if (this.isFrost()) {
      Snowball snowball = new Snowball(level, this, new ItemStack(Items.SNOWBALL));
      snowball.shoot(delta.x, delta.y + delta.horizontalDistance() * 0.2, delta.z, 1.6F, 4.0F);
      level.addFreshEntity(snowball);
      this.playSound(SoundEvents.SNOW_GOLEM_SHOOT, 1.0F, 0.8F);
      return;
    }
    SmallFireball fireball = new SmallFireball(level, this, delta.normalize());
    fireball.setPos(eye.x, eye.y, eye.z);
    level.addFreshEntity(fireball);
    this.playSound(SoundEvents.BLAZE_SHOOT, 1.0F, 0.9F);
  }

  public void burnOut(ServerLevel level) {

    level.sendParticles(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 30, 0.3, 0.3, 0.3, 0.05);
    level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.FIRE_EXTINGUISH, this.getSoundSource(), 1.5F, 0.6F);
    this.discard();
  }

  @Override
  public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {

    Wickman body = this.getBody();
    if (body == null || this.isInvulnerableToBase(source) || source.getEntity() == body) {
      return false;
    }
    this.markHurt();
    this.hurtTime = this.hurtDuration = 10;
    level.broadcastDamageEvent(this, source);
    if (source.getEntity() instanceof LivingEntity attacker && attacker != body && !(attacker instanceof WickmanHead)) {
      body.setTarget(attacker);
      this.setTarget(attacker);
    }
    return body.hurtServer(level, source, damage * DAMAGE_MULTIPLIER);
  }

  @Override
  protected boolean considersEntityAsAlly(Entity other) {
    return other instanceof Wickman || super.considersEntityAsAlly(other);
  }

  @Override
  public boolean removeWhenFarAway(double distSqr) {
    return false;
  }

  @Override
  public boolean fireImmune() {
    return true;
  }

  @Override
  protected void addAdditionalSaveData(ValueOutput output) {
    super.addAdditionalSaveData(output);
    output.storeNullable("body", UUIDUtil.CODEC, this.body);
    output.putBoolean("frost", this.isFrost());
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    this.body = input.read("body", UUIDUtil.CODEC).orElse(null);
    this.entityData.set(DATA_FROST, input.getBooleanOr("frost", false));
  }

  private final class ReturnToBodyGoal extends Goal {

    ReturnToBodyGoal() {
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
      Wickman body = WickmanHead.this.getBody();
      return body != null && WickmanHead.this.distanceTo(body) > LEASH_MAX;
    }

    @Override
    public boolean canContinueToUse() {
      Wickman body = WickmanHead.this.getBody();
      return body != null && WickmanHead.this.distanceTo(body) > LEASH_RETURN;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
      return true;
    }

    @Override
    public void tick() {
      Wickman body = WickmanHead.this.getBody();
      if (body != null) {
        Vec3 above = body.position().add(0.0, body.getBbHeight() + 0.5, 0.0);
        WickmanHead.this.getNavigation().stop();
        WickmanHead.this.getMoveControl().setWantedPosition(above.x, above.y, above.z, 1.4);
      }
    }
  }
}
