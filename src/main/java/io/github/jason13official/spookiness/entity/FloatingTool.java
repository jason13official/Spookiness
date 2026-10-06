package io.github.jason13official.spookiness.entity;

import java.util.EnumSet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class FloatingTool extends FloatingPathfinderMob {

  private static final float FADE_PER_TICK = 0.08F;

  private float visibility;
  private float oVisibility;

  protected FloatingTool(EntityType<? extends FloatingTool> type, Level level) {
    super(type, level);
    this.xpReward = 5;
  }

  public static AttributeSupplier.Builder createAttributes() {
    return createFloatingAttributes().add(Attributes.MAX_HEALTH, 14.0F).add(Attributes.ATTACK_DAMAGE, 3.0F).add(Attributes.FOLLOW_RANGE, 24.0F).add(Attributes.FLYING_SPEED, 0.3F);
  }

  @Override
  public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
    RandomSource random = level.getRandom();
    if (this.getMainHandItem().isEmpty()) {
      this.populateDefaultEquipmentSlots(random, difficulty);
      this.populateDefaultEquipmentEnchantments(level, random, difficulty);
    }
    return super.finalizeSpawn(level, difficulty, spawnReason, groupData);
  }

  @Override
  public void tick() {
    super.tick();
    if (this.level().isClientSide()) {
      this.oVisibility = this.visibility;
      this.visibility = Mth.approach(this.visibility, this.isAggressive() ? 1.0F : 0.0F, FADE_PER_TICK);
    }
  }

  public float getVisibility(float partialTicks) {
    return Mth.lerp(partialTicks, this.oVisibility, this.visibility);
  }

  @Override
  public float getLightLevelDependentMagicValue() {
    return 1.0F;
  }

  @Override
  protected SoundEvent getHurtSound(DamageSource source) {
    return SoundEvents.VEX_HURT;
  }

  @Override
  protected SoundEvent getDeathSound() {
    return SoundEvents.VEX_DEATH;
  }

  protected abstract class WorkGoal extends Goal {

    private static final int MIN_COOLDOWN = 100;
    private static final int MAX_COOLDOWN = 300;
    private static final int RETRY_COOLDOWN = 40;
    private static final int TIMEOUT = 200;

    private int nextWorkTick;
    private int timeout;
    private boolean working;

    protected WorkGoal() {
      this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    protected abstract boolean findWork(ServerLevel level);

    protected abstract @Nullable Vec3 workSite();

    protected abstract void work(ServerLevel level);

    protected double reachSqr() {
      return 4.0;
    }

    @Override
    public boolean canUse() {
      FloatingTool tool = FloatingTool.this;
      if (tool.getTarget() != null || tool.tickCount < this.nextWorkTick || !(tool.level() instanceof ServerLevel level)) {
        return false;
      }
      if (!this.findWork(level)) {
        this.nextWorkTick = tool.tickCount + RETRY_COOLDOWN;
        return false;
      }
      return true;
    }

    @Override
    public boolean canContinueToUse() {
      return this.working && this.timeout > 0 && FloatingTool.this.getTarget() == null && this.workSite() != null;
    }

    @Override
    public void start() {
      this.working = true;
      this.timeout = TIMEOUT;
      Vec3 site = this.workSite();
      if (site != null) {
        FloatingTool.this.getNavigation().moveTo(site.x, site.y, site.z, 1.0);
      }
    }

    @Override
    public void stop() {
      this.working = false;
      FloatingTool.this.getNavigation().stop();
      this.nextWorkTick = FloatingTool.this.tickCount + Mth.nextInt(FloatingTool.this.random, MIN_COOLDOWN, MAX_COOLDOWN);
    }

    @Override
    public boolean requiresUpdateEveryTick() {
      return true;
    }

    @Override
    public void tick() {
      FloatingTool tool = FloatingTool.this;
      Vec3 site = this.workSite();
      if (site == null || !(tool.level() instanceof ServerLevel level)) {
        return;
      }

      this.timeout--;
      tool.getLookControl().setLookAt(site.x, site.y, site.z);

      if (tool.getBoundingBox().getCenter().distanceToSqr(site) <= this.reachSqr()) {
        this.work(level);
        tool.swing(InteractionHand.MAIN_HAND);
        this.working = false;
        return;
      }

      if (tool.getNavigation().isDone()) {
        tool.getMoveControl().setWantedPosition(site.x, site.y, site.z, 1.0);
      }
    }
  }
}
