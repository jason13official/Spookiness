package io.github.jason13official.spookiness.client.model;

import com.mojang.serialization.Codec;
import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.client.anim.LamentConfigurationAnimations;
import io.github.jason13official.spookiness.client.renderer.state.LamentConfigurationRenderState;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

public class LamentConfigurationModel extends Model<LamentConfigurationRenderState> {

  public static final Identifier TEXTURE = Spookiness.id("textures/entity/lament/lament.png");

  private static final float SPIN_SPEED = 0.5F;

  public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Spookiness.id("lament_configuration"), "main");

  private final ModelPart box;
  private final ModelPart one;
  private final ModelPart center;
  private final ModelPart pieces;
  private final ModelPart two;
  private final ModelPart halfOne;
  private final ModelPart halfTwo;

  private final KeyframeAnimation spinAnimation;

  public LamentConfigurationModel(ModelPart root) {
    super(root, RenderTypes::entityCutout);
    this.box = root.getChild("box");
    this.one = this.box.getChild("one");
    this.center = this.one.getChild("center");
    this.pieces = this.one.getChild("pieces");
    this.two = this.box.getChild("two");
    this.halfOne = this.two.getChild("halfOne");
    this.halfTwo = this.two.getChild("halfTwo");

    this.spinAnimation = LamentConfigurationAnimations.SPIN_LOOP.bake(root);
  }

  public void showOnly(Part part) {
    this.one.visible = part != Part.TWO;
    this.two.visible = part != Part.ONE;
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition meshdefinition = new MeshDefinition();
    PartDefinition partdefinition = meshdefinition.getRoot();

    PartDefinition box = partdefinition.addOrReplaceChild("box", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

    PartDefinition one = box.addOrReplaceChild("one", CubeListBuilder.create(), PartPose.offset(0.0F, -1.7778F, 0.0F));

    PartDefinition center = one.addOrReplaceChild("center", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
        PartPose.offset(0.0F, -0.2222F, 0.0F));

    PartDefinition pieces = one.addOrReplaceChild("pieces", CubeListBuilder.create().texOffs(0, 64).addBox(-2.0F, -2.2222F, -4.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(48, 64)
        .addBox(0.0F, -2.2222F, -4.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(144, 96).addBox(2.0F, -2.2222F, -2.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(144, 136)
        .addBox(2.0F, -2.2222F, 0.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(96, 64).addBox(1.0F, -2.2222F, 2.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 112)
        .addBox(-1.0F, -2.2222F, 2.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 160).addBox(-4.0F, -2.2222F, 1.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(48, 160)
        .addBox(-4.0F, -2.2222F, -1.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

    PartDefinition two = box.addOrReplaceChild("two", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));

    PartDefinition halfOne = two.addOrReplaceChild("halfOne", CubeListBuilder.create().texOffs(48, 112).addBox(-1.0F, -2.0F, -4.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(96, 160)
        .addBox(2.0F, -2.0F, -1.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(128, 0).addBox(0.0F, -2.0F, 2.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(144, 176)
        .addBox(-4.0F, -2.0F, 0.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

    PartDefinition halfTwo = two.addOrReplaceChild("halfTwo", CubeListBuilder.create().texOffs(96, 112).addBox(1.0F, -2.0F, -4.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(176, 0)
        .addBox(2.0F, -2.0F, 1.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(144, 48).addBox(-2.0F, -2.0F, 2.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(192, 40)
        .addBox(-4.0F, -2.0F, -2.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

    return LayerDefinition.create(meshdefinition, 256, 256);
  }

  @Override
  public void setupAnim(LamentConfigurationRenderState state) {
    super.setupAnim(state);
    this.spinAnimation.apply(state.spinAnimationState, state.ageInTicks, SPIN_SPEED);
  }

  public enum Part implements StringRepresentable {
    ALL("all"),
    ONE("one"),
    TWO("two");

    public static final Codec<Part> CODEC = StringRepresentable.fromEnum(Part::values);

    private final String name;

    Part(String name) {
      this.name = name;
    }

    @Override
    public String getSerializedName() {
      return this.name;
    }
  }
}
