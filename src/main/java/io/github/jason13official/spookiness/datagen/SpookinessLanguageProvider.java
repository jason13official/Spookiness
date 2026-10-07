package io.github.jason13official.spookiness.datagen;

import io.github.jason13official.spookiness.registry.ModSounds;
import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.registry.ModBlocks;
import io.github.jason13official.spookiness.registry.ModEntities;
import io.github.jason13official.spookiness.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
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
    this.add(ModBlocks.LURKING_CARVED_PUMPKIN, "Carved Pumpkin");
    this.add(ModBlocks.LURKING_JACK_O_LANTERN, "Jack o'Lantern");
    this.add(ModItems.PIECE_OF_LAMENT_ONE, "Piece of Lament, One");
    this.add(ModItems.PIECE_OF_LAMENT_TWO, "Piece of Lament, Two");

    this.addEntity(ModEntities.JACK_O_MIMIC, "Jack o'Lantern", "Jack o'Mimic");
    this.addEntity(ModEntities.FLOATING_CANDLES, "Floating Candles");
    this.addEntity(ModEntities.FLOATING_BOOK, "Floating Book");
    this.addEntity(ModEntities.FLOATING_SWORD, "Floating Sword");
    this.addEntity(ModEntities.FLOATING_SHEARS, "Floating Shears");
    this.addEntity(ModEntities.FLOATING_HOE, "Floating Hoe");
    this.addEntity(ModEntities.FLOATING_LANTERN, "Floating Lantern");
    this.addEntity(ModEntities.FLOATING_SKULL, "Floating Skull");
    this.addEntity(ModEntities.HAUNTED_ARMOR_STAND, "Haunted Armor Stand");
    this.addEntity(ModEntities.SPECTRAL_JACK_O_MIMIC, "Spectral Jack o'Mimic");
    this.addEntity(ModEntities.WICKMAN, "The Wickman", "Wickman");
    this.add("entity.spookiness.frostwick", "The Frostwick");
    this.addEntity(ModEntities.WICKMAN_HEAD, "Wickman's Head");
    this.addEntity(ModEntities.VIGIL_CANDLE, "Vigil Candle");
    this.addEntity(ModEntities.HALLOWED_MOTHER, "Hallowed Mother");
    this.addEntity(ModEntities.GOURDWYRM, "Gourdwyrm");
    this.addEntity(ModEntities.PUMPKIN_BOMB, "Pumpkin Bomb");
    this.addEntity(ModEntities.FROST_VOLLEY, "Frost Volley");

    this.add("item.spookiness.pumpkin_mace.pumpkin_kills", "Pumpkin Kills: %s/%s");
    this.add("item.spookiness.pumpkin_mace.harvest", "Harvest: %s/%s");
    this.add("item.spookiness.pumpkin_mace.ignites", "Sets targets alight");
    this.add("item.spookiness.pumpkin_mace.thorns", "+%s Thorn Damage");
    this.add("item.spookiness.pumpkin_mace.stage.pumpkin", "Unripe");
    this.add("item.spookiness.pumpkin_mace.stage.carved", "Carved");
    this.add("item.spookiness.pumpkin_mace.stage.lantern", "Lit");
    this.add("item.spookiness.pumpkin_mace.stage.blazing", "Blazing");
    this.add("item.spookiness.pumpkin_mace.stage.thorned", "Thorned");
    this.add(ModItems.HARVEST_CROWN, "Harvest Crown");
    this.add(ModBlocks.GOURDWYRM_TROPHY, "Gourdwyrm Trophy");
    this.add("message.spookiness.companion_summoned", "A spectral Jack o'Mimic joins you!");
    this.add("message.spookiness.companions_summoned", "%s spectral Jack o'Mimics join you!");
    this.add("message.spookiness.hallowed", "%s is hallowed and joins you");
    this.add("message.spookiness.hallow_too_weak", "You are too weak to give any more of yourself");
    this.add("message.spookiness.hallow_too_many", "Your lantern cannot guide any more souls");
    this.add("message.spookiness.mother_warning", "The Hallowed Mother stirs beneath the field and wants to meet your family...");
    this.add("message.spookiness.mother_accept", "[Accept]");
    this.add("message.spookiness.haunted_harvest", "The Haunted Harvest has begun. Everything in the field is waking up...");
    this.add("message.spookiness.mother_accept_hover", "Let her rise and fight for your Hallowed allies");
    this.add("message.spookiness.mother_deny", "[Deny]");
    this.add("message.spookiness.mother_deny_hover", "Let her sleep until tomorrow night");
    this.add("message.spookiness.mother_denied", "The field falls quiet again. Perhaps tomorrow night.");
    this.add("message.spookiness.mother_gone", "Nothing answers. The moment has passed.");
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

  private void addEntity(EntityType<?> type, String name) {

    this.addEntity(type, name, name);
  }

  private void addEntity(EntityType<?> type, String name, String eggName) {

    this.add(type, name);
    ModItems.SPAWN_EGGS.stream().filter(egg -> egg.type() == type).forEach(egg -> this.add(egg.item(), eggName + " Spawn Egg"));
  }
}
