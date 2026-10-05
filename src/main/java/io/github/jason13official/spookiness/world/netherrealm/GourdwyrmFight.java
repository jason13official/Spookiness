package io.github.jason13official.spookiness.world.netherrealm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jason13official.spookiness.Spookiness;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public class GourdwyrmFight extends SavedData {

  public static final Codec<GourdwyrmFight> CODEC = RecordCodecBuilder.create(instance -> instance.group(
      BlockPos.CODEC.listOf().optionalFieldOf("defeated", List.of()).forGetter(fight -> List.copyOf(fight.defeated))
  ).apply(instance, GourdwyrmFight::new));

  public static final SavedDataType<GourdwyrmFight> TYPE = new SavedDataType<>(Spookiness.id("gourdwyrm_fights"), GourdwyrmFight::new, CODEC);

  private final List<BlockPos> defeated;

  public GourdwyrmFight() {
    this(List.of());
  }

  private GourdwyrmFight(List<BlockPos> defeated) {
    this.defeated = new ArrayList<>(defeated);
  }

  public static GourdwyrmFight get(ServerLevel level) {
    return level.getDataStorage().computeIfAbsent(TYPE);
  }

  public boolean hasDefeated(BlockPos center) {
    return this.defeated.contains(center);
  }

  public void markDefeated(BlockPos center) {
    if (!this.defeated.contains(center)) {
      this.defeated.add(center.immutable());
      this.setDirty();
    }
  }
}
