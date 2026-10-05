package io.github.jason13official.spookiness.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import io.github.jason13official.spookiness.client.model.LamentConfigurationModel;
import java.util.function.Consumer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.projectile.TridentModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.TridentSpecialRenderer;
import net.minecraft.client.renderer.special.TridentSpecialRenderer.Unbaked;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class LamentConfigurationSpecialRenderer implements NoDataSpecialModelRenderer {

  // TODO research?
  public static final Transformation DEFAULT_TRANSFORMATION = new Transformation(null, null, new Vector3f(1.0F, -1.0F, -1.0F), null);

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

  public static record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {

    public static final MapCodec<LamentConfigurationSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(new LamentConfigurationSpecialRenderer.Unbaked());

    public Unbaked() {}

    public MapCodec<LamentConfigurationSpecialRenderer.Unbaked> type() {

      return MAP_CODEC;
    }

    public LamentConfigurationSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {

      return new LamentConfigurationSpecialRenderer(new LamentConfigurationModel(context.entityModelSet().bakeLayer(LamentConfigurationModel.LAYER_LOCATION)));
    }
  }
}
