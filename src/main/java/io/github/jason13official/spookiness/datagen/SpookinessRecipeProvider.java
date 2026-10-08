package io.github.jason13official.spookiness.datagen;

import io.github.jason13official.spookiness.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;

public class SpookinessRecipeProvider extends RecipeProvider {

  protected SpookinessRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
    super(registries, output);
  }

  @Override
  protected void buildRecipes() {

    this.shaped(RecipeCategory.COMBAT, ModItems.PUMPKIN_MACE)
        .define('#', Items.PUMPKIN)
        .define('I', Items.STICK)
        .pattern("#")
        .pattern("I")
        .unlockedBy(getHasName(Items.PUMPKIN), this.has(Items.PUMPKIN))
        .save(this.output);

    this.shaped(RecipeCategory.MISC, ModItems.PIECE_OF_LAMENT_ONE)
        .define('G', Items.GOLD_BLOCK)
        .define('B', Items.BLAZE_ROD)
        .define('J', Items.JACK_O_LANTERN)
        .pattern("GBG")
        .pattern("BJB")
        .pattern("GBG")
        .unlockedBy(getHasName(ModItems.PUMPKIN_MACE), this.has(ModItems.PUMPKIN_MACE))
        .save(this.output);

    this.shaped(RecipeCategory.MISC, ModItems.PIECE_OF_LAMENT_TWO)
        .define('G', Items.GOLD_BLOCK)
        .define('M', Items.MAGMA_CREAM)
        .define('P', Items.CARVED_PUMPKIN)
        .define('E', Items.FERMENTED_SPIDER_EYE)
        .pattern("GMG")
        .pattern("EPE")
        .pattern("GMG")
        .unlockedBy(getHasName(ModItems.PUMPKIN_MACE), this.has(ModItems.PUMPKIN_MACE))
        .save(this.output);

    this.shapeless(RecipeCategory.MISC, ModItems.LAMENT_CONFIGURATION)
        .requires(ModItems.PIECE_OF_LAMENT_ONE)
        .requires(ModItems.PIECE_OF_LAMENT_TWO)
        .unlockedBy(getHasName(ModItems.PIECE_OF_LAMENT_ONE), this.has(ModItems.PIECE_OF_LAMENT_ONE))
        .save(this.output);
  }

  public static class Runner extends RecipeProvider.Runner {

    public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
      super(output, registries);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {

      return new SpookinessRecipeProvider(registries, output);
    }

    @Override
    public String getName() {

      return "Spookiness Recipes";
    }
  }
}
