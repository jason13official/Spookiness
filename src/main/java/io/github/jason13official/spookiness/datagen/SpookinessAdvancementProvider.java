package io.github.jason13official.spookiness.datagen;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.advancement.SpookyTrigger;
import io.github.jason13official.spookiness.item.MaceStage;
import io.github.jason13official.spookiness.registry.ModEntities;
import io.github.jason13official.spookiness.registry.ModItems;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.KilledTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

public class SpookinessAdvancementProvider {

  public static final Map<String, String[]> TEXT = Map.ofEntries(
      Map.entry("root", new String[] {"Spookiness", "Something is wrong with the pumpkins this year"}),
      Map.entry("treat", new String[] {"Treat", "Fashion a Pumpkin Mace"}),
      Map.entry("hallowed_be", new String[] {"Hallowed Be", "Give a little of yourself to Hallow a pumpkin-headed mob"}),
      Map.entry("too_bright", new String[] {"Too Bright", "Kindle a lantern-headed mob into a Wickman"}),
      Map.entry("snuffed", new String[] {"Snuffed", "Put out the Wickman for good"}),
      Map.entry("conga", new String[] {"Conga!", "Lead a line of twelve Floating Candles"}),
      Map.entry("thirteen", new String[] {"Thirteen Is Unlucky", "Claim one candle too many"}),
      Map.entry("light_following", new String[] {"Light Following", "Claim a Floating Lantern"}),
      Map.entry("carved_out", new String[] {"Carved Out", "Your Pumpkin Mace grows a face"}),
      Map.entry("thorned", new String[] {"Thorned", "Harvest fifty pumpkin souls with one Pumpkin Mace"}),
      Map.entry("midnight_bloom", new String[] {"Midnight Bloom", "Witness the Hallowed Mother rise from the field"}),
      Map.entry("mother_knows_best", new String[] {"Mother Knows Best", "Defeat the Hallowed Mother"}),
      Map.entry("puzzle_box", new String[] {"Puzzle Box", "Join the two Pieces of Lament"}),
      Map.entry("under_the_fire", new String[] {"Under the Fire", "Complete the Lament ritual"}),
      Map.entry("long_harvest", new String[] {"The Long Harvest", "Burn the Gourdwyrm down to its root"})
  );

  public static AdvancementProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
    return new AdvancementProvider(output, registries, List.of(new Advancements()));
  }

  private static class Advancements implements AdvancementSubProvider {

    private Consumer<AdvancementHolder> output;

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> output) {

      this.output = output;
      HolderGetter<EntityType<?>> entities = registries.lookupOrThrow(Registries.ENTITY_TYPE);

      AdvancementHolder root = this.make("root", null, Items.CARVED_PUMPKIN, AdvancementType.TASK, false, InventoryChangeTrigger.TriggerInstance.hasItems(Items.CARVED_PUMPKIN), null);
      AdvancementHolder treat = this.make("treat", root, ModItems.PUMPKIN_MACE, AdvancementType.TASK, false, InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.PUMPKIN_MACE), null);
      AdvancementHolder hallowed = this.make("hallowed_be", treat, Items.SOUL_LANTERN, AdvancementType.TASK, false, SpookyTrigger.TriggerInstance.of(SpookyTrigger.HALLOW), null);
      AdvancementHolder kindled = this.make("too_bright", treat, Items.JACK_O_LANTERN, AdvancementType.TASK, false, SpookyTrigger.TriggerInstance.of(SpookyTrigger.KINDLE), null);
      this.make("snuffed", kindled, ModItems.PIECE_OF_LAMENT_ONE, AdvancementType.GOAL, false, killed(entities, ModEntities.WICKMAN), null);
      AdvancementHolder conga = this.make("conga", root, Items.CANDLE, AdvancementType.TASK, false, SpookyTrigger.TriggerInstance.of(SpookyTrigger.FULL_CONGA), null);
      this.make("thirteen", conga, Items.SOUL_CAMPFIRE, AdvancementType.TASK, true, SpookyTrigger.TriggerInstance.of(SpookyTrigger.CANDLE_OVERFLOW), null);
      this.make("light_following", root, Items.SOUL_LANTERN, AdvancementType.TASK, false, SpookyTrigger.TriggerInstance.of(SpookyTrigger.CLAIM_LANTERN), null);
      AdvancementHolder carved = this.make("carved_out", treat, Items.CARVED_PUMPKIN, AdvancementType.TASK, false,
          SpookyTrigger.TriggerInstance.of(SpookyTrigger.MACE_EVOLVED + MaceStage.CARVED.getSerializedName()), null);
      this.make("thorned", carved, Items.SWEET_BERRIES, AdvancementType.CHALLENGE, false,
          SpookyTrigger.TriggerInstance.of(SpookyTrigger.MACE_EVOLVED + MaceStage.THORNED.getSerializedName()), AdvancementRewards.Builder.experience(100));
      AdvancementHolder bloom = this.make("midnight_bloom", hallowed, Items.PUMPKIN, AdvancementType.TASK, false, SpookyTrigger.TriggerInstance.of(SpookyTrigger.MOTHER_RISES), null);
      AdvancementHolder mother = this.make("mother_knows_best", bloom, ModItems.PIECE_OF_LAMENT_TWO, AdvancementType.GOAL, false, killed(entities, ModEntities.HALLOWED_MOTHER), null);
      AdvancementHolder puzzle = this.make("puzzle_box", mother, ModItems.LAMENT_CONFIGURATION, AdvancementType.TASK, false,
          InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.LAMENT_CONFIGURATION), null);
      AdvancementHolder under = this.make("under_the_fire", puzzle, Items.SOUL_SOIL, AdvancementType.TASK, false, SpookyTrigger.TriggerInstance.of(SpookyTrigger.LAMENT_RITUAL), null);
      this.make("long_harvest", under, ModItems.HARVEST_CROWN, AdvancementType.CHALLENGE, false, killed(entities, ModEntities.GOURDWYRM), AdvancementRewards.Builder.experience(500));
    }

    private static Criterion<KilledTrigger.TriggerInstance> killed(HolderGetter<EntityType<?>> entities, EntityType<?> type) {
      return KilledTrigger.TriggerInstance.playerKilledEntity(EntityPredicate.Builder.entity().of(entities, type));
    }

    private AdvancementHolder make(String name, @Nullable AdvancementHolder parent, ItemLike icon, AdvancementType type, boolean hidden, Criterion<?> criterion,
        AdvancementRewards.@Nullable Builder rewards) {

      Advancement.Builder builder = Advancement.Builder.advancement();
      if (parent != null) {
        builder.parent(parent);
      }
      builder.display(icon, Component.translatable(key(name, "title")), Component.translatable(key(name, "description")),
          parent == null ? Identifier.withDefaultNamespace("gui/advancements/backgrounds/husbandry") : null, type, true, true, hidden);
      builder.addCriterion(name, criterion);
      if (rewards != null) {
        builder.rewards(rewards);
      }
      return builder.save(this.output, Spookiness.id(name).toString());
    }
  }

  public static String key(String name, String part) {
    return "advancements.spookiness." + name + "." + part;
  }
}
