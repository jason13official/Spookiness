package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.block.GourdwyrmTrophyBlock;
import io.github.jason13official.spookiness.block.JackOMimicStemBlock;
import io.github.jason13official.spookiness.block.LurkingPumpkinBlock;
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
  public static Block LURKING_CARVED_PUMPKIN;
  public static Block LURKING_JACK_O_LANTERN;
  public static Block JACK_O_MIMIC_STEM;

  public static void register(BiConsumer<Block, Identifier> consumer) {

    SOULLESS_JACK_O_MIMIC = register(consumer, "soulless_jack_o_mimic", SoullessJackOMimicBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE)
        .strength(1.5F).sound(SoundType.WOOD).lightLevel(state -> state.getValue(SoullessJackOMimicBlock.LIT) ? 15 : 0).noOcclusion().pushReaction(PushReaction.BLOCK));

    GOURDWYRM_TROPHY = register(consumer, "gourdwyrm_trophy", GourdwyrmTrophyBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.GOLD).strength(3.0F, 6.0F)
        .sound(SoundType.METAL).lightLevel(state -> 10).noOcclusion());

    LURKING_CARVED_PUMPKIN = register(consumer, "lurking_carved_pumpkin", LurkingPumpkinBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE)
        .strength(1.0F).sound(SoundType.WOOD).pushReaction(PushReaction.BLOCK));

    LURKING_JACK_O_LANTERN = register(consumer, "lurking_jack_o_lantern", LurkingPumpkinBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE)
        .strength(1.0F).sound(SoundType.WOOD).lightLevel(state -> 15).pushReaction(PushReaction.BLOCK));

    JACK_O_MIMIC_STEM = register(consumer, "jack_o_mimic_stem", JackOMimicStemBlock::new, BlockBehaviour.Properties.of().mapColor(MapColor.PLANT)
        .noCollision().randomTicks().instabreak().sound(SoundType.HARD_CROP).pushReaction(PushReaction.DESTROY));
  }

  private static Block register(BiConsumer<Block, Identifier> consumer, String name, Function<BlockBehaviour.Properties, Block> factory,
      BlockBehaviour.Properties properties) {

    ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Spookiness.id(name));
    Block block = factory.apply(properties.setId(key));
    consumer.accept(block, key.identifier());
    return block;
  }
}
