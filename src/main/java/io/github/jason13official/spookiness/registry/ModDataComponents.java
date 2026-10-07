package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.item.MaceStage;
import java.util.function.BiConsumer;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

public class ModDataComponents {

  public static DataComponentType<Integer> PUMPKIN_KILLS;
  public static DataComponentType<GlobalPos> LAMENT_ORIGIN;
  public static DataComponentType<Integer> MACE_HARVEST;
  public static DataComponentType<MaceStage> MACE_STAGE;

  public static void register(BiConsumer<DataComponentType<?>, Identifier> consumer) {

    PUMPKIN_KILLS = DataComponentType.<Integer>builder().persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();
    consumer.accept(PUMPKIN_KILLS, Spookiness.id("pumpkin_kills"));

    LAMENT_ORIGIN = DataComponentType.<GlobalPos>builder().persistent(GlobalPos.CODEC).networkSynchronized(GlobalPos.STREAM_CODEC).build();
    consumer.accept(LAMENT_ORIGIN, Spookiness.id("lament_origin"));

    MACE_HARVEST = DataComponentType.<Integer>builder().persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT).build();
    consumer.accept(MACE_HARVEST, Spookiness.id("mace_harvest"));

    MACE_STAGE = DataComponentType.<MaceStage>builder().persistent(MaceStage.CODEC).networkSynchronized(MaceStage.STREAM_CODEC).build();
    consumer.accept(MACE_STAGE, Spookiness.id("mace_stage"));
  }
}
