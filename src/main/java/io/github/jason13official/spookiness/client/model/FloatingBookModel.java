package io.github.jason13official.spookiness.client.model;

import io.github.jason13official.spookiness.Spookiness;
import io.github.jason13official.spookiness.client.renderer.state.FloatingBookRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class FloatingBookModel extends EntityModel<FloatingBookRenderState> {

  public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Spookiness.id("floating_book"), "main");

  private final ModelPart leftLid;
  private final ModelPart rightLid;
  private final ModelPart leftPages;
  private final ModelPart rightPages;
  private final ModelPart flipPage1;
  private final ModelPart flipPage2;

  public FloatingBookModel(ModelPart root) {
    super(root);
    ModelPart book = root.getChild("book");
    this.leftLid = book.getChild("left_lid");
    this.rightLid = book.getChild("right_lid");
    this.leftPages = book.getChild("left_pages");
    this.rightPages = book.getChild("right_pages");
    this.flipPage1 = book.getChild("flip_page1");
    this.flipPage2 = book.getChild("flip_page2");
  }

  public static LayerDefinition createBodyLayer() {
    MeshDefinition mesh = new MeshDefinition();
    PartDefinition root = mesh.getRoot();

    PartDefinition book = root.addOrReplaceChild("book", CubeListBuilder.create(),
        PartPose.offsetAndRotation(0.0F, 16.0F, 0.0F, (float) (Math.PI / 2), 0.0F, (float) (Math.PI / 2)));

    book.addOrReplaceChild("left_lid", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -5.0F, -0.005F, 6.0F, 10.0F, 0.005F),
        PartPose.offset(0.0F, 0.0F, -1.0F));
    book.addOrReplaceChild("right_lid", CubeListBuilder.create().texOffs(16, 0).addBox(0.0F, -5.0F, -0.005F, 6.0F, 10.0F, 0.005F),
        PartPose.offset(0.0F, 0.0F, 1.0F));
    book.addOrReplaceChild("seam", CubeListBuilder.create().texOffs(12, 0).addBox(-1.0F, -5.0F, 0.0F, 2.0F, 10.0F, 0.005F),
        PartPose.rotation(0.0F, (float) (Math.PI / 2), 0.0F));
    book.addOrReplaceChild("left_pages", CubeListBuilder.create().texOffs(0, 10).addBox(0.0F, -4.0F, -0.99F, 5.0F, 8.0F, 1.0F), PartPose.ZERO);
    book.addOrReplaceChild("right_pages", CubeListBuilder.create().texOffs(12, 10).addBox(0.0F, -4.0F, -0.01F, 5.0F, 8.0F, 1.0F), PartPose.ZERO);

    CubeListBuilder page = CubeListBuilder.create().texOffs(24, 10).addBox(0.0F, -4.0F, 0.0F, 5.0F, 8.0F, 0.005F);
    book.addOrReplaceChild("flip_page1", page, PartPose.ZERO);
    book.addOrReplaceChild("flip_page2", page, PartPose.ZERO);

    return LayerDefinition.create(mesh, 64, 32);
  }

  @Override
  public void setupAnim(FloatingBookRenderState state) {
    super.setupAnim(state);

    float openness = state.openness;
    this.leftLid.yRot = (float) Math.PI + openness;
    this.rightLid.yRot = -openness;
    this.leftPages.yRot = openness;
    this.rightPages.yRot = -openness;
    this.flipPage1.yRot = openness - openness * 2.0F * state.pageFlip1;
    this.flipPage2.yRot = openness - openness * 2.0F * state.pageFlip2;
    this.leftPages.x = Mth.sin(openness);
    this.rightPages.x = Mth.sin(openness);
    this.flipPage1.x = Mth.sin(openness);
    this.flipPage2.x = Mth.sin(openness);
  }
}
