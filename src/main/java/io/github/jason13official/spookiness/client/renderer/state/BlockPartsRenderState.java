package io.github.jason13official.spookiness.client.renderer.state;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class BlockPartsRenderState extends EntityRenderState {

  public final List<Part> parts = new ArrayList<>();
  public int used;
  public boolean hurt;

  public Part next() {
    if (this.used == this.parts.size()) {
      this.parts.add(new Part());
    }
    return this.parts.get(this.used++);
  }

  public static class Part {

    public final BlockModelRenderState model = new BlockModelRenderState();
    public float x;
    public float y;
    public float z;
    public float scale;
    public float yRot;
    public float xRot;
  }
}
