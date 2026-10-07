package io.github.jason13official.spookiness.entity.projectile;

import io.github.jason13official.spookiness.effect.TemporaryBlocks;
import io.github.jason13official.spookiness.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.EventHooks;

public class FrostVolley extends ThrowableItemProjectile {

  private static final float HIT_DAMAGE = 2.0F;
  private static final int FREEZE_TICKS = 60;
  private static final int POWDER_SNOW_TICKS = 200;

  public FrostVolley(EntityType<? extends FrostVolley> type, Level level) {
    super(type, level);
  }

  public FrostVolley(Level level, LivingEntity owner) {
    super(ModEntities.FROST_VOLLEY, owner, level, new ItemStack(Items.SNOWBALL));
  }

  @Override
  protected Item getDefaultItem() {
    return Items.SNOWBALL;
  }

  @Override
  protected void onHitEntity(EntityHitResult hitResult) {
    super.onHitEntity(hitResult);
    if (this.level() instanceof ServerLevel level && hitResult.getEntity() != this.getOwner()) {
      Entity entity = hitResult.getEntity();
      entity.hurtServer(level, this.damageSources().thrown(this, this.getOwner()), HIT_DAMAGE);
      entity.setTicksFrozen(Math.max(entity.getTicksFrozen(), entity.getTicksRequiredToFreeze() + FREEZE_TICKS));
    }
  }

  @Override
  protected void onHitBlock(BlockHitResult hitResult) {
    super.onHitBlock(hitResult);
    if (this.level() instanceof ServerLevel level && hitResult.getDirection() == Direction.UP && EventHooks.canEntityGrief(level, this.getOwner())) {
      BlockPos pos = hitResult.getBlockPos().above();
      TemporaryBlocks.place(level, pos, Blocks.POWDER_SNOW.defaultBlockState(), POWDER_SNOW_TICKS);
    }
  }

  @Override
  protected void onHit(HitResult hitResult) {
    super.onHit(hitResult);
    if (this.level() instanceof ServerLevel level) {
      level.sendParticles(ParticleTypes.SNOWFLAKE, this.getX(), this.getY(), this.getZ(), 8, 0.2, 0.2, 0.2, 0.05);
      this.discard();
    }
  }
}
