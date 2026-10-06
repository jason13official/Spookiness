package io.github.jason13official.spookiness.entity;

import io.github.jason13official.spookiness.registry.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class HauntedArmorStand extends ArmorStand {

  private static final double WATCH_RANGE = 64.0;
  private static final double FACE_RANGE = 16.0;
  private static final float TURN_SPEED = 12.0F;
  private static final int HOP_CHANCE = 1200;
  private static final double HOP_SPEED = 0.3;
  private static final double HOP_HEIGHT = 0.42;
  private static final int REVERT_CHANCE = 200;
  private static final int WATCHED_TURN_CHANCE = 600;
  private static final int WATCHED_HOP_CHANCE = 4800;

  private boolean revertsAtDawn;
  private boolean facedOnArrival;

  public HauntedArmorStand(EntityType<? extends HauntedArmorStand> type, Level level) {
    super(type, level);
  }

  public static boolean isWatched(ServerLevel level, ArmorStand stand) {
    for (Player player : level.players()) {
      if (!player.isSpectator() && player.distanceToSqr(stand) < WATCH_RANGE * WATCH_RANGE
          && stand.isLookingAtMe(player, 0.5, false, true, stand.getEyeY(), stand.getY() + 0.5, (stand.getEyeY() + stand.getY()) / 2.0)) {
        return true;
      }
    }
    return false;
  }

  public static @Nullable ArmorStand convert(ServerLevel level, ArmorStand from, EntityType<? extends ArmorStand> type) {
    ArmorStand to = type.create(level, EntitySpawnReason.CONVERSION);
    if (to == null) {
      return null;
    }

    to.snapTo(from.getX(), from.getY(), from.getZ(), from.getYRot(), from.getXRot());
    to.getEntityData().set(DATA_CLIENT_FLAGS, from.getEntityData().get(DATA_CLIENT_FLAGS));
    to.setHeadPose(from.getHeadPose());
    to.setBodyPose(from.getBodyPose());
    to.setLeftArmPose(from.getLeftArmPose());
    to.setRightArmPose(from.getRightArmPose());
    to.setLeftLegPose(from.getLeftLegPose());
    to.setRightLegPose(from.getRightLegPose());
    to.setCustomName(from.getCustomName());
    to.setNoGravity(from.isNoGravity());
    to.setInvulnerable(from.isInvulnerable());
    to.setSilent(true);
    for (EquipmentSlot slot : EquipmentSlot.VALUES) {
      to.setItemSlot(slot, from.getItemBySlot(slot).copy());
    }
    to.setSilent(from.isSilent());

    from.discard();
    level.addFreshEntity(to);
    return to;
  }

  public static void haunt(ServerLevel level, ArmorStand stand) {
    if (convert(level, stand, ModEntities.HAUNTED_ARMOR_STAND) instanceof HauntedArmorStand haunted) {
      haunted.revertsAtDawn = true;
    }
  }

  @Override
  protected void addAdditionalSaveData(ValueOutput output) {
    super.addAdditionalSaveData(output);
    output.putBoolean("reverts_at_dawn", this.revertsAtDawn);
  }

  @Override
  protected void readAdditionalSaveData(ValueInput input) {
    super.readAdditionalSaveData(input);
    this.revertsAtDawn = input.getBooleanOr("reverts_at_dawn", false);
  }

  private void face(Player player, float maxTurn) {
    float yaw = (float) (Mth.atan2(player.getZ() - this.getZ(), player.getX() - this.getX()) * Mth.RAD_TO_DEG) - 90.0F;
    this.setYRot(Mth.approachDegrees(this.getYRot(), yaw, maxTurn));
  }

  private void hop(Player player) {
    Vec3 toward = new Vec3(player.getX() - this.getX(), 0.0, player.getZ() - this.getZ()).normalize().scale(HOP_SPEED);
    this.setDeltaMovement(toward.x, HOP_HEIGHT, toward.z);
    this.needsSync = true;
    this.playSound(SoundEvents.ARMOR_STAND_FALL, 1.0F, 0.6F + this.random.nextFloat() * 0.2F);
  }

  @Override
  public void tick() {
    super.tick();
    if (!(this.level() instanceof ServerLevel level) || this.isMarker()) {
      return;
    }

    Player player = level.getNearestPlayer(this.getX(), this.getY(), this.getZ(), FACE_RANGE, EntitySelector.NO_SPECTATORS);
    if (!this.facedOnArrival && player != null) {
      this.face(player, 360.0F);
      this.facedOnArrival = true;
    }

    boolean watched = isWatched(level, this);
    if (!watched && this.revertsAtDawn && level.isBrightOutside() && this.random.nextInt(REVERT_CHANCE) == 0) {
      convert(level, this, EntityType.ARMOR_STAND);
      return;
    }
    if (player == null) {
      return;
    }

    if (!watched) {
      this.face(player, TURN_SPEED);
    } else if (this.random.nextInt(WATCHED_TURN_CHANCE) == 0) {
      this.face(player, 360.0F);
    }

    boolean canHop = this.onGround() && !this.isNoGravity() && this.distanceToSqr(player) > 4.0;
    if (canHop && this.random.nextInt(watched ? WATCHED_HOP_CHANCE : HOP_CHANCE) == 0) {
      this.face(player, 360.0F);
      this.hop(player);
    }
  }
}
