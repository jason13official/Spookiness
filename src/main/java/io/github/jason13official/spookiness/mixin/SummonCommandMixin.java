package io.github.jason13official.spookiness.mixin;

import io.github.jason13official.spookiness.companion.Allies;
import io.github.jason13official.spookiness.entity.SpectralJackOMimic;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.commands.SummonCommand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SummonCommand.class)
public class SummonCommandMixin {

  @Inject(method = "createEntity", at = @At("RETURN"))
  private static void spookiness$createEntity(CommandSourceStack source, Holder.Reference<EntityType<?>> type, Vec3 pos, CompoundTag nbt, boolean finalize,
      CallbackInfoReturnable<Entity> cir) {

    if (cir.getReturnValue() instanceof SpectralJackOMimic mimic && mimic.getOwnerUUID() == null && source.getEntity() instanceof Player player) {
      Allies.ally(mimic, player);
    }
  }
}
