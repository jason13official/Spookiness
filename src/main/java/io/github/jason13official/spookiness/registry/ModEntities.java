package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.entity.projectile.FrostVolley;
import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.entity.FloatingBook;
import io.github.jason13official.spookiness.entity.FloatingCandles;
import io.github.jason13official.spookiness.entity.FloatingHoe;
import io.github.jason13official.spookiness.entity.FloatingLantern;
import io.github.jason13official.spookiness.entity.FloatingShears;
import io.github.jason13official.spookiness.entity.FloatingSkull;
import io.github.jason13official.spookiness.entity.FloatingSword;
import io.github.jason13official.spookiness.entity.HauntedArmorStand;
import io.github.jason13official.spookiness.entity.JackOMimic;
import io.github.jason13official.spookiness.entity.SpectralJackOMimic;
import io.github.jason13official.spookiness.entity.boss.Gourdwyrm;
import io.github.jason13official.spookiness.entity.projectile.PumpkinBomb;
import io.github.jason13official.spookiness.entity.boss.HallowedMother;
import io.github.jason13official.spookiness.entity.boss.VigilCandle;
import io.github.jason13official.spookiness.entity.boss.Wickman;
import io.github.jason13official.spookiness.entity.boss.WickmanHead;
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
  public static EntityType<FloatingShears> FLOATING_SHEARS;
  public static EntityType<FloatingHoe> FLOATING_HOE;
  public static EntityType<FloatingLantern> FLOATING_LANTERN;
  public static EntityType<FloatingSkull> FLOATING_SKULL;
  public static EntityType<HauntedArmorStand> HAUNTED_ARMOR_STAND;
  public static EntityType<SpectralJackOMimic> SPECTRAL_JACK_O_MIMIC;
  public static EntityType<Wickman> WICKMAN;
  public static EntityType<WickmanHead> WICKMAN_HEAD;
  public static EntityType<VigilCandle> VIGIL_CANDLE;
  public static EntityType<HallowedMother> HALLOWED_MOTHER;
  public static EntityType<Gourdwyrm> GOURDWYRM;
  public static EntityType<PumpkinBomb> PUMPKIN_BOMB;
  public static EntityType<FrostVolley> FROST_VOLLEY;

  public static void register(BiConsumer<EntityType<?>, Identifier> consumer) {

    JACK_O_MIMIC = EntityType.Builder.of(JackOMimic::new, MobCategory.CREATURE).sized(1.0f, 1.0f).clientTrackingRange(16).build(key("jack_o_mimic"));
    consumer.accept(JACK_O_MIMIC, Spookiness.id("jack_o_mimic"));

    FLOATING_CANDLES = EntityType.Builder.of(FloatingCandles::new, MobCategory.AMBIENT).sized(0.5f, 0.5f).clientTrackingRange(16).build(key("floating_candles"));
    consumer.accept(FLOATING_CANDLES, Spookiness.id("floating_candles"));

    FLOATING_BOOK = EntityType.Builder.of(FloatingBook::new, MobCategory.CREATURE).sized(0.8f, 0.5f).clientTrackingRange(8).build(key("floating_book"));
    consumer.accept(FLOATING_BOOK, Spookiness.id("floating_book"));

    FLOATING_SWORD = EntityType.Builder.of(FloatingSword::new, MobCategory.MONSTER).sized(0.6f, 1.95f).clientTrackingRange(8).build(key("floating_sword"));
    consumer.accept(FLOATING_SWORD, Spookiness.id("floating_sword"));

    FLOATING_SHEARS = EntityType.Builder.of(FloatingShears::new, MobCategory.CREATURE).sized(0.6f, 1.95f).clientTrackingRange(8).build(key("floating_shears"));
    consumer.accept(FLOATING_SHEARS, Spookiness.id("floating_shears"));

    FLOATING_HOE = EntityType.Builder.of(FloatingHoe::new, MobCategory.CREATURE).sized(0.6f, 1.95f).clientTrackingRange(8).build(key("floating_hoe"));
    consumer.accept(FLOATING_HOE, Spookiness.id("floating_hoe"));

    FLOATING_LANTERN = EntityType.Builder.of(FloatingLantern::new, MobCategory.AMBIENT).fireImmune().sized(0.4f, 0.6f).clientTrackingRange(16).build(key("floating_lantern"));
    consumer.accept(FLOATING_LANTERN, Spookiness.id("floating_lantern"));

    FLOATING_SKULL = EntityType.Builder.of(FloatingSkull::new, MobCategory.MONSTER).sized(0.5f, 0.5f).clientTrackingRange(8).build(key("floating_skull"));
    consumer.accept(FLOATING_SKULL, Spookiness.id("floating_skull"));

    HAUNTED_ARMOR_STAND = EntityType.Builder.<HauntedArmorStand>of(HauntedArmorStand::new, MobCategory.MISC).sized(0.5f, 1.975f).eyeHeight(1.7775f).clientTrackingRange(10)
        .build(key("haunted_armor_stand"));
    consumer.accept(HAUNTED_ARMOR_STAND, Spookiness.id("haunted_armor_stand"));

    SPECTRAL_JACK_O_MIMIC = EntityType.Builder.of(SpectralJackOMimic::new, MobCategory.MISC).sized(1.0f, 1.0f).clientTrackingRange(10).build(key("spectral_jack_o_mimic"));
    consumer.accept(SPECTRAL_JACK_O_MIMIC, Spookiness.id("spectral_jack_o_mimic"));

    WICKMAN = EntityType.Builder.of(Wickman::new, MobCategory.MONSTER).fireImmune().sized(1.0f, 3.2f).clientTrackingRange(10).build(key("wickman"));
    consumer.accept(WICKMAN, Spookiness.id("wickman"));

    WICKMAN_HEAD = EntityType.Builder.of(WickmanHead::new, MobCategory.MISC).noLootTable().fireImmune().sized(1.0f, 1.0f).clientTrackingRange(10)
        .build(key("wickman_head"));
    consumer.accept(WICKMAN_HEAD, Spookiness.id("wickman_head"));

    VIGIL_CANDLE = EntityType.Builder.<VigilCandle>of(VigilCandle::new, MobCategory.MISC).noLootTable().fireImmune().sized(0.5f, 0.9f).clientTrackingRange(10)
        .build(key("vigil_candle"));
    consumer.accept(VIGIL_CANDLE, Spookiness.id("vigil_candle"));

    HALLOWED_MOTHER = EntityType.Builder.of(HallowedMother::new, MobCategory.MONSTER).sized(4.0f, 4.0f).clientTrackingRange(10).build(key("hallowed_mother"));
    consumer.accept(HALLOWED_MOTHER, Spookiness.id("hallowed_mother"));

    GOURDWYRM = EntityType.Builder.of(Gourdwyrm::new, MobCategory.MONSTER).fireImmune().sized(3.0f, 3.0f).clientTrackingRange(16).build(key("gourdwyrm"));
    consumer.accept(GOURDWYRM, Spookiness.id("gourdwyrm"));

    PUMPKIN_BOMB = EntityType.Builder.<PumpkinBomb>of(PumpkinBomb::new, MobCategory.MISC).noLootTable().sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(10)
        .build(key("pumpkin_bomb"));
    consumer.accept(PUMPKIN_BOMB, Spookiness.id("pumpkin_bomb"));

    FROST_VOLLEY = EntityType.Builder.<FrostVolley>of(FrostVolley::new, MobCategory.MISC).noLootTable().sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10)
        .build(key("frost_volley"));
    consumer.accept(FROST_VOLLEY, Spookiness.id("frost_volley"));
  }

  private static ResourceKey<EntityType<?>> key(String path) {

    return ResourceKey.create(Registries.ENTITY_TYPE, Spookiness.id(path));
  }
}
