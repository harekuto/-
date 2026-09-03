package dev.harekuto.motifx;

import dev.harekuto.motifx.api.animation.AnimationClip;
import dev.harekuto.motifx.api.animation.AnimationMixer;
import dev.harekuto.motifx.api.animation.BoneMask;
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

class AnimationMixerTest {
    @Test
    void additiveLayerUsesBindRelativeTranslationAndScale() {
        Transform bind = new Transform(new Vec3f(2, 0, 0), dev.harekuto.motifx.api.math.Quatf.IDENTITY, new Vec3f(2, 2, 2));
        Skeleton skeleton = Skeleton.builder().addRoot("root", bind).build();
        BoneTrack track = new BoneTrack(List.of(
                new BoneTrack.VecKey(0, new Vec3f(4, 0, 0), Interpolation.LINEAR)
        ), List.of(), List.of(
                new BoneTrack.VecKey(0, new Vec3f(4, 4, 4), Interpolation.LINEAR)
        ));
        AnimationClip clip = new AnimationClip("add", 1, LoopMode.HOLD, Map.of(0, track), List.of());
        Pose base = new Pose(skeleton);
        Pose output = new Pose(skeleton);
        Pose scratch = new Pose(skeleton);
        AnimationMixer.mix(skeleton, base, output, scratch,
                List.of(new AnimationMixer.Layer(clip, 0, 0.5f, BoneMask.all(skeleton), true)));
        assertEquals(3.0f, output.get(0).translation().x(), 1.0e-4f);
        assertEquals(3.0f, output.get(0).scale().x(), 1.0e-4f);
    }
}
