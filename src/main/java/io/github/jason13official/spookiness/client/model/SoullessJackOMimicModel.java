package io.github.jason13official.spookiness.client.model;

import io.github.jason13official.spookiness.Spookiness;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class SoullessJackOMimicModel extends Model<Float> {

  public static final Identifier TEXTURE = Spookiness.id("textures/entity/jack_o_mimic/soulless_jack_o_mimic.png");

  private static final float OPEN_ANGLE = 55.0F * Mth.DEG_TO_RAD;

  private final ModelPart cranium;

  public SoullessJackOMimicModel(ModelPart root) {
    super(root, RenderTypes::entityCutout);
    this.cranium = root.getChild("head").getChild("cranium");
  }

  @Override
  public void setupAnim(Float openness) {
    super.setupAnim(openness);
    this.cranium.xRot = -openness * OPEN_ANGLE;
  }
}
