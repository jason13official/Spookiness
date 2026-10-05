package io.github.jason13official.spookiness.client.anim;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class HallowedMotherAnimations {

  public static final AnimationDefinition SPIT = AnimationDefinition.Builder.withLength(0.5F)
      .addAnimation("cranium", new AnimationChannel(AnimationChannel.Targets.ROTATION,
          new Keyframe(0.0F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
          new Keyframe(0.12F, KeyframeAnimations.degreeVec(-40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
          new Keyframe(0.3F, KeyframeAnimations.degreeVec(-40.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM),
          new Keyframe(0.5F, KeyframeAnimations.degreeVec(0.0F, 0.0F, 0.0F), AnimationChannel.Interpolations.CATMULLROM)
      ))
      .addAnimation("head", new AnimationChannel(AnimationChannel.Targets.SCALE,
          new Keyframe(0.0F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM),
          new Keyframe(0.1F, KeyframeAnimations.scaleVec(0.8F, 0.8F, 0.8F), AnimationChannel.Interpolations.CATMULLROM),
          new Keyframe(0.25F, KeyframeAnimations.scaleVec(1.08F, 1.08F, 1.08F), AnimationChannel.Interpolations.CATMULLROM),
          new Keyframe(0.5F, KeyframeAnimations.scaleVec(1.0F, 1.0F, 1.0F), AnimationChannel.Interpolations.CATMULLROM)
      ))
      .build();
}
