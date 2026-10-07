package io.github.jason13official.spookiness.boss;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jason13official.spookiness.advancement.SpookyTrigger;
import io.github.jason13official.spookiness.entity.boss.Wickman;
import io.github.jason13official.spookiness.registry.ModAttachments;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class Kindling {

  public static final int DURATION_TICKS = 40;

  private static final double LIFT_SPEED = 0.04;
  private static final double RING_RADIUS = 1.0;
  private static final int RING_ARMS = 3;

  public static boolean isKindling(Mob mob) {

    return mob.hasData(ModAttachments.KINDLE);
  }

  public static boolean start(ServerLevel level, ServerPlayer player, Mob vessel, Wickman.Variant variant) {

    if (isKindling(vessel)) {
      return false;
    }

    vessel.setNoAi(true);
    vessel.setInvulnerable(true);
    vessel.setNoGravity(true);
    vessel.setTarget(null);
    if (vessel instanceof SnowGolem golem) {
      golem.setPumpkin(false);
    } else {
      vessel.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
    }

    vessel.setData(ModAttachments.KINDLE, new Kindle(player.getUUID(), variant, 0));
    level.playSound(null, vessel.getX(), vessel.getY(), vessel.getZ(), variant == Wickman.Variant.FROST ? SoundEvents.POWDER_SNOW_BREAK : SoundEvents.FIRECHARGE_USE,
        SoundSource.HOSTILE, 1.5F, 0.5F);
    level.playSound(null, vessel.getX(), vessel.getY(), vessel.getZ(), SoundEvents.PORTAL_TRIGGER, SoundSource.HOSTILE, 0.6F, 1.6F);
    return true;
  }

  public static void tick(Entity entity) {

    if (!(entity instanceof Mob vessel) || !(vessel.level() instanceof ServerLevel level)) {
      return;
    }
    Kindle kindle = vessel.getExistingDataOrNull(ModAttachments.KINDLE);
    if (kindle == null) {
      return;
    }

    int ticks = kindle.ticks() + 1;
    vessel.setDeltaMovement(0.0, LIFT_SPEED, 0.0);
    vessel.hurtMarked = true;
    emit(level, vessel, kindle.variant(), ticks);

    if (ticks < DURATION_TICKS) {
      vessel.setData(ModAttachments.KINDLE, new Kindle(kindle.kindler(), kindle.variant(), ticks));
      return;
    }

    vessel.removeData(ModAttachments.KINDLE);
    Player kindler = level.getPlayerByUUID(kindle.kindler());
    Wickman.kindle(level, vessel, kindle.variant(), kindler);
    SpookyTrigger.award(kindler, SpookyTrigger.KINDLE);
    burst(level, vessel.position(), kindle.variant());
    vessel.discard();
  }

  private static void emit(ServerLevel level, Mob vessel, Wickman.Variant variant, int ticks) {

    boolean frost = variant == Wickman.Variant.FROST;
    double rise = (ticks % 20) / 20.0 * (vessel.getBbHeight() + 1.0);
    for (int arm = 0; arm < RING_ARMS; arm++) {
      double angle = ticks * 0.5 + arm * Mth.TWO_PI / RING_ARMS;
      level.sendParticles(frost ? ParticleTypes.SNOWFLAKE : ParticleTypes.FLAME, vessel.getX() + Math.cos(angle) * RING_RADIUS, vessel.getY() + rise,
          vessel.getZ() + Math.sin(angle) * RING_RADIUS, 1, 0.0, 0.0, 0.0, 0.0);
    }
    Vec3 head = vessel.getEyePosition();
    level.sendParticles(frost ? ParticleTypes.ITEM_SNOWBALL : ParticleTypes.LAVA, head.x, head.y, head.z, 1, 0.1, 0.1, 0.1, 0.0);
    if (ticks % 5 == 0) {
      level.sendParticles(ParticleTypes.LARGE_SMOKE, vessel.getX(), vessel.getY(), vessel.getZ(), 4, 0.5, 0.05, 0.5, 0.02);
    }
  }

  private static void burst(ServerLevel level, Vec3 pos, Wickman.Variant variant) {

    boolean frost = variant == Wickman.Variant.FROST;
    level.sendParticles(frost ? ParticleTypes.SNOWFLAKE : ParticleTypes.FLAME, pos.x, pos.y + 1.5, pos.z, 80, 0.6, 1.2, 0.6, 0.15);
    level.playSound(null, pos.x, pos.y, pos.z, frost ? SoundEvents.GLASS_BREAK : SoundEvents.BLAZE_SHOOT, SoundSource.HOSTILE, 2.0F, 0.5F);
  }

  public record Kindle(UUID kindler, Wickman.Variant variant, int ticks) {

    public static final Codec<Kindle> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        UUIDUtil.CODEC.fieldOf("kindler").forGetter(Kindle::kindler),
        Wickman.Variant.CODEC.fieldOf("variant").forGetter(Kindle::variant),
        Codec.INT.fieldOf("ticks").forGetter(Kindle::ticks)
    ).apply(instance, Kindle::new));
  }
}
