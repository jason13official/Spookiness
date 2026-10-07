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
import net.minecraft.world.entity.Entity;
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

    JACK_O_MIMIC = register(consumer, "jack_o_mimic", EntityType.Builder.of(JackOMimic::new, MobCategory.CREATURE).sized(1.0f, 1.0f).clientTrackingRange(16));
    FLOATING_CANDLES = register(consumer, "floating_candles", EntityType.Builder.of(FloatingCandles::new, MobCategory.AMBIENT).sized(0.5f, 0.5f).clientTrackingRange(16));
    FLOATING_BOOK = register(consumer, "floating_book", EntityType.Builder.of(FloatingBook::new, MobCategory.CREATURE).sized(0.8f, 0.5f).clientTrackingRange(8));
    FLOATING_SWORD = register(consumer, "floating_sword", EntityType.Builder.of(FloatingSword::new, MobCategory.MONSTER).sized(0.6f, 1.95f).clientTrackingRange(8));
    FLOATING_SHEARS = register(consumer, "floating_shears", EntityType.Builder.of(FloatingShears::new, MobCategory.CREATURE).sized(0.6f, 1.95f).clientTrackingRange(8));
    FLOATING_HOE = register(consumer, "floating_hoe", EntityType.Builder.of(FloatingHoe::new, MobCategory.CREATURE).sized(0.6f, 1.95f).clientTrackingRange(8));
    FLOATING_LANTERN = register(consumer, "floating_lantern", EntityType.Builder.of(FloatingLantern::new, MobCategory.AMBIENT).fireImmune().sized(0.4f, 0.6f).clientTrackingRange(16));
    FLOATING_SKULL = register(consumer, "floating_skull", EntityType.Builder.of(FloatingSkull::new, MobCategory.MONSTER).sized(0.5f, 0.5f).clientTrackingRange(8));
    HAUNTED_ARMOR_STAND = register(consumer, "haunted_armor_stand", EntityType.Builder.<HauntedArmorStand>of(HauntedArmorStand::new, MobCategory.MISC).sized(0.5f, 1.975f).eyeHeight(1.7775f).clientTrackingRange(10));
    SPECTRAL_JACK_O_MIMIC = register(consumer, "spectral_jack_o_mimic", EntityType.Builder.of(SpectralJackOMimic::new, MobCategory.MISC).sized(1.0f, 1.0f).clientTrackingRange(10));
    WICKMAN = register(consumer, "wickman", EntityType.Builder.of(Wickman::new, MobCategory.MONSTER).fireImmune().sized(1.0f, 3.2f).clientTrackingRange(10));
    WICKMAN_HEAD = register(consumer, "wickman_head", EntityType.Builder.of(WickmanHead::new, MobCategory.MISC).noLootTable().fireImmune().sized(1.0f, 1.0f).clientTrackingRange(10));
    VIGIL_CANDLE = register(consumer, "vigil_candle", EntityType.Builder.<VigilCandle>of(VigilCandle::new, MobCategory.MISC).noLootTable().fireImmune().sized(0.5f, 0.9f).clientTrackingRange(10));
    HALLOWED_MOTHER = register(consumer, "hallowed_mother", EntityType.Builder.of(HallowedMother::new, MobCategory.MONSTER).sized(4.0f, 4.0f).clientTrackingRange(10));
    GOURDWYRM = register(consumer, "gourdwyrm", EntityType.Builder.of(Gourdwyrm::new, MobCategory.MONSTER).fireImmune().sized(3.0f, 3.0f).clientTrackingRange(16));
    PUMPKIN_BOMB = register(consumer, "pumpkin_bomb", EntityType.Builder.<PumpkinBomb>of(PumpkinBomb::new, MobCategory.MISC).noLootTable().sized(0.5f, 0.5f).clientTrackingRange(4).updateInterval(10));
    FROST_VOLLEY = register(consumer, "frost_volley", EntityType.Builder.<FrostVolley>of(FrostVolley::new, MobCategory.MISC).noLootTable().sized(0.25f, 0.25f).clientTrackingRange(4).updateInterval(10));
  }

  private static <T extends Entity> EntityType<T> register(BiConsumer<EntityType<?>, Identifier> consumer, String name, EntityType.Builder<T> builder) {

    ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, Spookiness.id(name));
    EntityType<T> type = builder.build(key);
    consumer.accept(type, key.identifier());
    return type;
  }
}
