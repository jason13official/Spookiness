package io.github.jason13official.spookiness.entity;

import io.github.jason13official.spookiness.companion.PlayerFollower;
import java.util.Optional;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public abstract class FloatingCompanion extends FloatingPathfinderMob implements PlayerFollower {

  private static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> DATA_OWNER = SynchedEntityData.defineId(FloatingCompanion.class,
      EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);

  protected FloatingCompanion(EntityType<? extends FloatingCompanion> type, Level level) {
    super(type, level);
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder entityData) {
    super.defineSynchedData(entityData);
    entityData.define(DATA_OWNER, Optional.empty());
  }

  @Override
  public @Nullable EntityReference<LivingEntity> getOwnerReference() {
    return this.entityData.get(DATA_OWNER).orElse(null);
  }

  public boolean hasOwner() {
    return this.getOwnerReference() != null;
  }

  public boolean isOwnedBy(Entity entity) {
    EntityReference<LivingEntity> owner = this.getOwnerReference();
    return owner != null && owner.getUUID().equals(entity.getUUID());
  }

  protected void setOwner(LivingEntity owner) {
    this.entityData.set(DATA_OWNER, Optional.of(EntityReference.of(owner)));
    this.setPersistenceRequired();
    this.getNavigation().stop();
  }

  @Override
  protected void addAdditionalSaveData(ValueOutput output) {
    super.addAdditionalSaveData(output);
    EntityReference.store(this.getOwnerReference(), output, "owner");
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    this.entityData.set(DATA_OWNER, Optional.ofNullable(EntityReference.read(input, "owner")));
  }

  @Override
  public boolean canBeLeashed() {
    return false;
  }
}
