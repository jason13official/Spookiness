package io.github.jason13official.spookiness.world.netherrealm;

import io.github.jason13official.spookiness.Spookiness;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

public class ModStructures {

  public static final ResourceKey<Structure> NETHERREALM = ResourceKey.create(Registries.STRUCTURE, Spookiness.id("netherrealm"));
  public static final ResourceKey<StructureSet> NETHERREALM_SET = ResourceKey.create(Registries.STRUCTURE_SET, Spookiness.id("netherrealm"));

  private static final int SPACING = 64;
  private static final int SEPARATION = 32;
  private static final int SALT = 0x5B00C1;

  public static StructureType<NetherrealmStructure> NETHERREALM_TYPE;
  public static StructurePieceType NETHERREALM_PIECE;

  public static void registerTypes(BiConsumer<StructureType<?>, Identifier> consumer) {

    NETHERREALM_TYPE = () -> NetherrealmStructure.CODEC;
    consumer.accept(NETHERREALM_TYPE, Spookiness.id("netherrealm"));
  }

  public static void registerPieces(BiConsumer<StructurePieceType, Identifier> consumer) {

    NETHERREALM_PIECE = (StructurePieceType.ContextlessType) NetherrealmPiece::new;
    consumer.accept(NETHERREALM_PIECE, Spookiness.id("netherrealm_arena"));
  }

  public static void bootstrapStructures(BootstrapContext<Structure> context) {

    HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
    context.register(NETHERREALM, new NetherrealmStructure(new Structure.StructureSettings(biomes.getOrThrow(BiomeTags.IS_NETHER), Map.of(),
        GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.NONE)));
  }

  public static void bootstrapSets(BootstrapContext<StructureSet> context) {

    HolderGetter<Structure> structures = context.lookup(Registries.STRUCTURE);
    context.register(NETHERREALM_SET, new StructureSet(structures.getOrThrow(NETHERREALM),
        new RandomSpreadStructurePlacement(SPACING, SEPARATION, RandomSpreadType.LINEAR, SALT)));
  }
}
