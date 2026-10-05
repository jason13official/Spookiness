package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.entity.FloatingBook;
import io.github.jason13official.spookiness.entity.FloatingCandles;
import io.github.jason13official.spookiness.entity.FloatingSword;
import io.github.jason13official.spookiness.entity.JackOMimic;
import io.github.jason13official.spookiness.entity.SpectralJackOMimic;
import io.github.jason13official.spookiness.entity.boss.Gourdwyrm;
import io.github.jason13official.spookiness.entity.boss.HallowedMother;
import io.github.jason13official.spookiness.entity.boss.VigilCandle;
import io.github.jason13official.spookiness.entity.boss.Wickman;
import java.util.function.BiConsumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

  public static EntityType<JackOMimic> JACK_O_MIMIC;
  public static EntityType<FloatingCandles> FLOATING_CANDLES;
  public static EntityType<FloatingBook> FLOATING_BOOK;
  public static EntityType<FloatingSword> FLOATING_SWORD;
  public static EntityType<SpectralJackOMimic> SPECTRAL_JACK_O_MIMIC;
  public static EntityType<Wickman> WICKMAN;
  public static EntityType<VigilCandle> VIGIL_CANDLE;
  public static EntityType<HallowedMother> HALLOWED_MOTHER;
  public static EntityType<Gourdwyrm> GOURDWYRM;

  public static void register(BiConsumer<EntityType<?>, Identifier> consumer) {

    JACK_O_MIMIC = EntityType.Builder.of(JackOMimic::new, MobCategory.CREATURE).sized(1.0f, 1.0f).clientTrackingRange(16).build(key("jack_o_mimic"));
    consumer.accept(JACK_O_MIMIC, Spookiness.id("jack_o_mimic"));

    FLOATING_CANDLES = EntityType.Builder.of(FloatingCandles::new, MobCategory.AMBIENT).sized(0.5f, 0.5f).clientTrackingRange(16).build(key("floating_candles"));
    consumer.accept(FLOATING_CANDLES, Spookiness.id("floating_candles"));

    FLOATING_BOOK = EntityType.Builder.of(FloatingBook::new, MobCategory.CREATURE).sized(0.8f, 0.5f).clientTrackingRange(8).build(key("floating_book"));
    consumer.accept(FLOATING_BOOK, Spookiness.id("floating_book"));

    FLOATING_SWORD = EntityType.Builder.of(FloatingSword::new, MobCategory.MONSTER).sized(0.6f, 1.95f).clientTrackingRange(8).build(key("floating_sword"));
    consumer.accept(FLOATING_SWORD, Spookiness.id("floating_sword"));

    SPECTRAL_JACK_O_MIMIC = EntityType.Builder.of(SpectralJackOMimic::new, MobCategory.MISC).sized(1.0f, 1.0f).clientTrackingRange(10).build(key("spectral_jack_o_mimic"));
    consumer.accept(SPECTRAL_JACK_O_MIMIC, Spookiness.id("spectral_jack_o_mimic"));

    WICKMAN = EntityType.Builder.of(Wickman::new, MobCategory.MONSTER).fireImmune().sized(1.0f, 3.2f).clientTrackingRange(10).build(key("wickman"));
    consumer.accept(WICKMAN, Spookiness.id("wickman"));

    VIGIL_CANDLE = EntityType.Builder.<VigilCandle>of(VigilCandle::new, MobCategory.MISC).noLootTable().fireImmune().sized(0.5f, 0.9f).clientTrackingRange(10)
        .build(key("vigil_candle"));
    consumer.accept(VIGIL_CANDLE, Spookiness.id("vigil_candle"));

    HALLOWED_MOTHER = EntityType.Builder.of(HallowedMother::new, MobCategory.MONSTER).sized(4.0f, 4.0f).clientTrackingRange(10).build(key("hallowed_mother"));
    consumer.accept(HALLOWED_MOTHER, Spookiness.id("hallowed_mother"));

    GOURDWYRM = EntityType.Builder.of(Gourdwyrm::new, MobCategory.MONSTER).fireImmune().sized(3.0f, 3.0f).clientTrackingRange(16).build(key("gourdwyrm"));
    consumer.accept(GOURDWYRM, Spookiness.id("gourdwyrm"));
  }

  private static ResourceKey<EntityType<?>> key(String path) {

    return ResourceKey.create(Registries.ENTITY_TYPE, Spookiness.id(path));
  }
}
