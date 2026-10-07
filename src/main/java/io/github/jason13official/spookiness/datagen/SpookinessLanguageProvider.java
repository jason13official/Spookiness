package io.github.jason13official.spookiness.datagen;

import io.github.jason13official.spookiness.registry.ModSounds;
import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.registry.ModBlocks;
import io.github.jason13official.spookiness.registry.ModEntities;
import io.github.jason13official.spookiness.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class SpookinessLanguageProvider extends LanguageProvider {

  public SpookinessLanguageProvider(PackOutput output) {
    super(output, Spookiness.MOD_ID, "en_us");
  }

  @Override
  protected void addTranslations() {

    this.add(ModItems.PUMPKIN_MACE, "Pumpkin Mace");
    this.add(ModItems.LAMENT_CONFIGURATION, "Lament Configuration");
    this.add("item.spookiness.lament_configuration.return_home", "Ready to return home?");
    this.add(ModBlocks.SOULLESS_JACK_O_MIMIC, "Soulless Jack o'Mimic");
    this.add(ModItems.PIECE_OF_LAMENT_ONE, "Piece of Lament, One");
    this.add(ModItems.PIECE_OF_LAMENT_TWO, "Piece of Lament, Two");

    this.add(ModItems.JACK_O_MIMIC_SPAWN_EGG, "Jack o'Mimic Spawn Egg");
    this.add(ModItems.FLOATING_CANDLES_SPAWN_EGG, "Floating Candles Spawn Egg");
    this.add(ModItems.FLOATING_BOOK_SPAWN_EGG, "Floating Book Spawn Egg");
    this.add(ModItems.FLOATING_SWORD_SPAWN_EGG, "Floating Sword Spawn Egg");
    this.add(ModItems.FLOATING_SHEARS_SPAWN_EGG, "Floating Shears Spawn Egg");
    this.add(ModItems.FLOATING_HOE_SPAWN_EGG, "Floating Hoe Spawn Egg");
    this.add(ModItems.FLOATING_LANTERN_SPAWN_EGG, "Floating Lantern Spawn Egg");
    this.add(ModItems.FLOATING_SKULL_SPAWN_EGG, "Floating Skull Spawn Egg");
    this.add(ModItems.HAUNTED_ARMOR_STAND_SPAWN_EGG, "Haunted Armor Stand Spawn Egg");
    this.add(ModItems.SPECTRAL_JACK_O_MIMIC_SPAWN_EGG, "Spectral Jack o'Mimic Spawn Egg");
    this.add(ModItems.WICKMAN_SPAWN_EGG, "Wickman Spawn Egg");
    this.add(ModItems.HALLOWED_MOTHER_SPAWN_EGG, "Hallowed Mother Spawn Egg");
    this.add(ModItems.GOURDWYRM_SPAWN_EGG, "Gourdwyrm Spawn Egg");

    this.add(ModEntities.JACK_O_MIMIC, "Jack o'Lantern");
    this.add(ModEntities.FLOATING_CANDLES, "Floating Candles");
    this.add(ModEntities.FLOATING_BOOK, "Floating Book");
    this.add(ModEntities.FLOATING_SWORD, "Floating Sword");
    this.add(ModEntities.FLOATING_SHEARS, "Floating Shears");
    this.add(ModEntities.FLOATING_HOE, "Floating Hoe");
    this.add(ModEntities.FLOATING_LANTERN, "Floating Lantern");
    this.add(ModEntities.FLOATING_SKULL, "Floating Skull");
    this.add(ModEntities.HAUNTED_ARMOR_STAND, "Haunted Armor Stand");
    this.add(ModEntities.SPECTRAL_JACK_O_MIMIC, "Spectral Jack o'Mimic");
    this.add(ModEntities.WICKMAN, "The Wickman");
    this.add("entity.spookiness.frostwick", "The Frostwick");
    this.add(ModEntities.WICKMAN_HEAD, "Wickman's Head");
    this.add(ModEntities.VIGIL_CANDLE, "Vigil Candle");
    this.add(ModEntities.HALLOWED_MOTHER, "Hallowed Mother");
    this.add(ModEntities.GOURDWYRM, "Gourdwyrm");
    this.add(ModEntities.PUMPKIN_BOMB, "Pumpkin Bomb");

    this.add("item.spookiness.pumpkin_mace.pumpkin_kills", "Pumpkin Kills: %s/%s");
    this.add("item.spookiness.pumpkin_mace.harvest", "Harvest: %s/%s");
    this.add("item.spookiness.pumpkin_mace.stage.pumpkin", "Unripe");
    this.add("item.spookiness.pumpkin_mace.stage.carved", "Carved");
    this.add("item.spookiness.pumpkin_mace.stage.lantern", "Lit");
    this.add("item.spookiness.pumpkin_mace.stage.blazing", "Blazing");
    this.add("item.spookiness.pumpkin_mace.stage.thorned", "Thorned");
    this.add(ModItems.HARVEST_CROWN, "Harvest Crown");
    this.add("message.spookiness.companion_summoned", "A spectral Jack o'Mimic joins you!");
    this.add("message.spookiness.companions_summoned", "%s spectral Jack o'Mimics join you!");
    this.add("message.spookiness.hallowed", "%s is hallowed and joins you");
    this.add("message.spookiness.hallow_too_weak", "You are too weak to give any more of yourself");
    this.add("message.spookiness.hallow_too_many", "Your lantern cannot guide any more souls");
    this.add("message.spookiness.mother_warning", "Your lanterns grow restless...");
    this.add("message.spookiness.mother_sinks_dawn", "The Hallowed Mother retreats from the daylight");
    this.add("message.spookiness.mother_sinks_lost", "The Hallowed Mother has reclaimed her children");
    this.add("death.attack.spookiness.hallowing", "%1$s gave too much of themselves to the harvest");
    ModSounds.PLACEHOLDERS.forEach(placeholder -> this.add(ModSounds.subtitleKey(placeholder.event()), placeholder.subtitle()));
    SpookinessAdvancementProvider.TEXT.forEach((name, text) -> {
      this.add(SpookinessAdvancementProvider.key(name, "title"), text[0]);
      this.add(SpookinessAdvancementProvider.key(name, "description"), text[1]);
    });
    this.add("death.attack.spookiness.hallowing.player", "%1$s gave too much of themselves to the harvest");
  }
}
