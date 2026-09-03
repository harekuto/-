package dev.harekuto.motifx;

import dev.harekuto.motifx.api.animation.AnimationClip;
import dev.harekuto.motifx.api.animation.BoneTrack;
import dev.harekuto.motifx.api.animation.Interpolation;
import dev.harekuto.motifx.api.animation.LoopMode;
import dev.harekuto.motifx.api.animation.Pose;
import dev.harekuto.motifx.api.animation.Skeleton;
import dev.harekuto.motifx.api.animation.Transform;
import dev.harekuto.motifx.api.math.Vec3f;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnimationRuntimeTest {
    @Test
    void clipInterpolatesTranslationAtHalfTime() {
        Skeleton skeleton = Skeleton.builder().addRoot("root", Transform.IDENTITY).build();
        BoneTrack track = new BoneTrack(List.of(
                new BoneTrack.VecKey(0.0f, Vec3f.ZERO, Interpolation.LINEAR),
                new BoneTrack.VecKey(1.0f, new Vec3f(10, 0, 0), Interpolation.LINEAR)
        ), List.of(), List.of());
        AnimationClip clip = new AnimationClip("move", 1.0f, LoopMode.HOLD, Map.of(0, track), List.of());
        Pose pose = new Pose(skeleton);
        clip.sample(skeleton, 0.5f, pose);
        assertEquals(5.0f, pose.get(0).translation().x(), 1.0e-4f);
    }
}
