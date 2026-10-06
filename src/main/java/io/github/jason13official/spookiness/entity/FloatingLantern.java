package io.github.jason13official.spookiness.entity;

import io.github.jason13official.spookiness.companion.PlayerFollower;
import io.github.jason13official.spookiness.companion.PlayerFollowers;
import io.github.jason13official.spookiness.effect.SoulBurst;
import io.github.jason13official.spookiness.lighting.LivingLights;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class FloatingLantern extends FloatingPathfinderMob implements PlayerFollower {

  private static final int LIGHT_EMISSION = 10;
  private static final double WILD_SEEK_RANGE = 16.0;
  private static final double GUARD_RANGE = 10.0;
  private static final double HOVER_HEIGHT = 1.5;
  private static final double SEAR_RANGE = 2.5;
  private static final int SEAR_INTERVAL = 40;
  private static final float SEAR_SECONDS = 3.0F;
  private static final double OWNER_OFFSET = 1.2;
  private static final double TELEPORT_DISTANCE = 16.0;

  private @Nullable EntityReference<Player> owner;

  public FloatingLantern(EntityType<? extends FloatingLantern> type, Level level) {
    super(type, level);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return createFloatingAttributes().add(Attributes.MAX_HEALTH, 6.0F).add(Attributes.FLYING_SPEED, 0.15F);
  }

  @Override
  protected void registerGoals() {

    int goalPriority = 1;

    this.goalSelector.addGoal(goalPriority++, new SearGoal());
    this.goalSelector.addGoal(goalPriority++, new FollowOwnerGoal());
    this.goalSelector.addGoal(goalPriority++, new WaterAvoidingRandomFlyingGoal(this, 1.0) {

      @Override
      public boolean canUse() {
        return !FloatingLantern.this.isClaimed() && super.canUse();
      }
    });
    this.goalSelector.addGoal(goalPriority++, new LookAtPlayerGoal(this, Player.class, 6.0F));
    this.goalSelector.addGoal(goalPriority++, new RandomLookAroundGoal(this));
  }

  @Override
  protected void addAdditionalSaveData(ValueOutput output) {
    super.addAdditionalSaveData(output);
    EntityReference.store(this.owner, output, "owner");
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    this.owner = EntityReference.read(input, "owner");
  }

  @Override
  public @Nullable UUID getOwnerUUID() {
    return this.owner == null ? null : this.owner.getUUID();
  }

  public boolean isClaimed() {
    return this.owner != null;
  }

  private @Nullable Player getOwner() {
    return this.owner == null ? null : EntityReference.getPlayer(this.owner, this.level());
  }

  public boolean claim(ServerLevel level, Player player) {
    if (this.isClaimed()) {
      return false;
    }
    if (player instanceof ServerPlayer serverPlayer && PlayerFollowers.count(serverPlayer, mob -> mob instanceof FloatingLantern) > 0) {
      level.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.4, this.getZ(), 8, 0.15, 0.15, 0.15, 0.01);
      this.playSound(SoundEvents.CANDLE_EXTINGUISH, 1.0F, 0.6F);
      return false;
    }

    this.owner = EntityReference.of(player);
    this.setPersistenceRequired();
    this.getNavigation().stop();
    PlayerFollowers.track(this);
    SoulBurst.spawn(level, this.getBoundingBox().getCenter(), 16, 0.25, 0.04);
    this.playSound(SoundEvents.LANTERN_PLACE, 1.0F, 0.6F);
    return true;
  }

  private void release(ServerLevel level) {
    level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.LANTERN_PLACE, SoundSource.NEUTRAL, 1.0F, 1.0F);
    this.spawnAtLocation(level, new ItemStack(Items.SOUL_LANTERN));
    this.discard();
  }

  @Override
  protected InteractionResult mobInteract(Player player, InteractionHand hand) {
    if (!(this.level() instanceof ServerLevel level)) {
      return InteractionResult.SUCCESS;
    }
    if (this.isClaimed()) {
      if (player.isSecondaryUseActive() && this.owner != null && this.owner.matches(player)) {
        this.release(level);
        return InteractionResult.SUCCESS_SERVER;
      }
      return super.mobInteract(player, hand);
    }
    this.claim(level, player);
    return InteractionResult.SUCCESS_SERVER;
  }

  public BlockState getLanternState() {
    return Blocks.SOUL_LANTERN.defaultBlockState().setValue(LanternBlock.HANGING, true);
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
    if (this.level().isClientSide() && this.isAlive() && this.random.nextInt(6) == 0) {
      this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME, this.getX() + (this.random.nextDouble() - 0.5) * 0.2, this.getY() + 0.2,
          this.getZ() + (this.random.nextDouble() - 0.5) * 0.2, 0.0, 0.01, 0.0);
    }
  }

  @Override
  protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
    super.dropCustomDeathLoot(level, source, killedByPlayer);
    this.spawnAtLocation(level, new ItemStack(Items.SOUL_LANTERN));
  }

  @Override
  public boolean canBeLeashed() {
    return false;
  }

  @Override
  protected SoundEvent getHurtSound(DamageSource source) {
    return SoundEvents.LANTERN_HIT;
  }

  @Override
  protected SoundEvent getDeathSound() {
    return SoundEvents.LANTERN_BREAK;
  }

  private boolean isSearable(Mob mob) {
    return mob.isAlive() && mob.is(EntityTypeTags.BURN_IN_DAYLIGHT) && PlayerFollowers.ownerOf(mob) == null && !mob.fireImmune();
  }

  private class SearGoal extends Goal {

    private @Nullable Mob prey;
    private int searCooldown;

    SearGoal() {
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    private @Nullable Mob findPrey() {
      FloatingLantern lantern = FloatingLantern.this;
      Player player = lantern.getOwner();
      Entity center = lantern.isClaimed() ? player : lantern;
      if (center == null) {
        return null;
      }
      double range = lantern.isClaimed() ? GUARD_RANGE : WILD_SEEK_RANGE;
      return lantern.level().getEntitiesOfClass(Mob.class, center.getBoundingBox().inflate(range), lantern::isSearable).stream()
          .min(Comparator.comparingDouble(center::distanceToSqr)).orElse(null);
    }

    @Override
    public boolean canUse() {
      if (FloatingLantern.this.tickCount % 10 != 0) {
        return false;
      }
      this.prey = this.findPrey();
      return this.prey != null;
    }

    @Override
    public boolean canContinueToUse() {
      FloatingLantern lantern = FloatingLantern.this;
      if (this.prey == null || !lantern.isSearable(this.prey)) {
        return false;
      }
      Player player = lantern.getOwner();
      return player == null || this.prey.distanceTo(player) <= GUARD_RANGE * 1.5;
    }

    @Override
    public void stop() {
      this.prey = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
      return true;
    }

    @Override
    public void tick() {
      FloatingLantern lantern = FloatingLantern.this;
      if (this.prey == null) {
        return;
      }

      Vec3 above = this.prey.position().add(0.0, this.prey.getBbHeight() + HOVER_HEIGHT, 0.0);
      lantern.getLookControl().setLookAt(this.prey);
      lantern.getMoveControl().setWantedPosition(above.x, above.y, above.z, 1.5);

      if (this.searCooldown > 0) {
        this.searCooldown--;
        return;
      }
      if (lantern.distanceToSqr(above) <= SEAR_RANGE * SEAR_RANGE && lantern.level() instanceof ServerLevel level) {
        this.prey.igniteForSeconds(SEAR_SECONDS);
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.prey.getX(), this.prey.getY() + this.prey.getBbHeight() * 0.5, this.prey.getZ(), 12,
            this.prey.getBbWidth() * 0.4, this.prey.getBbHeight() * 0.3, this.prey.getBbWidth() * 0.4, 0.02);
        lantern.playSound(SoundEvents.SOUL_ESCAPE.value(), 1.0F, 0.8F + lantern.random.nextFloat() * 0.4F);
        this.searCooldown = SEAR_INTERVAL;
      }
    }
  }

  private class FollowOwnerGoal extends Goal {

    private @Nullable Player player;

    FollowOwnerGoal() {
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
      this.player = FloatingLantern.this.getOwner();
      return this.player != null && !this.player.isSpectator();
    }

    @Override
    public boolean canContinueToUse() {
      return this.player != null && this.player.isAlive() && !this.player.isRemoved() && !this.player.isSpectator();
    }

    @Override
    public void stop() {
      this.player = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
      return true;
    }

    @Override
    public void tick() {
      FloatingLantern lantern = FloatingLantern.this;
      if (this.player == null) {
        return;
      }

      float yaw = this.player.getYRot() * Mth.DEG_TO_RAD;
      Vec3 shoulder = this.player.position().add(-Mth.cos(yaw) * OWNER_OFFSET, this.player.getBbHeight() + 0.3, -Mth.sin(yaw) * OWNER_OFFSET);
      double distance = lantern.position().distanceTo(shoulder);
      lantern.getLookControl().setLookAt(this.player);

      if (distance > TELEPORT_DISTANCE) {
        lantern.teleportTo(shoulder.x, shoulder.y, shoulder.z);
        lantern.setDeltaMovement(Vec3.ZERO);
        return;
      }
      if (distance > 0.3) {
        lantern.getMoveControl().setWantedPosition(shoulder.x, shoulder.y, shoulder.z, Mth.clamp(distance * 1.5, 0.5, 6.0));
      }
    }
  }
}
