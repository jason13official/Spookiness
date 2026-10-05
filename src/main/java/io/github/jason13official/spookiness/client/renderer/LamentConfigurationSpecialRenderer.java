package io.github.jason13official.spookiness.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.jason13official.spookiness.client.model.LamentConfigurationModel;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import org.joml.Vector3fc;

public class LamentConfigurationSpecialRenderer implements NoDataSpecialModelRenderer {

  private final LamentConfigurationModel model;

  public LamentConfigurationSpecialRenderer(LamentConfigurationModel model) {
    this.model = model;
  }

  @Override
  public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
    submitNodeCollector.submitModelPart(this.model.root(), poseStack, this.model.renderType(LamentConfigurationModel.TEXTURE), lightCoords, overlayCoords, null, false, hasFoil, -1, null,
        outlineColor);
  }

  @Override
  public void getExtents(Consumer<Vector3fc> output) {
    PoseStack poseStack = new PoseStack();
    this.model.root().getExtentsForGui(poseStack, output);
  }

  public record Unbaked(LamentConfigurationModel.Part part) implements NoDataSpecialModelRenderer.Unbaked {

    public static final MapCodec<LamentConfigurationSpecialRenderer.Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        LamentConfigurationModel.Part.CODEC.optionalFieldOf("part", LamentConfigurationModel.Part.ALL).forGetter(Unbaked::part)
    ).apply(instance, Unbaked::new));

    @Override
    public MapCodec<LamentConfigurationSpecialRenderer.Unbaked> type() {

      return MAP_CODEC;
    }

    @Override
    public LamentConfigurationSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {

      LamentConfigurationModel model = new LamentConfigurationModel(context.entityModelSet().bakeLayer(LamentConfigurationModel.LAYER_LOCATION));
      model.showOnly(this.part);
      return new LamentConfigurationSpecialRenderer(model);
    }
  }
}
