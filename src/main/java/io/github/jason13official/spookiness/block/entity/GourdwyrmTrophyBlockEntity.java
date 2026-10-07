package io.github.jason13official.spookiness.block.entity;

import io.github.jason13official.spookiness.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GourdwyrmTrophyBlockEntity extends BlockEntity {

  public GourdwyrmTrophyBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.GOURDWYRM_TROPHY, pos, state);
  }
}
