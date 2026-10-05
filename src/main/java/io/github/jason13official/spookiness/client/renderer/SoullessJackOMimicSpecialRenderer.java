package io.github.jason13official.spookiness.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import io.github.jason13official.spookiness.client.model.JackOMimicModel;
import io.github.jason13official.spookiness.client.model.SoullessJackOMimicModel;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import org.joml.Vector3fc;

public class SoullessJackOMimicSpecialRenderer implements NoDataSpecialModelRenderer {

  private final SoullessJackOMimicModel model;

  public SoullessJackOMimicSpecialRenderer(SoullessJackOMimicModel model) {
    this.model = model;
  }

  @Override
  public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
    submitNodeCollector.submitModelPart(this.model.root(), poseStack, this.model.renderType(SoullessJackOMimicModel.TEXTURE), lightCoords, overlayCoords, null,
        false, hasFoil, -1, null, outlineColor);
  }

  @Override
  public void getExtents(Consumer<Vector3fc> output) {
    this.model.root().getExtentsForGui(new PoseStack(), output);
  }

  public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {

    public static final MapCodec<SoullessJackOMimicSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(new SoullessJackOMimicSpecialRenderer.Unbaked());

    @Override
    public MapCodec<SoullessJackOMimicSpecialRenderer.Unbaked> type() {
      return MAP_CODEC;
    }

    @Override
    public SoullessJackOMimicSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
      return new SoullessJackOMimicSpecialRenderer(new SoullessJackOMimicModel(context.entityModelSet().bakeLayer(JackOMimicModel.LAYER_LOCATION)));
    }
  }
}
