package io.github.jason13official.spookiness.client.renderer.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.core.Direction;

public class SoullessJackOMimicRenderState extends BlockEntityRenderState {

  public final ItemStackRenderState item = new ItemStackRenderState();
  public Direction facing = Direction.NORTH;
  public float openness;
}
