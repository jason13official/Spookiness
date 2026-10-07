package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.block.GourdwyrmTrophyBlock;
import io.github.jason13official.spookiness.block.SoullessJackOMimicBlock;
import java.util.function.BiConsumer;
import java.util.function.Function;
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

    SOULLESS_JACK_O_MIMIC = register(consumer, "soulless_jack_o_mimic", SoullessJackOMimicBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE)
        .strength(1.5F).sound(SoundType.WOOD).lightLevel(state -> state.getValue(SoullessJackOMimicBlock.LIT) ? 15 : 0).noOcclusion().pushReaction(PushReaction.BLOCK));

    GOURDWYRM_TROPHY = register(consumer, "gourdwyrm_trophy", GourdwyrmTrophyBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(3.0F, 6.0F)
        .sound(SoundType.METAL).lightLevel(state -> 10).noOcclusion());
  }

  private static Block register(BiConsumer<Block, Identifier> consumer, String name, Function<BlockBehaviour.Properties, Block> factory,
      BlockBehaviour.Properties properties) {

    ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Spookiness.id(name));
    Block block = factory.apply(properties.setId(key));
    consumer.accept(block, key.identifier());
    return block;
  }
}
