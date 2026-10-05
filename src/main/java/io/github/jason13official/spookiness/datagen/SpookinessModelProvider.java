package io.github.jason13official.spookiness.datagen;

import com.mojang.math.Transformation;
import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.client.model.LamentConfigurationModel;
import io.github.jason13official.spookiness.client.renderer.LamentConfigurationSpecialRenderer;
import io.github.jason13official.spookiness.registry.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplate;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import org.joml.Vector3f;

public class SpookinessModelProvider extends ModelProvider {

  private static final Transformation LAMENT_TRANSFORMATION = new Transformation(new Vector3f(0.5F, 1.875F, 0.5F), null, new Vector3f(1.0F, -1.0F, -1.0F), null);

  private static final ExtendedModelTemplate LAMENT_BASE = ExtendedModelTemplateBuilder.builder()
      .requiredTextureSlot(TextureSlot.PARTICLE)
      .guiLight(UnbakedModel.GuiLight.SIDE)
      .transform(ItemDisplayContext.GUI, transform -> transform.rotation(30.0F, 225.0F, 0.0F))
      .transform(ItemDisplayContext.GROUND, transform -> transform.translation(0.0F, 3.0F, 0.0F).scale(0.5F))
      .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, transform -> transform.rotation(75.0F, 45.0F, 0.0F).translation(0.0F, 2.5F, 0.0F).scale(0.6F))
      .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, transform -> transform.rotation(75.0F, 45.0F, 0.0F).translation(0.0F, 2.5F, 0.0F).scale(0.6F))
      .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND, transform -> transform.rotation(0.0F, 45.0F, 0.0F).scale(0.8F))
      .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND, transform -> transform.rotation(0.0F, 225.0F, 0.0F).scale(0.8F))
      .build();

  public SpookinessModelProvider(PackOutput output) {
    super(output, Spookiness.MOD_ID);
  }

  @Override
  protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {

    itemModels.itemModelOutput.accept(ModItems.PUMPKIN_MACE, ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(ModItems.PUMPKIN_MACE)));

    Identifier lamentBase = LAMENT_BASE.create(ModItems.LAMENT_CONFIGURATION, TextureMapping.particle(Blocks.GOLD_BLOCK), itemModels.modelOutput);
    lament(itemModels, ModItems.LAMENT_CONFIGURATION, lamentBase, LamentConfigurationModel.Part.ALL);
    lament(itemModels, ModItems.PIECE_OF_LAMENT_ONE, lamentBase, LamentConfigurationModel.Part.ONE);
    lament(itemModels, ModItems.PIECE_OF_LAMENT_TWO, lamentBase, LamentConfigurationModel.Part.TWO);
  }

  private static void lament(ItemModelGenerators itemModels, Item item, Identifier base, LamentConfigurationModel.Part part) {

    itemModels.itemModelOutput.accept(item, ItemModelUtils.specialModel(base, LAMENT_TRANSFORMATION, new LamentConfigurationSpecialRenderer.Unbaked(part)));
  }
}
