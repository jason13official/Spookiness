package io.github.jason13official.spookiness.client.renderer.state;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class FloatingCandlesRenderState extends EntityRenderState {

  public final BlockModelRenderState candles = new BlockModelRenderState();
  public float deathTime;
}
