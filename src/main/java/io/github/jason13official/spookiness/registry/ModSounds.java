package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.Spookiness;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {

  public static final List<Placeholder> PLACEHOLDERS = new ArrayList<>();

  public static SoundEvent JACK_O_MIMIC_AMBIENT;
  public static SoundEvent JACK_O_MIMIC_HOP;
  public static SoundEvent JACK_O_MIMIC_HURT;
  public static SoundEvent JACK_O_MIMIC_DEATH;

  public static SoundEvent WICKMAN_AMBIENT;
  public static SoundEvent WICKMAN_HURT;
  public static SoundEvent WICKMAN_DEATH;

  public static SoundEvent HALLOWED_MOTHER_AMBIENT;
  public static SoundEvent HALLOWED_MOTHER_HURT;
  public static SoundEvent HALLOWED_MOTHER_DEATH;

  public static SoundEvent GOURDWYRM_AMBIENT;
  public static SoundEvent GOURDWYRM_HURT;
  public static SoundEvent GOURDWYRM_DEATH;

  public static SoundEvent MACE_EVOLVE;

  public static void register(BiConsumer<SoundEvent, Identifier> consumer) {

    JACK_O_MIMIC_AMBIENT = placeholder("entity.jack_o_mimic.ambient", "entity.bogged.ambient", 1.4F, "Jack o'Mimic rattles");
    JACK_O_MIMIC_HOP = placeholder("entity.jack_o_mimic.hop", "entity.slime.jump_small", 0.8F, "Jack o'Mimic hops");
    JACK_O_MIMIC_HURT = placeholder("entity.jack_o_mimic.hurt", "entity.polar_bear.hurt", 1.2F, "Jack o'Mimic hurts");
    JACK_O_MIMIC_DEATH = placeholder("entity.jack_o_mimic.death", "entity.snow_golem.death", 0.8F, "Jack o'Mimic splits");

    WICKMAN_AMBIENT = placeholder("entity.wickman.ambient", "entity.creaking.ambient", 0.7F, "Wickman creaks");
    WICKMAN_HURT = placeholder("entity.wickman.hurt", "entity.creaking.sway", 0.8F, "Wickman hurts");
    WICKMAN_DEATH = placeholder("entity.wickman.death", "entity.creaking.death", 0.6F, "Wickman burns out");

    HALLOWED_MOTHER_AMBIENT = placeholder("entity.hallowed_mother.ambient", "entity.ravager.ambient", 0.6F, "Hallowed Mother groans");
    HALLOWED_MOTHER_HURT = placeholder("entity.hallowed_mother.hurt", "entity.ravager.hurt", 0.6F, "Hallowed Mother hurts");
    HALLOWED_MOTHER_DEATH = placeholder("entity.hallowed_mother.death", "entity.ravager.death", 0.5F, "Hallowed Mother wilts");

    GOURDWYRM_AMBIENT = placeholder("entity.gourdwyrm.ambient", "entity.ender_dragon.ambient", 0.6F, "Gourdwyrm roars");
    GOURDWYRM_HURT = placeholder("entity.gourdwyrm.hurt", "entity.ender_dragon.hurt", 0.6F, "Gourdwyrm hurts");
    GOURDWYRM_DEATH = placeholder("entity.gourdwyrm.death", "entity.ender_dragon.death", 0.7F, "Gourdwyrm withers");

    MACE_EVOLVE = placeholder("item.pumpkin_mace.evolve", "item.totem.use", 1.4F, "Pumpkin Mace ripens");

    for (Placeholder placeholder : PLACEHOLDERS) {
      consumer.accept(placeholder.event(), placeholder.event().location());
    }
  }

  public static String subtitleKey(SoundEvent event) {
    return "subtitles." + event.location().getNamespace() + "." + event.location().getPath();
  }

  private static SoundEvent placeholder(String name, String vanilla, float pitch, String subtitle) {
    SoundEvent event = SoundEvent.createVariableRangeEvent(Spookiness.id(name));
    PLACEHOLDERS.add(new Placeholder(event, Identifier.withDefaultNamespace(vanilla), pitch, subtitle));
    return event;
  }

  public record Placeholder(SoundEvent event, Identifier vanilla, float pitch, String subtitle) {
  }
}
