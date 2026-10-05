package io.github.jason13official.spookiness.entity;

import io.github.jason13official.spookiness.entity.control.FloatingMoveControl;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public abstract class FloatingPathfinderMob extends PathfinderMob {

  protected FloatingPathfinderMob(EntityType<? extends FloatingPathfinderMob> type, Level level) {
    super(type, level);
    this.moveControl = new FloatingMoveControl(this, 0.1, 10.0F);
    this.setNoGravity(true);
  }

  public static AttributeSupplier.Builder createFloatingAttributes() {
    return Mob.createMobAttributes().add(Attributes.FLYING_SPEED, 0.2F).add(Attributes.MOVEMENT_SPEED, 0.2F);
  }

  @Override
  protected PathNavigation createNavigation(Level level) {
    FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
    navigation.setCanOpenDoors(false);
    navigation.setCanFloat(true);
    return navigation;
  }

  @Override
  public void travel(Vec3 input) {
    this.travelFlying(input, this.getSpeed());
  }

  @Override
  protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {
  }

  @Override
  protected void playStepSound(BlockPos pos, BlockState blockState) {
  }
}
