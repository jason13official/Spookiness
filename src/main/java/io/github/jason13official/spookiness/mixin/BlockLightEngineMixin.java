package io.github.jason13official.spookiness.mixin;

import io.github.jason13official.spookiness.lighting.LivingLights;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.BlockLightEngine;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockLightEngine.class)
public class BlockLightEngineMixin {

  @Inject(at = @At("RETURN"), method = "getEmission", cancellable = true)
  private void spookiness$getEmission(long blockNode, BlockState state, CallbackInfoReturnable<Integer> cir) {

    BlockLightEngine self = (BlockLightEngine) (Object) this;

    int emission = LivingLights.getEmission(self.chunkSource.getLevel(), blockNode);

    if (emission > cir.getReturnValueI()) {
      cir.setReturnValue(emission);
    }
  }
}
