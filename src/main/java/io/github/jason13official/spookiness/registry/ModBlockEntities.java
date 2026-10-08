package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.block.entity.GourdwyrmTrophyBlockEntity;
import io.github.jason13official.spookiness.block.entity.JackOMimicStemBlockEntity;
import io.github.jason13official.spookiness.block.entity.LurkingPumpkinBlockEntity;
import io.github.jason13official.spookiness.block.entity.SoullessJackOMimicBlockEntity;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

  public static BlockEntityType<SoullessJackOMimicBlockEntity> SOULLESS_JACK_O_MIMIC;
  public static BlockEntityType<GourdwyrmTrophyBlockEntity> GOURDWYRM_TROPHY;
  public static BlockEntityType<LurkingPumpkinBlockEntity> LURKING_PUMPKIN;
  public static BlockEntityType<JackOMimicStemBlockEntity> JACK_O_MIMIC_STEM;

  public static void register(BiConsumer<BlockEntityType<?>, Identifier> consumer) {

    SOULLESS_JACK_O_MIMIC = register(consumer, "soulless_jack_o_mimic", SoullessJackOMimicBlockEntity::new, ModBlocks.SOULLESS_JACK_O_MIMIC);
    GOURDWYRM_TROPHY = register(consumer, "gourdwyrm_trophy", GourdwyrmTrophyBlockEntity::new, ModBlocks.GOURDWYRM_TROPHY);
    LURKING_PUMPKIN = register(consumer, "lurking_pumpkin", LurkingPumpkinBlockEntity::new, ModBlocks.LURKING_CARVED_PUMPKIN, ModBlocks.LURKING_JACK_O_LANTERN);
    JACK_O_MIMIC_STEM = register(consumer, "jack_o_mimic_stem", JackOMimicStemBlockEntity::new, ModBlocks.JACK_O_MIMIC_STEM);
  }

  private static <T extends BlockEntity> BlockEntityType<T> register(BiConsumer<BlockEntityType<?>, Identifier> consumer, String name,
      BlockEntityType.BlockEntitySupplier<? extends T> factory, Block... validBlocks) {

    BlockEntityType<T> type = new BlockEntityType<>(factory, Set.of(validBlocks));
    consumer.accept(type, Spookiness.id(name));
    return type;
  }
}
