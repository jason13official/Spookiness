package io.github.jason13official.spookiness.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class FloatingBook extends FloatingPathfinderMob {

  private static final double READING_DISTANCE = 4.0;

  public float open;
  public float oOpen;
  public float flip;
  public float oFlip;
  private float flipT;
  private float flipA;

  public FloatingBook(EntityType<? extends FloatingBook> type, Level level) {
    super(type, level);
  }

  public static AttributeSupplier.Builder createAttributes() {
    return createFloatingAttributes().add(Attributes.MAX_HEALTH, 10.0F).add(Attributes.ATTACK_DAMAGE, 2.0F).add(Attributes.ATTACK_KNOCKBACK, 1.0F);
  }

  @Override
  protected void registerGoals() {

    int goalPriority = 1;

    this.goalSelector.addGoal(goalPriority++, new MeleeAttackGoal(this, 1.4, true));
    this.goalSelector.addGoal(goalPriority++, new WaterAvoidingRandomFlyingGoal(this, 1.0));
    this.goalSelector.addGoal(goalPriority++, new LookAtPlayerGoal(this, Player.class, 8.0F));
    this.goalSelector.addGoal(goalPriority++, new RandomLookAroundGoal(this));

    int targetPriority = 1;

    this.targetSelector.addGoal(targetPriority++, new HurtByTargetGoal(this).setAlertOthers());
  }

  @Override
  public void tick() {
    super.tick();
    if (this.level().isClientSide()) {
      this.bookAnimationTick();
    }
  }

  private void bookAnimationTick() {
    this.oOpen = this.open;
    this.oFlip = this.flip;

    boolean reading = !this.isAggressive() && this.level().getNearestPlayer(this, READING_DISTANCE) != null;
    if (reading) {
      this.open += 0.1F;
      if (this.open < 0.5F || this.random.nextInt(40) == 0) {
        float old = this.flipT;
        do {
          this.flipT += this.random.nextInt(4) - this.random.nextInt(4);
        } while (old == this.flipT);
      }
    } else {
      this.open -= 0.1F;
    }

    this.open = Mth.clamp(this.open, 0.0F, 1.0F);
    float diff = Mth.clamp((this.flipT - this.flip) * 0.4F, -0.2F, 0.2F);
    this.flipA += (diff - this.flipA) * 0.9F;
    this.flip += this.flipA;
  }

  @Override
  protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
    super.dropCustomDeathLoot(level, source, killedByPlayer);
    this.spawnAtLocation(level, Items.BOOK);
  }

  @Override
  public boolean canBeLeashed() {
    return false;
  }

  @Override
  protected SoundEvent getAmbientSound() {
    return SoundEvents.BOOK_PAGE_TURN;
  }

  @Override
  protected SoundEvent getHurtSound(DamageSource source) {
    return SoundEvents.BOOK_PAGE_TURN;
  }

  @Override
  protected SoundEvent getDeathSound() {
    return SoundEvents.BOOK_PUT;
  }
}
