package io.github.jason13official.spookiness.entity.boss;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.entity.PartEntity;
import org.jspecify.annotations.Nullable;

public class GourdwyrmPart extends PartEntity<Gourdwyrm> {

  public final int index;
  private final EntityDimensions size;

  public GourdwyrmPart(Gourdwyrm parent, int index, float size) {
    super(parent);
    this.index = index;
    this.size = EntityDimensions.scalable(size, size);
    this.refreshDimensions();
  }

  @Override
  protected void defineSynchedData(SynchedEntityData.Builder entityData) {
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
  }

  @Override
  protected void addAdditionalSaveData(ValueOutput output) {
  }

  @Override
  public boolean isPickable() {
    return this.getParent().isSegmentAlive(this.index);
  }

  @Override
  public @Nullable ItemStack getPickResult() {
    return this.getParent().getPickResult();
  }

  @Override
  public final boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
    return !this.isInvulnerableToBase(source) && this.getParent().hurtSegment(level, this, source, damage);
  }

  @Override
  public boolean is(Entity other) {
    return this == other || this.getParent() == other;
  }

  @Override
  public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
    throw new UnsupportedOperationException();
  }

  @Override
  public EntityDimensions getDimensions(Pose pose) {
    return this.size;
  }

  @Override
  public boolean shouldBeSaved() {
    return false;
  }
}
