package io.github.jason13official.spookiness.entity.projectile;

import io.github.jason13official.spookiness.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

public class PumpkinBomb extends ThrowableItemProjectile {

  private static final double BLAST_RADIUS = 2.5;
  private static final float BLAST_DAMAGE = 6.0F;
  private static final int FIRE_RADIUS = 1;

  public PumpkinBomb(EntityType<? extends PumpkinBomb> type, Level level) {
    super(type, level);
  }

  public PumpkinBomb(Level level, LivingEntity owner) {
    super(ModEntities.PUMPKIN_BOMB, owner, level, new ItemStack(Items.JACK_O_LANTERN));
  }

  @Override
  protected Item getDefaultItem() {
    return Items.JACK_O_LANTERN;
  }

  @Override
  public void tick() {
    super.tick();
    if (this.level().isClientSide()) {
      this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY() + 0.2, this.getZ(), 0.0, 0.02, 0.0);
      this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.2, this.getZ(), 0.0, 0.02, 0.0);
    }
  }

  @Override
  protected void onHit(HitResult hitResult) {
    super.onHit(hitResult);
    if (!(this.level() instanceof ServerLevel level)) {
      return;
    }

    Vec3 center = this.position();
    Entity owner = this.getOwner();
    level.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z, 1, 0.0, 0.0, 0.0, 0.0);
    level.sendParticles(ParticleTypes.FLAME, center.x, center.y + 0.3, center.z, 30, 0.8, 0.3, 0.8, 0.05);
    level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 1.0F, 1.4F);
    level.playSound(null, center.x, center.y, center.z, SoundEvents.WOOD_BREAK, SoundSource.HOSTILE, 1.0F, 0.6F);

    for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(BLAST_RADIUS),
        entity -> entity != owner && entity.isAlive() && entity.distanceToSqr(center) <= BLAST_RADIUS * BLAST_RADIUS)) {
      if (victim.hurtServer(level, this.damageSources().thrown(this, owner), BLAST_DAMAGE)) {
        Vec3 push = victim.position().subtract(center).normalize().scale(0.6);
        victim.push(push.x, 0.3, push.z);
      }
    }

    if (EventHooks.canEntityGrief(level, owner)) {
      BlockPos origin = BlockPos.containing(center);
      for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-FIRE_RADIUS, -1, -FIRE_RADIUS), origin.offset(FIRE_RADIUS, 1, FIRE_RADIUS))) {
        if (level.isEmptyBlock(pos) && BaseFireBlock.canBePlacedAt(level, pos, Direction.UP)) {
          level.setBlockAndUpdate(pos, BaseFireBlock.getState(level, pos));
        }
      }
    }

    this.discard();
  }
}
