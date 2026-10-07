package io.github.jason13official.spookiness.entity.book;

import java.util.EnumSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

final class BookStepGoal extends Goal {

  private static final double STEP_DISTANCE = 1.6;
  private static final double STEP_RISE = 0.6;
  private static final double STEP_RETARGET_DISTANCE = 0.5;
  private static final double STEP_SETTLE_DISTANCE = 0.3;
  private static final double PASS_DISTANCE = 1.5;
  private static final int STEP_HOLD_TICKS = 40;
  private static final double PHASE_MARGIN = 0.2;
  private static final double DETOUR_CLEARANCE = 0.4;
  private static final double DETOUR_ARC = 2.5;
  private static final double DETOUR_LEAD = 0.6;
  private static final double PATHFIND_DISTANCE = 4.0;

  private final FloatingBook book;
  private @Nullable Vec3 stepTarget;
  private boolean stepSettled;
  private int stepHoldTicks;

  BookStepGoal(FloatingBook book) {
    this.book = book;
    this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
  }

  @Override
  public boolean canUse() {
    return this.book.guidedPlayer() != null;
  }

  @Override
  public boolean canContinueToUse() {
    return this.book.guidedPlayer() != null;
  }

  @Override
  public void start() {
    this.resetStep();
  }

  @Override
  public void stop() {
    this.book.stopGuiding();
    this.resetStep();
    if (!this.book.isSupportingPlayer()) {
      this.book.setServerStillTimeout(0);
    }
  }

  @Override
  public boolean requiresUpdateEveryTick() {
    return true;
  }

  private void resetStep() {
    this.stepTarget = null;
    this.stepSettled = false;
    this.stepHoldTicks = 0;
  }

  @Override
  public void tick() {
    FloatingBook book = this.book;
    Player player = book.guidedPlayer();
    FloatingBook platform = book.guidePlatform();
    if (player == null || platform == null || platform.isRemoved() || !book.isCalmStep() || book.getTarget() != null || !BookStairs.isGuide(player, book)) {
      book.stopGuiding();
      return;
    }

    float yaw = player.getYRot();
    if (this.stepHoldTicks > 0) {
      this.stepHoldTicks--;
    } else {
      Vec3 candidate = this.findStep(platform.position(), yaw);
      if (candidate != null && (this.stepTarget == null || candidate.distanceTo(this.stepTarget) > STEP_RETARGET_DISTANCE)) {
        this.stepTarget = candidate;
        this.stepSettled = false;
      }
    }
    if (this.stepTarget == null) {
      return;
    }

    Vec3 target = this.stepTarget;
    boolean overlapping = book.getBoundingBox().inflate(PHASE_MARGIN).intersects(player.getBoundingBox());

    if (this.stepSettled || book.position().distanceTo(target) < STEP_SETTLE_DISTANCE) {
      book.setPhasing(overlapping && book.canPhase());
      if (!this.stepSettled) {
        this.stepSettled = true;
        this.stepHoldTicks = STEP_HOLD_TICKS;
        book.setPos(target.x, target.y, target.z);
        book.setYRot(yaw);
        book.yBodyRot = yaw;
        book.yHeadRot = yaw;
      }
      book.setDeltaMovement(Vec3.ZERO);
      book.setServerStillTimeout(FloatingBook.MAX_STILL_TIMEOUT);
      return;
    }

    book.setServerStillTimeout(0);
    book.getLookControl().setLookAt(player);

    double horizontal = book.position().subtract(player.position()).horizontalDistance();
    boolean behindPlayer = book.position().subtract(target).horizontalDistance() > player.position().subtract(target).horizontalDistance();
    boolean mustPass = overlapping || (behindPlayer && horizontal < PASS_DISTANCE);

    Vec3 wanted = target;
    if (book.isPhasing() || (mustPass && book.canPhase())) {
      book.setPhasing(true);
    } else if (mustPass) {
      wanted = this.detour(player, target);
    }

    if (!book.isPhasing() && book.position().distanceTo(wanted) > PATHFIND_DISTANCE) {
      if (book.tickCount % 10 == 0 || book.getNavigation().isDone()) {
        book.getNavigation().moveTo(wanted.x, wanted.y, wanted.z, 1.0);
      }
    } else {
      book.getNavigation().stop();
      book.getMoveControl().setWantedPosition(wanted.x, wanted.y, wanted.z, 1.0);
    }
  }

  private Vec3 detour(Player player, Vec3 target) {
    Vec3 toTarget = target.subtract(player.position()).multiply(1.0, 0.0, 1.0);
    Vec3 forward = toTarget.lengthSqr() > 1.0E-4 ? toTarget.normalize() : Vec3.ZERO;
    Vec3 lead = player.position().add(forward.scale(DETOUR_LEAD));

    Vec3 over = new Vec3(lead.x, player.getY() + player.getBbHeight() + DETOUR_CLEARANCE, lead.z);
    Vec3 under = new Vec3(lead.x, player.getY() - this.book.getBbHeight() - DETOUR_CLEARANCE, lead.z);
    Vec3 side = new Vec3(-forward.z, 0.0, forward.x).scale(DETOUR_ARC);
    Vec3 left = player.position().add(side).add(0.0, target.y - player.getY(), 0.0);
    Vec3 right = player.position().subtract(side).add(0.0, target.y - player.getY(), 0.0);
    Vec3 nearSide = left.distanceToSqr(this.book.position()) <= right.distanceToSqr(this.book.position()) ? left : right;
    Vec3 farSide = nearSide == left ? right : left;

    for (Vec3 waypoint : new Vec3[] {over, under, nearSide, farSide}) {
      if (this.book.level().noCollision(this.book, this.book.getDimensions(this.book.getPose()).makeBoundingBox(waypoint))) {
        return waypoint;
      }
    }
    return target;
  }

  private @Nullable Vec3 findStep(Vec3 from, float yaw) {
    double radians = yaw * Mth.DEG_TO_RAD;
    Vec3 forward = new Vec3(-Mth.sin((float) radians), 0.0, Mth.cos((float) radians)).scale(STEP_DISTANCE);
    for (double rise : new double[] {STEP_RISE, 0.0}) {
      Vec3 candidate = from.add(forward).add(0.0, rise, 0.0);
      AABB space = this.book.getDimensions(this.book.getPose()).makeBoundingBox(candidate);
      if (this.book.level().noCollision(this.book, space)) {
        return candidate;
      }
    }
    return null;
  }
}
