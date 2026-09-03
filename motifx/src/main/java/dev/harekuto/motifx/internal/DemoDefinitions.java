package dev.harekuto.motifx.internal;

import dev.harekuto.motifx.api.animation.AnimationClip;
import dev.harekuto.motifx.api.animation.BoneTrack;
import dev.harekuto.motifx.api.animation.Interpolation;
import dev.harekuto.motifx.api.animation.LoopMode;
import dev.harekuto.motifx.api.animation.Skeleton;
import dev.harekuto.motifx.api.animation.Transform;
import dev.harekuto.motifx.api.graph.AnimationStateGraph;
import dev.harekuto.motifx.api.graph.ParameterLayout;
import dev.harekuto.motifx.api.math.Quatf;
import dev.harekuto.motifx.api.math.Vec3f;

import java.util.List;
import java.util.Map;

public final class DemoDefinitions {
    public record DemoBundle(Skeleton skeleton, ParameterLayout layout, ParameterLayout.BoolParam wave,
                             AnimationStateGraph graph, int armBone) {}

    private DemoDefinitions() {}

    public static DemoBundle create() {
        Skeleton skeleton = Skeleton.builder()
                .addRoot("root", Transform.IDENTITY)
                .add("body", "root", Transform.IDENTITY)
                .add("right_arm", "body", Transform.IDENTITY)
                .build();
        int arm = skeleton.requireIndex("right_arm");

        AnimationClip idle = new AnimationClip("idle", 1.0f, LoopMode.LOOP, Map.of(), List.of());
        BoneTrack waveTrack = new BoneTrack(List.of(), List.of(
                new BoneTrack.RotKey(0.0f, Quatf.fromEulerDegrees(0, 0, -15), Interpolation.SMOOTHSTEP),
                new BoneTrack.RotKey(0.5f, Quatf.fromEulerDegrees(0, 0, 55), Interpolation.SMOOTHSTEP),
                new BoneTrack.RotKey(1.0f, Quatf.fromEulerDegrees(0, 0, -15), Interpolation.SMOOTHSTEP)
        ), List.of());
        AnimationClip waveClip = new AnimationClip("wave", 1.0f, LoopMode.LOOP, Map.of(arm, waveTrack), List.of());

        ParameterLayout.Builder parameters = ParameterLayout.builder();
        ParameterLayout.BoolParam wave = parameters.boolParam("wave");
        ParameterLayout layout = parameters.build();

        AnimationStateGraph graph = AnimationStateGraph.builder()
                .state("idle", idle)
                .state("wave", waveClip)
                .initial("idle")
                .transition("idle", "wave", 0.15f, 100, store -> store.get(wave))
                .transition("wave", "idle", 0.15f, 100, store -> !store.get(wave))
                .build();

        return new DemoBundle(skeleton, layout, wave, graph, arm);
    }
}
