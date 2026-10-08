package io.github.jason13official.spookiness.block.entity;

import io.github.jason13official.spookiness.registry.ModBlockEntities;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class JackOMimicStemBlockEntity extends BlockEntity {

  private @Nullable UUID owner;

  public JackOMimicStemBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.JACK_O_MIMIC_STEM, pos, state);
  }

  public @Nullable UUID getOwner() {
    return this.owner;
  }

  public void setOwner(UUID owner) {
    this.owner = owner;
    this.setChanged();
  }

  @Override
  protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    this.owner = input.read("owner", UUIDUtil.CODEC).orElse(null);
  }

  @Override
  protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    output.storeNullable("owner", UUIDUtil.CODEC, this.owner);
  }
}
