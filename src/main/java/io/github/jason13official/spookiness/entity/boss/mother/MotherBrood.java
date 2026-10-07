package io.github.jason13official.spookiness.entity.boss.mother;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.entity.JackOMimic;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import org.jspecify.annotations.Nullable;

public final class MotherBrood {

  public static final String TAG = "spookiness.brood";
  public static final double FALL_IMMUNITY = 15.0;

  private static final Identifier HEALTH_ID = Spookiness.id("brood_health");
  private static final double HEALTH_BONUS = 5.0;
  private static final float KNOCKBACK_BONUS = 0.8F;
  private static final double MOTHER_SEARCH = 48.0;
  private static final double HERD_START = 5.0;
  private static final double FLANK_DISTANCE = 1.6;
  private static final double FLANK_REACHED = 1.2;
  private static final double HERD_RANGE = 12.0;
  private static final int HERD_PRIORITY = 1;

  private static final Set<Mob> HERDING = Collections.newSetFromMap(new WeakHashMap<>());

  public static boolean isBrood(Entity entity) {
    return entity instanceof Mob && entity.entityTags().contains(TAG);
  }

  public static void adopt(Mob mob) {

    mob.addTag(TAG);
    mob.setPersistenceRequired();
    if (mob instanceof JackOMimic) {
      AttributeInstance health = mob.getAttribute(Attributes.MAX_HEALTH);
      if (health != null && !health.hasModifier(HEALTH_ID)) {
        health.addPermanentModifier(new AttributeModifier(HEALTH_ID, HEALTH_BONUS, AttributeModifier.Operation.ADD_VALUE));
        mob.setHealth(mob.getMaxHealth());
      }
    }
    applyGoals(mob);
  }

  public static void applyGoals(Mob mob) {

    if (mob instanceof PathfinderMob pathfinder && isBrood(mob) && HERDING.add(mob)) {
      mob.goalSelector.addGoal(HERD_PRIORITY, new HerdGoal(pathfinder));
    }
  }

  public static void onChangeTarget(LivingChangeTargetEvent event) {

    LivingEntity target = event.getNewAboutToBeSetTarget();
    if (target != null && isBrood(event.getEntity()) && (target instanceof HallowedMother || isBrood(target))) {
      event.setCanceled(true);
    }
  }

  public static void onEffectApplicable(MobEffectEvent.Applicable event) {

    LivingEntity entity = event.getEntity();
    if (event.getEffectInstance().is(MobEffects.POISON) && (entity instanceof HallowedMother || isBrood(entity))) {
      event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
    }
  }

  public static @Nullable HallowedMother findMother(Entity entity) {

    return entity.level().getEntitiesOfClass(HallowedMother.class, new AABB(entity.blockPosition()).inflate(MOTHER_SEARCH), Entity::isAlive).stream()
        .min(Comparator.comparingDouble(entity::distanceToSqr)).orElse(null);
  }

  public static void onKnockBack(LivingKnockBackEvent event) {

    LivingEntity victim = event.getEntity();
    LivingEntity attacker = victim.getLastHurtByMob();
    if (attacker == null || !isBrood(attacker) || victim.getLastHurtByMobTimestamp() != victim.tickCount) {
      return;
    }
    HallowedMother mother = findMother(attacker);
    event.setStrength(event.getStrength() + KNOCKBACK_BONUS);
    if (mother == null) {
      return;
    }
    Vec3 away = victim.position().subtract(mother.position());
    if (away.horizontalDistanceSqr() > 1.0E-4) {
      event.setRatioX(away.x);
      event.setRatioZ(away.z);
    }
  }

  public static void onFall(LivingFallEvent event) {

    LivingEntity entity = event.getEntity();
    if (entity instanceof HallowedMother || isBrood(entity)) {
      event.setDistance(Math.max(0.0, event.getDistance() - FALL_IMMUNITY));
    }
  }

  private static final class HerdGoal extends Goal {

    private final PathfinderMob mimic;
    private @Nullable HallowedMother mother;
    private @Nullable Vec3 flank;

    HerdGoal(PathfinderMob mimic) {
      this.mimic = mimic;
      this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {

      LivingEntity target = this.mimic.getTarget();
      if (target == null || this.mimic.tickCount % 10 != 0 || this.mimic.distanceTo(target) > HERD_RANGE) {
        return false;
      }
      this.mother = findMother(this.mimic);
      if (this.mother == null || target.distanceTo(this.mother) < HERD_START) {
        return false;
      }
      this.flank = this.flankPoint(target);
      return this.mimic.position().distanceTo(this.flank) > FLANK_REACHED;
    }

    @Override
    public boolean canContinueToUse() {

      LivingEntity target = this.mimic.getTarget();
      if (target == null || this.mother == null || !this.mother.isAlive() || target.distanceTo(this.mother) < HERD_START) {
        return false;
      }
      this.flank = this.flankPoint(target);
      return this.mimic.position().distanceTo(this.flank) > FLANK_REACHED && !this.mimic.getNavigation().isStuck();
    }

    @Override
    public void tick() {

      LivingEntity target = this.mimic.getTarget();
      if (this.flank != null && this.mimic.tickCount % 5 == 0) {
        this.mimic.getNavigation().moveTo(this.flank.x, this.flank.y, this.flank.z, 1.5);
      }
      if (target != null) {
        this.mimic.getLookControl().setLookAt(target);
      }
    }

    @Override
    public void stop() {
      this.mimic.getNavigation().stop();
      this.flank = null;
    }

    private Vec3 flankPoint(LivingEntity target) {

      Vec3 away = target.position().subtract(this.mother.position()).multiply(1.0, 0.0, 1.0);
      Vec3 direction = away.lengthSqr() > 1.0E-4 ? away.normalize() : Vec3.ZERO;
      return target.position().add(direction.scale(FLANK_DISTANCE));
    }
  }
}
