package io.github.jason13official.spookiness.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;

public enum MaceStage implements StringRepresentable {
  PUMPKIN(0, "pumpkin", 0, 0),
  CARVED(1, "carved", 5, 0),
  LANTERN(2, "lantern", 15, 10),
  BLAZING(3, "blazing", 25, 15),
  THORNED(4, "thorned", 50, 15);

  public static final Codec<MaceStage> CODEC = StringRepresentable.fromEnum(MaceStage::values);
  private static final IntFunction<MaceStage> BY_ID = ByIdMap.continuous(MaceStage::getId, values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
  public static final StreamCodec<ByteBuf, MaceStage> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, MaceStage::getId);

  private final int id;
  private final String name;
  private final int kills;
  private final int light;

  MaceStage(int id, String name, int kills, int light) {
    this.id = id;
    this.name = name;
    this.kills = kills;
    this.light = light;
  }

  public static MaceStage byId(int id) {
    return BY_ID.apply(id);
  }

  public static MaceStage forKills(int kills) {
    MaceStage stage = PUMPKIN;
    for (MaceStage candidate : values()) {
      if (kills >= candidate.kills) {
        stage = candidate;
      }
    }
    return stage;
  }

  public int getId() {
    return this.id;
  }

  public MaceStage next() {
    return byId(this.id + 1);
  }

  public boolean isFinal() {
    return this == THORNED;
  }

  public boolean ignites() {
    return this.id >= BLAZING.id;
  }

  public int kills() {
    return this.kills;
  }

  public int light() {
    return this.light;
  }

  @Override
  public String getSerializedName() {
    return this.name;
  }
}
