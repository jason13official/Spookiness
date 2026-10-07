package io.github.jason13official.spookiness.companion;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public class DefendOwnerGoal extends TargetGoal {

  private final boolean retaliate;
  private final Predicate<LivingEntity> canDefendAgainst;
  private @Nullable LivingEntity candidate;
  private int timestamp;

  public DefendOwnerGoal(Mob mob, boolean retaliate, Predicate<LivingEntity> canDefendAgainst) {
    super(mob, false);
    this.retaliate = retaliate;
    this.canDefendAgainst = canDefendAgainst;
    this.setFlags(EnumSet.of(Goal.Flag.TARGET));
  }

  private int ownerTimestamp(Player owner) {
    return this.retaliate ? owner.getLastHurtByMobTimestamp() : owner.getLastHurtMobTimestamp();
  }

  @Override
  public boolean canUse() {
    Player owner = Allies.owner(this.mob);
    if (owner == null) {
      return false;
    }
    this.candidate = this.retaliate ? owner.getLastHurtByMob() : owner.getLastHurtMob();
    return this.ownerTimestamp(owner) != this.timestamp && this.candidate != null && !Allies.isAlly(this.mob, this.candidate)
        && this.canAttack(this.candidate, TargetingConditions.DEFAULT) && this.canDefendAgainst.test(this.candidate);
  }

  @Override
  public void start() {
    this.mob.setTarget(this.candidate);
    Player owner = Allies.owner(this.mob);
    if (owner != null) {
      this.timestamp = this.ownerTimestamp(owner);
    }
    super.start();
  }
}
