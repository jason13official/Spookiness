package io.github.jason13official.spookiness.entity.boss;

import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import org.jspecify.annotations.Nullable;

public abstract class SpookyBoss extends Monster {

  protected final ServerBossEvent bossEvent;

  protected SpookyBoss(EntityType<? extends SpookyBoss> type, Level level, BossEvent.BossBarColor color, BossEvent.BossBarOverlay overlay) {
    super(type, level);
    this.bossEvent = new ServerBossEvent(UUID.randomUUID(), this.getDisplayName(), color, overlay);
    this.bossEvent.setCreateWorldFog(true);
    this.setPersistenceRequired();
  }

  protected boolean isAnchored() {
    return false;
  }

  @Override
  protected void customServerAiStep(ServerLevel level) {
    super.customServerAiStep(level);
    this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
  }

  @Override
  public void startSeenByPlayer(ServerPlayer player) {
    super.startSeenByPlayer(player);
    this.bossEvent.addPlayer(player);
  }

  @Override
  public void stopSeenByPlayer(ServerPlayer player) {
    super.stopSeenByPlayer(player);
    this.bossEvent.removePlayer(player);
  }

  @Override
  public void setCustomName(@Nullable Component name) {
    super.setCustomName(name);
    this.bossEvent.setName(this.getDisplayName());
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    this.bossEvent.setName(this.getDisplayName());
  }

  @Override
  public boolean removeWhenFarAway(double distSqr) {
    return false;
  }

  @Override
  public boolean isPushable() {
    return !this.isAnchored() && super.isPushable();
  }

  @Override
  public void push(Entity entity) {
    if (!this.isAnchored()) {
      super.push(entity);
    }
  }

  @Override
  public void knockback(double power, double xd, double zd) {
    if (!this.isAnchored()) {
      super.knockback(power, xd, zd);
    }
  }
}
