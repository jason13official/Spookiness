package io.github.jason13official.spookiness.item;

import io.github.jason13official.spookiness.companion.SpectralCompanions;
import io.github.jason13official.spookiness.entity.JackOMimic;
import io.github.jason13official.spookiness.registry.ModDataComponents;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class PumpkinMaceItem extends Item {

  /// 1.5F originally
  public static final float SMASH_ATTACK_FALL_THRESHOLD = 0.1F;
  public static final float SMASH_ATTACK_KNOCKBACK_RADIUS = 3.5F;
  /// -3.4 originally
  private static final float DEFAULT_ATTACK_SPEED = -2.4F;
  private static final int DEFAULT_ATTACK_DAMAGE = 5;
  private static final float SMASH_ATTACK_HEAVY_THRESHOLD = 5.0F;
  private static final float SMASH_ATTACK_KNOCKBACK_POWER = 0.7F;
  public static final int KILLS_PER_COMPANION = 5;

  public PumpkinMaceItem(Item.Properties properties) {
    super(properties);
  }

  public static ItemAttributeModifiers createAttributes() {
    return ItemAttributeModifiers.builder()
        .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, DEFAULT_ATTACK_DAMAGE, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
        .add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, DEFAULT_ATTACK_SPEED, Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
        .build();
  }

  public static boolean isPumpkinEntity(LivingEntity entity) {

    if (entity instanceof OwnableEntity ownable && ownable.getOwnerReference() != null) {
      return false;
    }

    return entity instanceof JackOMimic || entity.getItemBySlot(EquipmentSlot.HEAD).is(Items.JACK_O_LANTERN);
  }

  public static void addPumpkinKill(ServerLevel level, Player player, ItemStack stack) {

    int kills = stack.getOrDefault(ModDataComponents.PUMPKIN_KILLS, 0) + 1;
    if (kills < KILLS_PER_COMPANION) {
      stack.set(ModDataComponents.PUMPKIN_KILLS, kills);
      return;
    }

    stack.set(ModDataComponents.PUMPKIN_KILLS, 0);
    SpectralCompanions.summon(level, player, 1);
  }

  @Override
  public void appendHoverText(ItemStack itemStack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {

    int kills = itemStack.getOrDefault(ModDataComponents.PUMPKIN_KILLS, 0);
    builder.accept(Component.translatable("item.spookiness.pumpkin_mace.pumpkin_kills", kills, KILLS_PER_COMPANION).withStyle(ChatFormatting.GOLD));
  }

  public static Tool createToolProperties() {

    return new Tool(List.of(), 1.0F, 2, false);
  }

  private static void knockback(Level level, Entity attacker, Entity entity) {

    // level.levelEvent(2013, entity.getOnPos(), 750);
    // LevelEventHandler#levelEvent -> ParticleUtils#spawnSmashAttackParticles
    // '750' here for 'data' is read as "count", gets divided to 250 and 500 for DUST_PILLAR particles
    level.levelEvent(LevelEvent.PARTICLES_SMASH_ATTACK, entity.getOnPos(), 750);

    for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(SMASH_ATTACK_KNOCKBACK_RADIUS), knockbackPredicate(attacker, entity))) {
      Vec3 direction = nearby.position().subtract(entity.position());
      double knockbackPower = getKnockbackPower(attacker, nearby, direction);
      Vec3 knockbackVector = direction.normalize().scale(knockbackPower);

      if (knockbackPower <= (double) 0.0F) {
        continue;
      }

      nearby.push(knockbackVector.x, SMASH_ATTACK_KNOCKBACK_POWER, knockbackVector.z);
      if (nearby instanceof ServerPlayer otherPlayer) {
        otherPlayer.connection.send(new ClientboundSetEntityMotionPacket(otherPlayer));
      }
    }
  }

  private static Predicate<LivingEntity> knockbackPredicate(Entity attacker, Entity entity) {

    return nearby -> !nearby.isSpectator()
        && nearby != attacker && nearby != entity && !attacker.isAlliedTo(nearby)
        && !(nearby instanceof TamableAnimal a && entity instanceof LivingEntity o
        && a.isTame() && a.isOwnedBy(o)) && !(nearby instanceof ArmorStand s && s.isMarker())
        && entity.distanceToSqr(nearby) <= Math.pow(SMASH_ATTACK_KNOCKBACK_RADIUS, 2)
        && !(nearby instanceof Player p && p.isCreative() && p.getAbilities().flying);
  }

  private static double getKnockbackPower(Entity attacker, LivingEntity nearby, Vec3 direction) {

    double bonusFallKnockback = attacker.fallDistance > (double) SMASH_ATTACK_HEAVY_THRESHOLD ? 2.0D : 1.0D;

    return ((double) SMASH_ATTACK_KNOCKBACK_RADIUS - direction.length())
        * (double) SMASH_ATTACK_KNOCKBACK_POWER * bonusFallKnockback
        * ((double) 1.0F - nearby.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
  }

  public static boolean canSmashAttack(LivingEntity attacker) {
    return attacker.fallDistance > (double) SMASH_ATTACK_FALL_THRESHOLD && !attacker.isFallFlying();
  }

  public void hurtEnemy(ItemStack itemStack, LivingEntity mob, LivingEntity attacker) {

    if (!canSmashAttack(attacker)) {
      return;
    }

    ServerLevel level = (ServerLevel) attacker.level();
    attacker.setDeltaMovement(attacker.getDeltaMovement().with(Axis.Y, 0.01F));
    attacker.setIgnoreFallDamageFromCurrentImpulse(true, this.calculateImpactPosition(attacker));

    if (attacker instanceof ServerPlayer player) {
      player.connection.send(new ClientboundSetEntityMotionPacket(player));
    }

    if (mob.onGround()) {
      if (attacker instanceof ServerPlayer player) {
        player.setSpawnExtraParticlesOnFall(true);
      }

      SoundEvent sound = attacker.fallDistance > (double) SMASH_ATTACK_HEAVY_THRESHOLD ? SoundEvents.MACE_SMASH_GROUND_HEAVY : SoundEvents.MACE_SMASH_GROUND;
      level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), sound, attacker.getSoundSource(), 1.0F, 1.0F);
    } else {
      level.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.MACE_SMASH_AIR, attacker.getSoundSource(), 1.0F, 1.0F);
    }

    knockback(level, attacker, mob);
  }

  private Vec3 calculateImpactPosition(LivingEntity attacker) {

    if (attacker.isIgnoringFallDamageFromCurrentImpulse() && attacker.currentImpulseImpactPos != null && attacker.currentImpulseImpactPos.y <= attacker.position().y) {
      return attacker.currentImpulseImpactPos;
    } else {
      return attacker.position();
    }
  }

  public void postHurtEnemy(ItemStack itemStack, LivingEntity mob, LivingEntity attacker) {
    if (canSmashAttack(attacker)) {
      attacker.resetFallDistance();
    }
  }

  public float getAttackDamageBonus(Entity victim, float ignoredDamage, DamageSource damageSource) {

    if (!(damageSource.getDirectEntity() instanceof LivingEntity attacker)) {
      return 0.0F;
    }

    if (!canSmashAttack(attacker)) {
      return 0.0F;
    }

    double fallHeightThreshold1 = 3.0F;
    double fallHeightThreshold2 = 8.0F;
    double fallDistance = attacker.fallDistance;

    double damage;
    if (fallDistance <= fallHeightThreshold1) {
      damage = (double) 4.0F * fallDistance;
    } else if (fallDistance <= fallHeightThreshold2) {
      damage = (double) 12.0F + (double) 2.0F * (fallDistance - fallHeightThreshold1);
    } else {
      damage = (double) 22.0F + fallDistance - fallHeightThreshold2;
    }

    if (attacker.level() instanceof ServerLevel level) {
      return (float) (damage + (double) EnchantmentHelper.modifyFallBasedDamage(level, attacker.getWeaponItem(), victim, damageSource, 0.0F) * fallDistance);
    } else {
      return (float) damage;
    }
  }

  @SuppressWarnings("deprecation")
  public @Nullable DamageSource getItemDamageSource(LivingEntity attacker) {

    return canSmashAttack(attacker) ? attacker.damageSources().mace(attacker) : super.getItemDamageSource(attacker);
  }
}
