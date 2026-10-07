package io.github.jason13official.spookiness.item;

import io.github.jason13official.spookiness.entity.PumpkinHeads;
import io.github.jason13official.spookiness.registry.ModSounds;
import io.github.jason13official.spookiness.companion.SpectralCompanions;
import io.github.jason13official.spookiness.advancement.SpookyTrigger;
import io.github.jason13official.spookiness.effect.Particles;
import io.github.jason13official.spookiness.registry.ModDataComponents;
import io.github.jason13official.spookiness.registry.ModItems;
import java.util.List;
import java.util.function.Consumer;
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
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.animal.golem.SnowGolem;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class PumpkinMaceItem extends Item {

  /// 1.5F originally
  public static final float SMASH_ATTACK_FALL_THRESHOLD = 0.1F;
  /// -3.4 originally
  private static final float DEFAULT_ATTACK_SPEED = -2.4F;
  private static final int DEFAULT_ATTACK_DAMAGE = 5;
  private static final float SMASH_ATTACK_HEAVY_THRESHOLD = 5.0F;
  public static final int KILLS_PER_COMPANION = 5;
  private static final float BLAZING_FIRE_SECONDS = 3.0F;
  private static final float THORNED_DAMAGE_BONUS = 5.0F;

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

    if (entity instanceof SnowGolem golem) {
      return golem.hasPumpkin();
    }

    return PumpkinHeads.isPumpkinHeaded(entity);
  }

  public static MaceStage getStage(ItemStack stack) {
    return stack.getOrDefault(ModDataComponents.MACE_STAGE, MaceStage.PUMPKIN);
  }

  public static int getLight(ItemStack stack) {
    return stack.is(ModItems.PUMPKIN_MACE) ? getStage(stack).light() : 0;
  }

  private static void addHarvest(ServerLevel level, Player player, ItemStack stack) {

    int harvest = stack.getOrDefault(ModDataComponents.MACE_HARVEST, 0) + 1;
    stack.set(ModDataComponents.MACE_HARVEST, harvest);

    MaceStage stage = MaceStage.forKills(harvest);
    if (stage == getStage(stack)) {
      return;
    }

    stack.set(ModDataComponents.MACE_STAGE, stage);
    Particles.soulBurst(level, player.getBoundingBox().getCenter(), 32, 0.6, 0.06);
    level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.MACE_EVOLVE, player.getSoundSource(), 0.6F, 1.0F);
    SpookyTrigger.award(player, SpookyTrigger.MACE_EVOLVED + stage.getSerializedName());
  }

  public static void addPumpkinKill(ServerLevel level, Player player, ItemStack stack) {

    addHarvest(level, player, stack);

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

    MaceStage stage = getStage(itemStack);
    builder.accept(Component.translatable("item.spookiness.pumpkin_mace.stage." + stage.getSerializedName()).withStyle(ChatFormatting.DARK_GREEN));
    if (stage.ignites()) {
      builder.accept(Component.translatable("item.spookiness.pumpkin_mace.ignites").withStyle(ChatFormatting.RED));
    }
    if (stage == MaceStage.THORNED) {
      builder.accept(Component.translatable("item.spookiness.pumpkin_mace.thorns", (int) THORNED_DAMAGE_BONUS).withStyle(ChatFormatting.DARK_GREEN));
    }
    if (!stage.isFinal()) {
      int harvest = itemStack.getOrDefault(ModDataComponents.MACE_HARVEST, 0);
      builder.accept(Component.translatable("item.spookiness.pumpkin_mace.harvest", harvest, stage.next().kills()).withStyle(ChatFormatting.GRAY));
    }
  }

  public static Tool createToolProperties() {

    return new Tool(List.of(), 1.0F, 2, false);
  }

  public static boolean canSmashAttack(LivingEntity attacker) {
    return attacker.fallDistance > (double) SMASH_ATTACK_FALL_THRESHOLD && !attacker.isFallFlying();
  }

  @Override
  public void hurtEnemy(ItemStack itemStack, LivingEntity mob, LivingEntity attacker) {

    MaceStage stage = getStage(itemStack);
    if (stage.ignites()) {
      mob.igniteForSeconds(BLAZING_FIRE_SECONDS);
    }
    if (stage == MaceStage.THORNED) {
      attacker.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.THORNS_HIT, attacker.getSoundSource(), 1.0F, 0.8F);
    }

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

    MaceItem.knockback(level, attacker, mob);
  }

  private Vec3 calculateImpactPosition(LivingEntity attacker) {

    if (attacker.isIgnoringFallDamageFromCurrentImpulse() && attacker.currentImpulseImpactPos != null && attacker.currentImpulseImpactPos.y <= attacker.position().y) {
      return attacker.currentImpulseImpactPos;
    } else {
      return attacker.position();
    }
  }

  @Override
  public void postHurtEnemy(ItemStack itemStack, LivingEntity mob, LivingEntity attacker) {
    if (canSmashAttack(attacker)) {
      attacker.resetFallDistance();
    }
  }

  @Override
  public float getAttackDamageBonus(Entity victim, float ignoredDamage, DamageSource damageSource) {

    if (!(damageSource.getDirectEntity() instanceof LivingEntity attacker)) {
      return 0.0F;
    }

    float stageBonus = getStage(attacker.getWeaponItem()) == MaceStage.THORNED ? THORNED_DAMAGE_BONUS : 0.0F;
    if (!canSmashAttack(attacker)) {
      return stageBonus;
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
      return stageBonus + (float) (damage + (double) EnchantmentHelper.modifyFallBasedDamage(level, attacker.getWeaponItem(), victim, damageSource, 0.0F) * fallDistance);
    } else {
      return stageBonus + (float) damage;
    }
  }

  @Override
  @SuppressWarnings("deprecation")
  public @Nullable DamageSource getItemDamageSource(LivingEntity attacker) {

    return canSmashAttack(attacker) ? attacker.damageSources().mace(attacker) : super.getItemDamageSource(attacker);
  }
}
