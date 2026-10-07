package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.block.entity.GourdwyrmTrophyBlockEntity;
import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.block.entity.SoullessJackOMimicBlockEntity;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

  public static BlockEntityType<SoullessJackOMimicBlockEntity> SOULLESS_JACK_O_MIMIC;
  public static BlockEntityType<GourdwyrmTrophyBlockEntity> GOURDWYRM_TROPHY;

  public static void register(BiConsumer<BlockEntityType<?>, Identifier> consumer) {

    SOULLESS_JACK_O_MIMIC = new BlockEntityType<>(SoullessJackOMimicBlockEntity::new, Set.of(ModBlocks.SOULLESS_JACK_O_MIMIC));
    consumer.accept(SOULLESS_JACK_O_MIMIC, Spookiness.id("soulless_jack_o_mimic"));

    GOURDWYRM_TROPHY = new BlockEntityType<>(GourdwyrmTrophyBlockEntity::new, Set.of(ModBlocks.GOURDWYRM_TROPHY));
    consumer.accept(GOURDWYRM_TROPHY, Spookiness.id("gourdwyrm_trophy"));
  }
}
