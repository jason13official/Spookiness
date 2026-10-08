package io.github.jason13official.spookiness.world;

import io.github.jason13official.spookiness.registry.ModItems;
import io.github.jason13official.spookiness.util.PoisonClouds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class PumpkinSmashing {

  private static final float ROTTEN_CHANCE = 0.1F;
  private static final float CLOUD_RADIUS = 2.0F;
  private static final int CLOUD_DURATION = 100;
  private static final DustParticleOptions PULP = new DustParticleOptions(0xE38A1D, 1.5F);

  public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {

    if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START || !(event.getEntity() instanceof ServerPlayer player)
        || !(player.level() instanceof ServerLevel level) || player.getAbilities().instabuild || !isMace(player.getMainHandItem())) {
      return;
    }

    BlockPos pos = event.getPos();
    BlockState state = level.getBlockState(pos);
    if (!isSmashable(state) || !player.isWithinBlockInteractionRange(pos, 1.0) || !level.mayInteract(player, pos)) {
      return;
    }

    event.setCanceled(true);
    if (!player.gameMode.destroyBlock(pos)) {
      return;
    }

    Vec3 center = Vec3.atCenterOf(pos);
    level.sendParticles(PULP, center.x, center.y, center.z, 20, 0.3, 0.3, 0.3, 0.0);
    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), center.x, center.y, center.z, 16, 0.25, 0.25, 0.25, 0.15);

    if (level.getRandom().nextFloat() < ROTTEN_CHANCE) {
      PoisonClouds.spawn(level, null, Vec3.atBottomCenterOf(pos), CLOUD_RADIUS, CLOUD_DURATION);
      level.playSound(null, pos, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 1.0F, 0.6F);
    }
  }

  private static boolean isMace(ItemStack stack) {
    return stack.is(Items.MACE) || stack.is(ModItems.PUMPKIN_MACE);
  }

  private static boolean isSmashable(BlockState state) {
    return state.is(Blocks.PUMPKIN) || state.is(Blocks.CARVED_PUMPKIN) || state.is(Blocks.JACK_O_LANTERN);
  }
}
