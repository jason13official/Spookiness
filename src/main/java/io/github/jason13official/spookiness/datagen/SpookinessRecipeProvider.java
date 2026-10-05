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
        .define('#', Items.CARVED_PUMPKIN)
        .define('I', Items.STICK)
        .pattern(" # ")
        .pattern(" I ")
        .unlockedBy(getHasName(Items.CARVED_PUMPKIN), this.has(Items.CARVED_PUMPKIN))
        .save(this.output);

    this.shapeless(RecipeCategory.MISC, ModItems.PIECE_OF_LAMENT_ONE)
        .requires(Items.GOLD_INGOT, 2)
        .requires(Items.BLAZE_ROD)
        .unlockedBy(getHasName(Items.BLAZE_ROD), this.has(Items.BLAZE_ROD))
        .save(this.output);

    this.shapeless(RecipeCategory.MISC, ModItems.PIECE_OF_LAMENT_TWO)
        .requires(Items.GOLD_NUGGET, 4)
        .requires(Items.MAGMA_CREAM)
        .unlockedBy(getHasName(Items.MAGMA_CREAM), this.has(Items.MAGMA_CREAM))
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
