package io.github.jason13official.spookiness.mixin.client;

import io.github.jason13official.spookiness.Spookiness;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

  @Inject(at = @At("TAIL"), method = "<init>")
  private void spookiness$constructor(GameConfig gameConfig, CallbackInfo ci) {

    Spookiness.LOG.info("Minecraft.class constructor called.");
  }
}
