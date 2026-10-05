// Made with Blockbench 5.2.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class LamentConfiguration<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "lamentconfiguration"), "main");
	private final ModelPart box;
	private final ModelPart one;
	private final ModelPart center;
	private final ModelPart pieces;
	private final ModelPart two;
	private final ModelPart halfOne;
	private final ModelPart halfTwo;

	public LamentConfiguration(ModelPart root) {
		this.box = root.getChild("box");
		this.one = this.box.getChild("one");
		this.center = this.one.getChild("center");
		this.pieces = this.one.getChild("pieces");
		this.two = this.box.getChild("two");
		this.halfOne = this.two.getChild("halfOne");
		this.halfTwo = this.two.getChild("halfTwo");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition box = partdefinition.addOrReplaceChild("box", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition one = box.addOrReplaceChild("one", CubeListBuilder.create(), PartPose.offset(0.0F, -1.7778F, 0.0F));

		PartDefinition center = one.addOrReplaceChild("center", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.2222F, 0.0F));

		PartDefinition pieces = one.addOrReplaceChild("pieces", CubeListBuilder.create().texOffs(0, 64).addBox(-2.0F, -2.2222F, -4.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(48, 64).addBox(0.0F, -2.2222F, -4.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(144, 96).addBox(2.0F, -2.2222F, -2.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(144, 136).addBox(2.0F, -2.2222F, 0.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(96, 64).addBox(1.0F, -2.2222F, 2.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 112).addBox(-1.0F, -2.2222F, 2.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 160).addBox(-4.0F, -2.2222F, 1.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(48, 160).addBox(-4.0F, -2.2222F, -1.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition two = box.addOrReplaceChild("two", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition halfOne = two.addOrReplaceChild("halfOne", CubeListBuilder.create().texOffs(48, 112).addBox(-1.0F, -2.0F, -4.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(96, 160).addBox(2.0F, -2.0F, -1.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(128, 0).addBox(0.0F, -2.0F, 2.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(144, 176).addBox(-4.0F, -2.0F, 0.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition halfTwo = two.addOrReplaceChild("halfTwo", CubeListBuilder.create().texOffs(96, 112).addBox(1.0F, -2.0F, -4.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(176, 0).addBox(2.0F, -2.0F, 1.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(144, 48).addBox(-2.0F, -2.0F, 2.0F, 1.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(192, 40).addBox(-4.0F, -2.0F, -2.0F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		box.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}