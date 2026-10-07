package io.github.jason13official.spookiness.client.renderer.state;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class GourdwyrmTrophyRenderState extends BlockEntityRenderState {

  public final List<BlockPartsRenderState.Part> parts = new ArrayList<>();
  public int used;

  public BlockPartsRenderState.Part next() {
    if (this.used == this.parts.size()) {
      this.parts.add(new BlockPartsRenderState.Part());
    }
    return this.parts.get(this.used++);
  }
}
