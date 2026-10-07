package io.github.jason13official.spookiness.entity.boss.mother;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

final class BroodTracker {

  private static final int RETALIATE_TICKS = 100;

  private final HallowedMother mother;
  private final Set<UUID> brood = new HashSet<>();

  BroodTracker(HallowedMother mother) {
    this.mother = mother;
  }

  boolean contains(Entity entity) {
    return this.brood.contains(entity.getUUID());
  }

  void add(Mob mob) {
    this.brood.add(mob.getUUID());
  }

  List<Mob> living(ServerLevel level) {

    List<Mob> living = new ArrayList<>();
    this.brood.removeIf(id -> {
      if (level.getEntity(id) instanceof Mob mob && mob.isAlive()) {
        living.add(mob);
        return false;
      }
      return true;
    });
    return living;
  }

  @Nullable LivingEntity target() {

    LivingEntity attacker = this.mother.getLastHurtByMob();
    if (attacker != null && attacker.isAlive() && !this.contains(attacker) && this.mother.tickCount - this.mother.getLastHurtByMobTimestamp() < RETALIATE_TICKS) {
      return attacker;
    }
    return this.mother.getTarget();
  }

  void command(ServerLevel level) {

    LivingEntity target = this.target();
    if (target == null) {
      return;
    }
    for (Mob mob : this.living(level)) {
      if (mob.getTarget() != target) {
        mob.setTarget(target);
      }
    }
  }

  void emitParticles(ServerLevel level) {

    for (Mob mob : this.living(level)) {
      level.sendParticles(this.mother.getRandom().nextBoolean() ? ParticleTypes.SMALL_FLAME : ParticleTypes.SMOKE, mob.getX(), mob.getY() + mob.getBbHeight(),
          mob.getZ(), 1, 0.2, 0.1, 0.2, 0.01);
    }
  }

  void save(ValueOutput output) {
    output.store("brood", UUIDUtil.CODEC.listOf(), List.copyOf(this.brood));
  }

  void load(ValueInput input) {
    this.brood.clear();
    this.brood.addAll(input.read("brood", UUIDUtil.CODEC.listOf()).orElse(List.of()));
  }
}
