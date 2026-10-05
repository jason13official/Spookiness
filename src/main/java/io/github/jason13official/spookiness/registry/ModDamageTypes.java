package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.Spookiness;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageTypes {

  public static final ResourceKey<DamageType> HALLOWING = ResourceKey.create(Registries.DAMAGE_TYPE, Spookiness.id("hallowing"));

  public static void bootstrap(BootstrapContext<DamageType> context) {

    context.register(HALLOWING, new DamageType("spookiness.hallowing", 0.0F));
  }
}
