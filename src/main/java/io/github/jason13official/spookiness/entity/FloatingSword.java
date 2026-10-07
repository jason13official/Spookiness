package io.github.jason13official.spookiness.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class FloatingSword extends FloatingTool implements Enemy {

  public FloatingSword(EntityType<? extends FloatingSword> type, Level level) {
    super(type, level);
  }

  @Override
  protected void registerTargetGoals() {
    this.registerHostileTargets();
  }

  @Override
  protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
    this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(random.nextInt(4) == 0 ? Items.GOLDEN_SWORD : Items.IRON_SWORD));
  }
}
