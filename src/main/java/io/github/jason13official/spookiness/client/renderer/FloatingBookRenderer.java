package io.github.jason13official.spookiness.client.renderer;

import io.github.jason13official.spookiness.client.model.FloatingBookModel;
import io.github.jason13official.spookiness.client.renderer.state.FloatingBookRenderState;
import io.github.jason13official.spookiness.entity.FloatingBook;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class FloatingBookRenderer extends MobRenderer<FloatingBook, FloatingBookRenderState, FloatingBookModel> {

  private static final Identifier BOOK_LOCATION = Identifier.withDefaultNamespace("textures/entity/enchantment/enchanting_table_book.png");

  public FloatingBookRenderer(Context context) {
    super(context, new FloatingBookModel(context.bakeLayer(FloatingBookModel.LAYER_LOCATION)), 0.3F);
  }

  @Override
  public Identifier getTextureLocation(FloatingBookRenderState state) {

    return BOOK_LOCATION;
  }

  @Override
  public FloatingBookRenderState createRenderState() {

    return new FloatingBookRenderState();
  }

  @Override
  public void extractRenderState(FloatingBook entity, FloatingBookRenderState state, float partialTicks) {
    super.extractRenderState(entity, state, partialTicks);

    float open = Mth.lerp(partialTicks, entity.oOpen, entity.open);
    float flip = Mth.lerp(partialTicks, entity.oFlip, entity.flip);
    float flapSpeed = entity.isAggressive() ? 0.7F : 0.35F;
    float flapping = 0.4F + (Mth.sin(state.ageInTicks * flapSpeed) + 1.0F) * 0.45F;
    float reading = Mth.sin(state.ageInTicks * 0.02F) * 0.1F + 1.25F;

    state.openness = Mth.lerp(open, flapping, reading);
    state.pageFlip1 = Mth.clamp(Mth.frac(flip + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
    state.pageFlip2 = Mth.clamp(Mth.frac(flip + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);
  }

  @Override
  public Vec3 getRenderOffset(FloatingBookRenderState state) {

    return super.getRenderOffset(state).add(0.0, Mth.sin(state.ageInTicks * 0.1F) * 0.05, 0.0);
  }
}
