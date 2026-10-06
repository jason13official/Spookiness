package io.github.jason13official.spookiness.mixin;

import io.github.jason13official.spookiness.world.SpookySpawns;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.sheep.Sheep;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AgeableMob.class)
public class AgeableMobMixin {

  @Inject(method = "ageBoundaryReached", at = @At("HEAD"))
  private void spookiness$ageBoundaryReached(CallbackInfo ci) {

    if ((Object) this instanceof Sheep sheep) {
      SpookySpawns.onSheepGrownUp(sheep);
    }
  }
}
