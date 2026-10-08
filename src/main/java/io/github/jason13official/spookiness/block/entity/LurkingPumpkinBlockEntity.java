package io.github.jason13official.spookiness.block.entity;

import io.github.jason13official.spookiness.block.JackOMimicStemBlock;
import io.github.jason13official.spookiness.block.LurkingPumpkinBlock;
import io.github.jason13official.spookiness.companion.Hallowing;
import io.github.jason13official.spookiness.entity.JackOMimic;
import io.github.jason13official.spookiness.registry.ModBlockEntities;
import io.github.jason13official.spookiness.registry.ModEntities;
import io.github.jason13official.spookiness.registry.ModSounds;
import io.github.jason13official.spookiness.util.Spawning;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class LurkingPumpkinBlockEntity extends BlockEntity {

  private static final int CHECK_INTERVAL = 5;
  private static final double NOTICE_RANGE = 4.0;
  private static final int MIN_PATIENCE = 100;
  private static final int MAX_PATIENCE = 160;
  private static final int DORMANT_TICKS = 600;

  private int watched;
  private int patience;
  private @Nullable UUID owner;
  private int dormancy;

  public LurkingPumpkinBlockEntity(BlockPos pos, BlockState state) {
    super(ModBlockEntities.LURKING_PUMPKIN, pos, state);
  }

  public void setOwner(UUID owner) {
    this.owner = owner;
    this.setChanged();
  }

  public static void serverTick(Level level, BlockPos pos, BlockState state, LurkingPumpkinBlockEntity pumpkin) {

    if (level.getGameTime() % CHECK_INTERVAL != 0 || !(level instanceof ServerLevel serverLevel)) {
      return;
    }

    if (pumpkin.owner != null) {
      pumpkin.dormancy = Math.min(DORMANT_TICKS, pumpkin.dormancy + CHECK_INTERVAL);
      pumpkin.setChanged();
      if (pumpkin.dormancy >= DORMANT_TICKS) {
        pumpkin.awaken(serverLevel, null);
      }
      return;
    }

    Vec3 center = Vec3.atCenterOf(pos);
    Player player = level.getNearestPlayer(center.x, center.y, center.z, NOTICE_RANGE, EntitySelector.NO_CREATIVE_OR_SPECTATOR);
    if (player == null) {
      pumpkin.watched = 0;
      return;
    }

    if (pumpkin.watched == 0) {
      pumpkin.patience = MIN_PATIENCE + level.getRandom().nextInt(MAX_PATIENCE - MIN_PATIENCE + 1);
    }
    pumpkin.watched += CHECK_INTERVAL;
    if (pumpkin.watched >= pumpkin.patience) {
      pumpkin.awaken(serverLevel, player);
    }
  }

  public void awaken(ServerLevel level, @Nullable Player target) {

    UUID owner = this.owner;
    if (owner != null && !JackOMimicStemBlock.hasRoom(level, owner)) {
      return;
    }

    BlockPos pos = this.worldPosition;
    float yRot = this.getBlockState().getValue(LurkingPumpkinBlock.FACING).toYRot();
    JackOMimic mimic = Spawning.spawnFinalized(level, ModEntities.JACK_O_MIMIC, EntitySpawnReason.TRIGGERED, Vec3.atBottomCenterOf(pos), yRot, spawned -> {
      if (owner != null) {
        Hallowing.hallow(spawned, owner);
      } else {
        spawned.setTarget(target);
      }
    });
    if (mimic == null) {
      return;
    }

    level.removeBlock(pos, false);
    mimic.spawnSoulBurst(level);
    level.playSound(null, pos, ModSounds.JACK_O_MIMIC_AMBIENT, SoundSource.HOSTILE, 1.0F, 0.6F);
  }

  @Override
  protected void loadAdditional(ValueInput input) {
    super.loadAdditional(input);
    this.owner = input.read("owner", UUIDUtil.CODEC).orElse(null);
    this.dormancy = input.getIntOr("dormancy", 0);
  }

  @Override
  protected void saveAdditional(ValueOutput output) {
    super.saveAdditional(output);
    output.storeNullable("owner", UUIDUtil.CODEC, this.owner);
    if (this.owner != null) {
      output.putInt("dormancy", this.dormancy);
    }
  }
}
