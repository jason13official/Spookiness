package io.github.jason13official.spookiness.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jason13official.spookiness.registry.ModTriggers;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class SpookyTrigger extends SimpleCriterionTrigger<SpookyTrigger.TriggerInstance> {

  public static final String HALLOW = "hallow";
  public static final String KINDLE = "kindle";
  public static final String FULL_CONGA = "full_conga";
  public static final String CANDLE_OVERFLOW = "candle_overflow";
  public static final String MOTHER_RISES = "mother_rises";
  public static final String LAMENT_RITUAL = "lament_ritual";
  public static final String CLAIM_LANTERN = "claim_lantern";
  public static final String MACE_EVOLVED = "mace_";

  @Override
  public Codec<TriggerInstance> codec() {
    return TriggerInstance.CODEC;
  }

  public void trigger(ServerPlayer player, String event) {
    this.trigger(player, instance -> instance.event().equals(event));
  }

  public static void award(Player player, String event) {
    if (player instanceof ServerPlayer serverPlayer) {
      ModTriggers.SPOOKY.trigger(serverPlayer, event);
    }
  }

  public record TriggerInstance(Optional<ContextAwarePredicate> player, String event) implements SimpleCriterionTrigger.SimpleInstance {

    public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(i -> i.group(
        EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
        Codec.STRING.fieldOf("event").forGetter(TriggerInstance::event)
    ).apply(i, TriggerInstance::new));

    public static Criterion<TriggerInstance> of(String event) {
      return ModTriggers.SPOOKY.createCriterion(new TriggerInstance(Optional.empty(), event));
    }
  }
}
