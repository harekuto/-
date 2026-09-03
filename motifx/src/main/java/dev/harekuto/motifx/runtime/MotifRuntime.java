package dev.harekuto.motifx.runtime;

import dev.harekuto.motifx.api.animation.AnimationModel.Bone;
import dev.harekuto.motifx.api.animation.AnimationModel.Pose;
import dev.harekuto.motifx.api.animation.AnimationModel.Skeleton;
import dev.harekuto.motifx.api.animation.AnimationRuntime;
import dev.harekuto.motifx.api.animation.AnimationRuntime.BoneTrack;
import dev.harekuto.motifx.api.animation.AnimationRuntime.Clip;
import dev.harekuto.motifx.api.animation.AnimationRuntime.Interpolation;
import dev.harekuto.motifx.api.animation.AnimationRuntime.LoopMode;
import dev.harekuto.motifx.api.animation.AnimationRuntime.QuatTrack;
import dev.harekuto.motifx.api.compat.CompatibilityApi.PosePipeline;
import dev.harekuto.motifx.api.compat.CompatibilityApi.Registry;
import dev.harekuto.motifx.api.diagnostics.Diagnostics;
import dev.harekuto.motifx.api.math.MathTypes.Quatf;
import dev.harekuto.motifx.api.math.MathTypes.Transform;
import dev.harekuto.motifx.api.math.MathTypes.Vec3f;

import java.util.List;
import java.util.Set;

public final class MotifRuntime {
    public static final MotifRuntime INSTANCE = new MotifRuntime();

    private final Registry compatibility = new Registry();
    private final PosePipeline posePipeline = new PosePipeline();
    private final RuntimeMetrics metrics = new RuntimeMetrics();
    private final Set<String> features = Set.of(
        "skeletal-pose",
        "quaternion-slerp",
        "loop-modes",
        "event-markers",
        "bone-masks",
        "override-layers",
        "additive-layers",
        "typed-graph-parameters",
        "priority-transitions",
        "pose-compositor-spi",
        "compatibility-adapter-spi",
        "asset-format-detection",
        "structured-validation",
        "runtime-metrics",
        "client-inspector"
    );

    private MotifRuntime() {}

    public Registry compatibility() { return compatibility; }
    public PosePipeline posePipeline() { return posePipeline; }
    public RuntimeMetrics metrics() { return metrics; }
    public Set<String> features() { return features; }

    public SelfTestResult selfTest() {
        try {
            Skeleton skeleton = new Skeleton(List.of(
                new Bone("root", -1, Transform.IDENTITY),
                new Bone("arm", 0, Transform.IDENTITY)
            ));
            QuatTrack armRotation = new QuatTrack(
                new float[] {0f, 1f},
                new Quatf[] {Quatf.IDENTITY, Quatf.fromEulerDegrees(0f, 90f, 0f)},
                Interpolation.SMOOTHSTEP
            );
            Clip clip = new Clip("selftest", 1f, LoopMode.LOOP, List.of(new BoneTrack(1, null, armRotation, null)), List.of(new AnimationRuntime.Marker(0.5f, "half")));
            Pose pose = new Pose(skeleton);
            clip.sample(skeleton, 0.5f, pose);
            metrics.poseEvaluated();
            Diagnostics.Report report = Diagnostics.validateAnimationJson("{\"format_version\":1,\"clips\":{\"selftest\":{}}}");
            metrics.validation(report.errorCount());
            boolean ok = pose.isFinite() && report.valid() && Diagnostics.detectFormat("{\"animations\":{\"idle\":{\"animation_length\":1}}}") == Diagnostics.AssetFormat.GECKOLIB_BEDROCK_ANIMATION;
            return new SelfTestResult(ok, ok ? "core math, sampling and format validation passed" : "core self-test returned an invalid state");
        } catch (RuntimeException ex) {
            return new SelfTestResult(false, ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    public record SelfTestResult(boolean passed, String detail) {}
}
