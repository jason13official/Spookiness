package io.github.jason13official.spookiness.event;

import io.github.jason13official.spookiness.ritual.HallowedMotherTrigger;
import io.github.jason13official.spookiness.datagen.SpookinessDatagen;
import io.github.jason13official.spookiness.entity.book.FloatingBook;
import io.github.jason13official.spookiness.entity.FloatingCandles;
import io.github.jason13official.spookiness.entity.FloatingLantern;
import io.github.jason13official.spookiness.entity.FloatingSkull;
import io.github.jason13official.spookiness.entity.FloatingSword;
import io.github.jason13official.spookiness.entity.FloatingTool;
import io.github.jason13official.spookiness.entity.JackOMimic;
import io.github.jason13official.spookiness.entity.SpectralJackOMimic;
import io.github.jason13official.spookiness.entity.boss.gourdwyrm.Gourdwyrm;
import io.github.jason13official.spookiness.entity.boss.mother.HallowedMother;
import io.github.jason13official.spookiness.entity.boss.Wickman;
import io.github.jason13official.spookiness.entity.boss.WickmanHead;
import io.github.jason13official.spookiness.registry.ModEntities;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;

public final class SetupEvents {

  public static void register(IEventBus modBus, IEventBus gameBus) {

    // GatherDataEvent.Client
    modBus.addListener(SpookinessDatagen::init);

    // EntityAttributeCreationEvent
    modBus.addListener(SetupEvents::onAttributeCreation);

    // RegisterSpawnPlacementsEvent
    modBus.addListener(SetupEvents::onSpawnPlacements);

    // RegisterCommandsEvent
    gameBus.addListener((RegisterCommandsEvent event) -> HallowedMotherTrigger.registerCommands(event.getDispatcher()));
  }

  private static void onAttributeCreation(EntityAttributeCreationEvent event) {

    event.put(ModEntities.JACK_O_MIMIC, JackOMimic.createAttributes().build());
    event.put(ModEntities.FLOATING_CANDLES, FloatingCandles.createAttributes().build());
    event.put(ModEntities.FLOATING_BOOK, FloatingBook.createAttributes().build());
    event.put(ModEntities.FLOATING_SWORD, FloatingSword.createAttributes().build());
    event.put(ModEntities.FLOATING_SHEARS, FloatingTool.createAttributes().build());
    event.put(ModEntities.FLOATING_HOE, FloatingTool.createAttributes().build());
    event.put(ModEntities.FLOATING_LANTERN, FloatingLantern.createAttributes().build());
    event.put(ModEntities.FLOATING_SKULL, FloatingSkull.createAttributes().build());
    event.put(ModEntities.HAUNTED_ARMOR_STAND, ArmorStand.createAttributes().build());
    event.put(ModEntities.SPECTRAL_JACK_O_MIMIC, SpectralJackOMimic.createAttributes().build());
    event.put(ModEntities.WICKMAN, Wickman.createAttributes().build());
    event.put(ModEntities.WICKMAN_HEAD, WickmanHead.createAttributes().build());
    event.put(ModEntities.HALLOWED_MOTHER, HallowedMother.createAttributes().build());
    event.put(ModEntities.GOURDWYRM, Gourdwyrm.createAttributes().build());
  }

  private static void onSpawnPlacements(RegisterSpawnPlacementsEvent event) {

    event.register(ModEntities.JACK_O_MIMIC, SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, JackOMimic::checkJackOMimicSpawnRules,
        RegisterSpawnPlacementsEvent.Operation.REPLACE);
  }
}
