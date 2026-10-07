package io.github.jason13official.spookiness.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum MaceStage implements StringRepresentable {
  PUMPKIN("pumpkin", 0, 0),
  CARVED("carved", 5, 0),
  LANTERN("lantern", 15, 10),
  BLAZING("blazing", 25, 15),
  THORNED("thorned", 50, 15);

  public static final Codec<MaceStage> CODEC = StringRepresentable.fromEnum(MaceStage::values);
  public static final StreamCodec<ByteBuf, MaceStage> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(MaceStage::byOrdinal, MaceStage::ordinal);

  private final String name;
  private final int kills;
  private final int light;

  MaceStage(String name, int kills, int light) {
    this.name = name;
    this.kills = kills;
    this.light = light;
  }

  public static MaceStage byOrdinal(int ordinal) {
    MaceStage[] values = values();
    return values[Math.clamp(ordinal, 0, values.length - 1)];
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

  public MaceStage next() {
    return byOrdinal(this.ordinal() + 1);
  }

  public boolean isFinal() {
    return this == THORNED;
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
