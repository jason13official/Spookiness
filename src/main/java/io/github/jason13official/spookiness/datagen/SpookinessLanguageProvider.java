package io.github.jason13official.spookiness.datagen;

import io.github.jason13official.spookiness.Spookiness;
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
    this.add(ModItems.PIECE_OF_LAMENT_ONE, "Piece of Lament, One");
    this.add(ModItems.PIECE_OF_LAMENT_TWO, "Piece of Lament, Two");

    this.add(ModEntities.JACK_O_MIMIC, "Jack o'Lantern");
    this.add(ModEntities.FLOATING_CANDLES, "Floating Candles");
    this.add(ModEntities.FLOATING_BOOK, "Floating Book");
    this.add(ModEntities.FLOATING_SWORD, "Floating Sword");
    this.add(ModEntities.SPECTRAL_JACK_O_MIMIC, "Spectral Jack o'Mimic");

    this.add("item.spookiness.pumpkin_mace.pumpkin_kills", "Pumpkin Kills: %s/%s");
    this.add("message.spookiness.companion_summoned", "A spectral Jack o'Mimic joins you!");
    this.add("message.spookiness.companions_summoned", "%s spectral Jack o'Mimics join you!");
  }
}
