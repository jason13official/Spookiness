package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.item.MaceStage;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;
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

    PUMPKIN_KILLS = register(consumer, "pumpkin_kills", builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    LAMENT_ORIGIN = register(consumer, "lament_origin", builder -> builder.persistent(GlobalPos.CODEC).networkSynchronized(GlobalPos.STREAM_CODEC));
    MACE_HARVEST = register(consumer, "mace_harvest", builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT));
    MACE_STAGE = register(consumer, "mace_stage", builder -> builder.persistent(MaceStage.CODEC).networkSynchronized(MaceStage.STREAM_CODEC));
  }

  private static <T> DataComponentType<T> register(BiConsumer<DataComponentType<?>, Identifier> consumer, String name,
      UnaryOperator<DataComponentType.Builder<T>> builder) {

    DataComponentType<T> type = builder.apply(DataComponentType.builder()).build();
    consumer.accept(type, Spookiness.id(name));
    return type;
  }
}
