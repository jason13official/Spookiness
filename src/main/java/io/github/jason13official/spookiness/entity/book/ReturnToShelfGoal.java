package io.github.jason13official.spookiness.entity.book;

import java.util.EnumSet;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.phys.Vec3;

final class ReturnToShelfGoal extends Goal {

  private static final int MAX_RETURN_TICKS = 600;
  private static final double ENTER_DISTANCE = 0.8;
  private static final double APPROACH_DISTANCE = 3.0;

  private final FloatingBook book;
  private int returnTicks;

  ReturnToShelfGoal(FloatingBook book) {
    this.book = book;
    this.setFlags(EnumSet.of(Goal.Flag.MOVE));
  }

  @Override
  public boolean canUse() {
    return this.book.home().isRestless() && this.book.getTarget() == null && this.book.guidedPlayer() == null && !this.book.isOnStillTimeout();
  }

  @Override
  public boolean canContinueToUse() {
    return this.book.home().isShelf() && this.book.getTarget() == null && this.book.guidedPlayer() == null && !this.book.isSupportingPlayer();
  }

  @Override
  public void start() {
    this.returnTicks = 0;
  }

  @Override
  public void stop() {
    this.book.getNavigation().stop();
  }

  @Override
  public boolean requiresUpdateEveryTick() {
    return true;
  }

  @Override
  public void tick() {
    ChiseledBookShelfBlockEntity shelf = this.book.home().findShelf();
    if (shelf == null) {
      this.book.home().lose();
      return;
    }

    Vec3 front = FloatingBook.shelfFront(shelf);
    if (this.book.position().distanceTo(front) < ENTER_DISTANCE || ++this.returnTicks > MAX_RETURN_TICKS) {
      this.book.home().enter(shelf);
      return;
    }

    if (this.book.position().distanceTo(front) < APPROACH_DISTANCE) {
      this.book.getNavigation().stop();
      this.book.getMoveControl().setWantedPosition(front.x, front.y, front.z, 0.8);
    } else if (this.returnTicks % 10 == 1) {
      this.book.getNavigation().moveTo(front.x, front.y, front.z, 1.0);
    }
  }
}
