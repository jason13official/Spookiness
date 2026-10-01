// Made with Blockbench 5.2.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class JackOGolemModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "jackogolemmodel"), "main");
	private final ModelPart head;
	private final ModelPart cranium;
	private final ModelPart jaw;

	public JackOGolemModel(ModelPart root) {
		this.head = root.getChild("head");
		this.cranium = this.head.getChild("cranium");
		this.jaw = this.head.getChild("jaw");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 0.0F));

		PartDefinition cranium = head.addOrReplaceChild("cranium", CubeListBuilder.create().texOffs(0, 32).addBox(-8.0F, -12.0F, -16.0F, 16.0F, 12.0F, 16.0F, new CubeDeformation(-0.5F)), PartPose.offset(0.0F, 5.0F, 8.0F));

		PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 44).addBox(-8.0F, -2.0F, -8.0F, 16.0F, 4.0F, 16.0F, new CubeDeformation(-0.5F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}