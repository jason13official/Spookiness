package io.github.jason13official.spookiness.client.model;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.client.anim.JackOGolemAnimations;
import io.github.jason13official.spookiness.client.renderer.state.JackOGolemRenderState;
import net.minecraft.client.animation.KeyframeAnimation;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class JackOMimicModel<T extends JackOGolemRenderState> extends EntityModel<T> {

  public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Spookiness.id("jack_o_mimic"), "main");

  private final ModelPart head;
  private final ModelPart cranium;
  private final ModelPart jaw;

  private final KeyframeAnimation yappingAnimation;

  public JackOMimicModel(ModelPart root) {
    super(root);
    this.head = root.getChild("head");
    this.cranium = this.head.getChild("cranium");
    this.jaw = this.head.getChild("jaw");

    this.yappingAnimation = JackOGolemAnimations.YAP.bake(root);
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition meshdefinition = new MeshDefinition();
    PartDefinition partdefinition = meshdefinition.getRoot();

    PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 0.0F));

    PartDefinition cranium = head.addOrReplaceChild("cranium", CubeListBuilder.create().texOffs(0, 32)
//            .addBox(-8.0F, -12.0F, -16.0F, 16.0F, 12.0F, 16.0F, new CubeDeformation(-0.5F)),
            .addBox(-8.0F, -12.0F, -16.0F, 16.0F, 12.0F, 16.0F, CubeDeformation.NONE),
        // PartPose.offset(0.0F, 5.0F, 8.0F));
        PartPose.offset(0.0F, 4.0F, 8.0F));

    PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 44)
//            .addBox(-8.0F, -2.0F, -8.0F, 16.0F, 4.0F, 16.0F, new CubeDeformation(-0.5F)),
            .addBox(-8.0F, -2.0F, -8.0F, 16.0F, 4.0F, 16.0F, CubeDeformation.NONE),
        PartPose.offset(0.0F, 6.0F, 0.0F));

    return LayerDefinition.create(meshdefinition, 64, 64);
  }

  @Override
  public void setupAnim(T state) {

    super.setupAnim(state); // calls to resetPose

    this.yappingAnimation.apply(state.yapAnimationState, state.ageInTicks);
  }
}