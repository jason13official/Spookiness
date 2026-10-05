package io.github.jason13official.spookiness.datagen;

import com.mojang.math.Transformation;
import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.client.model.LamentConfigurationModel;
import io.github.jason13official.spookiness.client.renderer.LamentConfigurationSpecialRenderer;
import io.github.jason13official.spookiness.client.renderer.SoullessJackOMimicSpecialRenderer;
import io.github.jason13official.spookiness.registry.ModBlocks;
import io.github.jason13official.spookiness.registry.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.sprite.Material;
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

  private static final Transformation SOULLESS_TRANSFORMATION = new Transformation(new Vector3f(0.5F, 1.5F, 0.5F), null, new Vector3f(1.0F, -1.0F, -1.0F), null);

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

    Identifier spawnEgg = ModelTemplates.TWO_LAYERED_ITEM.create(Spookiness.id("item/spawn_egg"),
        TextureMapping.layered(new Material(Spookiness.id("item/spawn_egg")), new Material(Spookiness.id("item/spawn_egg_overlay"))), itemModels.modelOutput);
    spawnEgg(itemModels, ModItems.JACK_O_MIMIC_SPAWN_EGG, spawnEgg, 0xE38A1D, 0x3B2508);
    spawnEgg(itemModels, ModItems.FLOATING_CANDLES_SPAWN_EGG, spawnEgg, 0xEFE3C2, 0xF5A623);
    spawnEgg(itemModels, ModItems.FLOATING_BOOK_SPAWN_EGG, spawnEgg, 0x6E4325, 0xE5D9AE);
    spawnEgg(itemModels, ModItems.FLOATING_SWORD_SPAWN_EGG, spawnEgg, 0xB8BEC8, 0x5E3FAE);
    spawnEgg(itemModels, ModItems.SPECTRAL_JACK_O_MIMIC_SPAWN_EGG, spawnEgg, 0x5FE3E0, 0x1A4E7A);
    spawnEgg(itemModels, ModItems.WICKMAN_SPAWN_EGG, spawnEgg, 0xC9A65A, 0xFF7A1A);
    spawnEgg(itemModels, ModItems.HALLOWED_MOTHER_SPAWN_EGG, spawnEgg, 0x8A5A2B, 0x4E7A2E);
    spawnEgg(itemModels, ModItems.GOURDWYRM_SPAWN_EGG, spawnEgg, 0xD9731E, 0x3B5A1E);

    blockModels.createParticleOnlyBlock(ModBlocks.SOULLESS_JACK_O_MIMIC, Blocks.CARVED_PUMPKIN);
    itemModels.itemModelOutput.accept(ModItems.SOULLESS_JACK_O_MIMIC, ItemModelUtils.specialModel(ModelLocationUtils.getModelLocation(ModItems.SOULLESS_JACK_O_MIMIC),
        SOULLESS_TRANSFORMATION, new SoullessJackOMimicSpecialRenderer.Unbaked()));
    LAMENT_BASE.create(ModItems.SOULLESS_JACK_O_MIMIC, TextureMapping.particle(Blocks.CARVED_PUMPKIN), itemModels.modelOutput);

    itemModels.itemModelOutput.accept(ModItems.PUMPKIN_MACE, ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(ModItems.PUMPKIN_MACE)));

    Identifier lamentBase = LAMENT_BASE.create(ModItems.LAMENT_CONFIGURATION, TextureMapping.particle(Blocks.GOLD_BLOCK), itemModels.modelOutput);
    lament(itemModels, ModItems.LAMENT_CONFIGURATION, lamentBase, LamentConfigurationModel.Part.ALL);
    lament(itemModels, ModItems.PIECE_OF_LAMENT_ONE, lamentBase, LamentConfigurationModel.Part.ONE);
    lament(itemModels, ModItems.PIECE_OF_LAMENT_TWO, lamentBase, LamentConfigurationModel.Part.TWO);
  }

  private static void spawnEgg(ItemModelGenerators itemModels, Item egg, Identifier model, int baseColor, int spotColor) {

    itemModels.itemModelOutput.accept(egg, ItemModelUtils.tintedModel(model, ItemModelUtils.constantTint(baseColor), ItemModelUtils.constantTint(spotColor)));
  }

  private static void lament(ItemModelGenerators itemModels, Item item, Identifier base, LamentConfigurationModel.Part part) {

    itemModels.itemModelOutput.accept(item, ItemModelUtils.specialModel(base, LAMENT_TRANSFORMATION, new LamentConfigurationSpecialRenderer.Unbaked(part)));
  }
}
