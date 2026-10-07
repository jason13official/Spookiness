package io.github.jason13official.spookiness.companion;

import io.github.jason13official.spookiness.advancement.SpookyTrigger;
import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.effect.SoulBurst;
import io.github.jason13official.spookiness.registry.ModAttachments;
import io.github.jason13official.spookiness.registry.ModDamageTypes;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.jspecify.annotations.Nullable;

public final class Hallowing {

  public static final int MAX_ALLIES = 5;
  public static final int DURABILITY_COST = 15;
  public static final float HEALTH_COST = 2.0F;

  private static final int FOLLOW_PRIORITY = 3;
  private static final double FOLLOW_START_DISTANCE = 6.0;
  private static final double FOLLOW_STOP_DISTANCE = 3.0;
  private static final double TELEPORT_DISTANCE = 24.0;
  private static final float NIGHT_DAMAGE_MULTIPLIER = 1.25F;
  private static final double BLESSING_BONUS = 0.2;
  private static final Identifier BLESSING_ID = Spookiness.id("harvest_blessed");

  private static final Set<Mob> GOALS_APPLIED = Collections.newSetFromMap(new WeakHashMap<>());

  public static @Nullable UUID ownerOf(Entity entity) {

    Optional<UUID> owner = entity.getExistingDataOrNull(ModAttachments.HALLOWED_OWNER);
    return owner == null ? null : owner.orElse(null);
  }

  public static boolean isHallowed(Entity entity) {

    return ownerOf(entity) != null;
  }

  public static int count(ServerPlayer owner) {

    return PlayerFollowers.count(owner, Hallowing::isHallowed);
  }

  public static boolean hallow(ServerLevel level, ServerPlayer player, Mob mob, ItemStack mace, InteractionHand hand) {

    if (player.getHealth() <= HEALTH_COST) {
      refuse(level, player, "message.spookiness.hallow_too_weak");
      return false;
    }
    if (count(player) >= MAX_ALLIES) {
      refuse(level, player, "message.spookiness.hallow_too_many");
      return false;
    }

    mace.hurtAndBreak(DURABILITY_COST, player, hand);
    player.hurtServer(level, level.damageSources().source(ModDamageTypes.HALLOWING), HEALTH_COST);
    SoulBurst.spawn(level, player.getBoundingBox().getCenter(), 16, 0.3, 0.05);

    mob.setData(ModAttachments.HALLOWED_OWNER, Optional.of(player.getUUID()));
    mob.setTarget(null);
    mob.setPersistenceRequired();
    applyGoals(mob);

    SoulBurst.spawn(level, mob.getBoundingBox().getCenter(), 32, 0.4, 0.08);
    level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.5F, 1.2F);
    player.sendOverlayMessage(Component.translatable("message.spookiness.hallowed", mob.getDisplayName()));
    SpookyTrigger.award(player, SpookyTrigger.HALLOW);
    return true;
  }

  public static void unhallow(Mob mob) {

    mob.removeData(ModAttachments.HALLOWED_OWNER);
    GOALS_APPLIED.remove(mob);
    PlayerFollowers.untrack(mob);
    mob.setTarget(null);
    mob.goalSelector.removeAllGoals(goal -> goal instanceof FollowGoal);
    mob.targetSelector.removeAllGoals(goal -> true);
    if (mob instanceof PathfinderMob pathfinder) {
      mob.targetSelector.addGoal(1, new HurtByTargetGoal(pathfinder));
      mob.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, Player.class, true));
    }
  }

  public static void bless(Mob mob) {

    AttributeInstance maxHealth = mob.getAttribute(Attributes.MAX_HEALTH);
    if (maxHealth == null || maxHealth.hasModifier(BLESSING_ID)) {
      return;
    }
    maxHealth.addPermanentModifier(new AttributeModifier(BLESSING_ID, BLESSING_BONUS, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    mob.setHealth(mob.getMaxHealth());
  }

  public static void applyGoals(Mob mob) {

    if (!isHallowed(mob) || !(mob instanceof PathfinderMob pathfinder) || !GOALS_APPLIED.add(mob)) {
      return;
    }

    mob.targetSelector.removeAllGoals(goal -> true);
    mob.goalSelector.addGoal(FOLLOW_PRIORITY, new FollowGoal(pathfinder));

    int targetPriority = 1;
    mob.targetSelector.addGoal(targetPriority++, new DefendOwnerGoal(mob, true, target -> true));
    mob.targetSelector.addGoal(targetPriority++, new DefendOwnerGoal(mob, false, target -> true));
    mob.targetSelector.addGoal(targetPriority++, new HurtByTargetGoal(pathfinder));
    mob.targetSelector.addGoal(targetPriority++, new NearestAttackableTargetGoal<>(mob, Mob.class, true,
        (target, level) -> target instanceof Enemy && !(target instanceof Creeper) && !Allies.isAlly(mob, target)));
  }

  public static void onIncomingDamage(LivingIncomingDamageEvent event) {

    Entity attacker = event.getSource().getEntity();
    if (attacker != null && isHallowed(attacker) && attacker.level().isDarkOutside()) {
      event.setAmount(event.getAmount() * NIGHT_DAMAGE_MULTIPLIER);
    }
  }

  public static void clientTick(Entity entity) {

    if (entity.tickCount % 8 != 0 || !isHallowed(entity)) {
      return;
    }
    Vec3 center = entity.getBoundingBox().getCenter();
    double spread = entity.getBbWidth() * 0.5;
    entity.level().addParticle(ParticleTypes.SOUL, center.x + (entity.getRandom().nextDouble() - 0.5) * spread * 2.0,
        center.y + (entity.getRandom().nextDouble() - 0.5) * entity.getBbHeight(), center.z + (entity.getRandom().nextDouble() - 0.5) * spread * 2.0,
        0.0, 0.02, 0.0);
  }

  private static void refuse(ServerLevel level, ServerPlayer player, String message) {

    player.sendOverlayMessage(Component.translatable(message));
    level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 0.6F, 0.5F);
  }

  private static final class FollowGoal extends FollowOwnerGoal {

    private final PathfinderMob pathfinder;

    FollowGoal(PathfinderMob pathfinder) {
      super(pathfinder, TELEPORT_DISTANCE);
      this.pathfinder = pathfinder;
    }

    @Override
    protected boolean canStart(Player owner) {
      return this.mob.getTarget() == null && this.mob.distanceTo(owner) >= FOLLOW_START_DISTANCE;
    }

    @Override
    protected boolean canKeepFollowing(Player owner) {
      return this.mob.getTarget() == null && this.mob.distanceTo(owner) > FOLLOW_STOP_DISTANCE;
    }

    @Override
    protected Vec3 anchor(Entity leader) {
      return leader.position();
    }

    @Override
    protected void approach(Entity leader, Vec3 anchor, double distance) {
      if (this.mob.tickCount % 10 == 0) {
        this.pathfinder.getNavigation().moveTo(leader, 1.2);
      }
    }
  }
}
