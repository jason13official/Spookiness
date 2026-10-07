package io.github.jason13official.spookiness.datagen;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.registry.ModSounds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

public class SpookinessSoundDefinitionsProvider extends SoundDefinitionsProvider {

  public SpookinessSoundDefinitionsProvider(PackOutput output) {
    super(output, Spookiness.MOD_ID);
  }

  @Override
  public void registerSounds() {

    for (ModSounds.Placeholder placeholder : ModSounds.PLACEHOLDERS) {
      this.add(placeholder.event(), definition()
          .with(sound(placeholder.vanilla(), SoundDefinition.SoundType.EVENT).pitch(placeholder.pitch()))
          .subtitle(ModSounds.subtitleKey(placeholder.event())));
    }
  }
}
