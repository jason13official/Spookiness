package io.github.jason13official.spookiness.client.renderer.boss;

import io.github.jason13official.spookiness.client.renderer.BlockPartsRenderer;
import io.github.jason13official.spookiness.client.renderer.state.BlockPartsRenderState;
import io.github.jason13official.spookiness.entity.boss.Wickman;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class WickmanRenderer extends BlockPartsRenderer<Wickman> {

  private static final double SHOULDER_WIDTH = 0.62;
  private static final double SHOULDER_HEIGHT = 1.9;
  private static final float ARM_SPREAD = 12.0F;
  private static final float WALK_SWING = 50.0F;
  private static final float ATTACK_RAISE = 110.0F;

  public WickmanRenderer(Context context) {
    super(context, 0.8F);
  }

  @Override
  protected void collectParts(Wickman entity, BlockPartsRenderState state, float partialTicks) {

    boolean frost = entity.getVariant() == Wickman.Variant.FROST;
    float yRot = Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
    float headYRot = Mth.rotLerp(partialTicks, entity.yHeadRotO, entity.yHeadRot);
    float sway = Mth.sin((entity.tickCount + partialTicks) * 0.08F) * 4.0F;

    BlockState legs = frost ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.DARK_OAK_FENCE.defaultBlockState();
    BlockState body = frost ? Blocks.SNOW_BLOCK.defaultBlockState() : Blocks.HAY_BLOCK.defaultBlockState();
    BlockState head = frost ? Blocks.CARVED_PUMPKIN.defaultBlockState() : Blocks.JACK_O_LANTERN.defaultBlockState();
    BlockState arm = frost ? Blocks.PALE_OAK_FENCE.defaultBlockState() : Blocks.DARK_OAK_FENCE.defaultBlockState();

    float walk = Mth.cos(entity.walkAnimation.position(partialTicks) * 0.6662F) * entity.walkAnimation.speed(partialTicks) * WALK_SWING;
    float attack = entity.getAttackAnim(partialTicks) * ATTACK_RAISE;
    float radians = yRot * Mth.DEG_TO_RAD;
    double sideX = Mth.cos(radians) * SHOULDER_WIDTH;
    double sideZ = Mth.sin(radians) * SHOULDER_WIDTH;

    this.part(state, legs, 0.0, 0.0, 0.0, 1.0F, yRot, 0.0F);
    this.part(state, body, 0.0, 1.0, 0.0, 1.0F, yRot, sway);
    this.part(state, arm, sideX, SHOULDER_HEIGHT, sideZ, 1.0F, yRot, 180.0F + walk - attack, ARM_SPREAD);
    this.part(state, arm, -sideX, SHOULDER_HEIGHT, -sideZ, 1.0F, yRot, 180.0F - walk - attack, -ARM_SPREAD);
    if (entity.getPhase() != Wickman.Phase.HEADLESS) {
      this.part(state, head, 0.0, 2.0, 0.0, 1.2F, headYRot + 180.0F, 0.0F);
    }
  }
}
