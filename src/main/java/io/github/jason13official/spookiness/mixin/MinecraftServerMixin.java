package io.github.jason13official.spookiness.mixin;

import io.github.jason13official.spookiness.Spookiness;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

  @Inject(at = @At("TAIL"), method = "<clinit>")
  private static void spookiness$clinit(CallbackInfo ci) {

    Spookiness.LOG.info("MinecraftServer.class loaded.");
  }
}
