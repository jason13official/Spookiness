package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.entity.JackOMimic;
import java.util.function.BiConsumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

  public static EntityType<JackOMimic> JACK_O_MIMIC;

  /// floating candles
  /// floating book
  /// invisible mob holding sword like Spectral Sword from Curse of Darkness?
  public static void register(BiConsumer<EntityType<?>, Identifier> consumer) {

    JACK_O_MIMIC = EntityType.Builder.of(JackOMimic::new, MobCategory.MISC).sized(1.0f, 1.0f).build(ResourceKey.create(Registries.ENTITY_TYPE, Spookiness.id("jack_o_mimic")));
    consumer.accept(JACK_O_MIMIC, Spookiness.id("jack_o_mimic"));
  }
}
