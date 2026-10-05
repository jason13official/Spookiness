package io.github.jason13official.spookiness.entity;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jspecify.annotations.Nullable;

public class FloatingSword extends FloatingPathfinderMob implements Enemy {

  private static final float FADE_PER_TICK = 0.08F;

  private float visibility;
  private float oVisibility;

  public FloatingSword(EntityType<? extends FloatingSword> type, Level level) {
    super(type, level);
    this.xpReward = 5;
  }

  public static AttributeSupplier.Builder createAttributes() {
    return createFloatingAttributes().add(Attributes.MAX_HEALTH, 14.0F).add(Attributes.ATTACK_DAMAGE, 3.0F).add(Attributes.FOLLOW_RANGE, 24.0F).add(Attributes.FLYING_SPEED, 0.3F);
  }

  @Override
  protected void registerGoals() {

    int goalPriority = 1;

    this.goalSelector.addGoal(goalPriority++, new MeleeAttackGoal(this, 1.2, false));
    this.goalSelector.addGoal(goalPriority++, new WaterAvoidingRandomFlyingGoal(this, 0.8));
    this.goalSelector.addGoal(goalPriority++, new LookAtPlayerGoal(this, Player.class, 8.0F));
    this.goalSelector.addGoal(goalPriority++, new RandomLookAroundGoal(this));

    int targetPriority = 1;

    this.targetSelector.addGoal(targetPriority++, new HurtByTargetGoal(this).setAlertOthers());
    this.targetSelector.addGoal(targetPriority++, new NearestAttackableTargetGoal<>(this, Player.class, true));
  }

  @Override
  public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData groupData) {
    RandomSource random = level.getRandom();
    this.populateDefaultEquipmentSlots(random, difficulty);
    this.populateDefaultEquipmentEnchantments(level, random, difficulty);
    return super.finalizeSpawn(level, difficulty, spawnReason, groupData);
  }

  @Override
  protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
    this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(random.nextInt(4) == 0 ? Items.GOLDEN_SWORD : Items.IRON_SWORD));
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
}
