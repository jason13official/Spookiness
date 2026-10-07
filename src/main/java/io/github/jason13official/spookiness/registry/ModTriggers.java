package io.github.jason13official.spookiness.registry;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.advancement.SpookyTrigger;
import java.util.function.BiConsumer;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.resources.Identifier;

public class ModTriggers {

  public static SpookyTrigger SPOOKY;

  public static void register(BiConsumer<CriterionTrigger<?>, Identifier> consumer) {

    SPOOKY = new SpookyTrigger();
    consumer.accept(SPOOKY, Spookiness.id("spooky"));
  }
}
