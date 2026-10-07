package io.github.jason13official.spookiness.companion;

import java.util.EnumSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public abstract class FollowOwnerGoal extends Goal {

  protected final Mob mob;
  private final double teleportDistance;
  private @Nullable Player owner;

  protected FollowOwnerGoal(Mob mob, double teleportDistance) {
    this.mob = mob;
    this.teleportDistance = teleportDistance;
    this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
  }

  protected abstract Vec3 anchor(Entity leader);

  protected abstract void approach(Entity leader, Vec3 anchor, double distance);

  protected Entity leader(Player owner) {
    return owner;
  }

  protected boolean canStart(Player owner) {
    return true;
  }

  protected boolean canKeepFollowing(Player owner) {
    return true;
  }

  @Override
  public boolean canUse() {
    Player owner = Allies.owner(this.mob);
    if (owner == null || owner.isSpectator() || !this.canStart(owner)) {
      return false;
    }
    this.owner = owner;
    return true;
  }

  @Override
  public boolean canContinueToUse() {
    return this.owner != null && this.owner.isAlive() && !this.owner.isRemoved() && !this.owner.isSpectator() && this.canKeepFollowing(this.owner);
  }

  @Override
  public void stop() {
    this.owner = null;
    this.mob.getNavigation().stop();
  }

  @Override
  public boolean requiresUpdateEveryTick() {
    return true;
  }

  @Override
  public void tick() {
    if (this.owner == null) {
      return;
    }

    Entity leader = this.leader(this.owner);
    Vec3 anchor = this.anchor(leader);
    double distance = this.mob.position().distanceTo(anchor);
    this.mob.getLookControl().setLookAt(leader, 10.0F, this.mob.getMaxHeadXRot());

    if (distance > this.teleportDistance) {
      this.mob.teleportTo(anchor.x, anchor.y, anchor.z);
      this.mob.setDeltaMovement(Vec3.ZERO);
      this.mob.getNavigation().stop();
      return;
    }
    this.approach(leader, anchor, distance);
  }
}
