package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.block.GourdwyrmTrophyBlock;
import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.block.SoullessJackOMimicBlock;
import java.util.function.BiConsumer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class ModBlocks {

  public static Block SOULLESS_JACK_O_MIMIC;
  public static Block GOURDWYRM_TROPHY;

  public static void register(BiConsumer<Block, Identifier> consumer) {

    SOULLESS_JACK_O_MIMIC = new SoullessJackOMimicBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(1.5F).sound(SoundType.WOOD)
        .noOcclusion().pushReaction(PushReaction.BLOCK).setId(key("soulless_jack_o_mimic")));
    consumer.accept(SOULLESS_JACK_O_MIMIC, Spookiness.id("soulless_jack_o_mimic"));

    GOURDWYRM_TROPHY = new GourdwyrmTrophyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(3.0F, 6.0F).sound(SoundType.METAL)
        .lightLevel(state -> 10).noOcclusion().setId(key("gourdwyrm_trophy")));
    consumer.accept(GOURDWYRM_TROPHY, Spookiness.id("gourdwyrm_trophy"));
  }

  private static ResourceKey<Block> key(String path) {

    return ResourceKey.create(Registries.BLOCK, Spookiness.id(path));
  }
}
