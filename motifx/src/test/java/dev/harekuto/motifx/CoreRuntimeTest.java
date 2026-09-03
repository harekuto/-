package dev.harekuto.motifx;

import dev.harekuto.motifx.api.animation.AnimationModel.Bone;
import dev.harekuto.motifx.api.animation.AnimationModel.Pose;
import dev.harekuto.motifx.api.animation.AnimationModel.Skeleton;
import dev.harekuto.motifx.api.animation.AnimationRuntime.BlendMode;
import dev.harekuto.motifx.api.animation.AnimationRuntime.BoneMask;
import dev.harekuto.motifx.api.animation.AnimationRuntime.BoneTrack;
import dev.harekuto.motifx.api.animation.AnimationRuntime.Clip;
import dev.harekuto.motifx.api.animation.AnimationRuntime.Interpolation;
import dev.harekuto.motifx.api.animation.AnimationRuntime.Layer;
import dev.harekuto.motifx.api.animation.AnimationRuntime.LoopMode;
import dev.harekuto.motifx.api.animation.AnimationRuntime.Mixer;
import dev.harekuto.motifx.api.animation.AnimationRuntime.QuatTrack;
import dev.harekuto.motifx.api.animation.AnimationRuntime.VecTrack;
import dev.harekuto.motifx.api.compat.CompatibilityApi;
import dev.harekuto.motifx.api.compat.importer.BedrockAnimationImporter;
import dev.harekuto.motifx.api.diagnostics.Diagnostics;
import dev.harekuto.motifx.api.graph.GraphRuntime;
import dev.harekuto.motifx.api.math.MathTypes.Quatf;
import dev.harekuto.motifx.api.math.MathTypes.Transform;
import dev.harekuto.motifx.api.math.MathTypes.Vec3f;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CoreRuntimeTest {
    private static Skeleton skeleton() {
        return new Skeleton(List.of(
            new Bone("root", -1, Transform.IDENTITY),
            new Bone("arm", 0, Transform.IDENTITY)
        ));
    }

    @Test
    void quaternionSlerpStaysFiniteAndNormalized() {
        Quatf half = Quatf.slerp(Quatf.IDENTITY, Quatf.fromEulerDegrees(0f, 180f, 0f), 0.5f);
        assertTrue(half.isFinite());
        double length = Math.sqrt((double)half.x() * half.x() + (double)half.y() * half.y() + (double)half.z() * half.z() + (double)half.w() * half.w());
        assertEquals(1.0, length, 1.0e-5);
    }

    @Test
    void clipSamplesAndLoopsDeterministically() {
        Skeleton skeleton = skeleton();
        VecTrack translation = new VecTrack(new float[]{0f, 1f}, new Vec3f[]{Vec3f.ZERO, new Vec3f(10f, 0f, 0f)}, Interpolation.LINEAR);
        Clip clip = new Clip("move", 1f, LoopMode.LOOP, List.of(new BoneTrack(0, translation, null, null)), List.of());
        Pose pose = new Pose(skeleton);
        clip.sample(skeleton, 1.25f, pose);
        assertEquals(2.5f, pose.get(0).translation().x(), 1.0e-5f);
        assertTrue(pose.isFinite());
    }

    @Test
    void mixerRespectsMaskAndAdditiveLayer() {
        Skeleton skeleton = skeleton();
        Clip moveArm = new Clip("arm", 1f, LoopMode.HOLD,
            List.of(new BoneTrack(1, new VecTrack(new float[]{0f, 1f}, new Vec3f[]{Vec3f.ZERO, new Vec3f(2f, 0f, 0f)}, Interpolation.LINEAR), null, null)), List.of());
        Layer layer = new Layer("arm-add", moveArm, 10, BlendMode.ADDITIVE, BoneMask.only(2, 1), 0.5f);
        layer.setTime(1f);
        Mixer mixer = new Mixer(skeleton);
        mixer.addLayer(layer);
        Pose pose = new Pose(skeleton);
        mixer.evaluate(pose);
        assertEquals(0f, pose.get(0).translation().x(), 1.0e-6f);
        assertEquals(1f, pose.get(1).translation().x(), 1.0e-6f);
    }

    @Test
    void typedGraphTransitionsWithoutStringMapHotPath() {
        Clip idle = new Clip("idle", 1f, LoopMode.LOOP, List.of(), List.of());
        Clip run = new Clip("run", 1f, LoopMode.LOOP, List.of(), List.of());
        GraphRuntime.ParameterSchema.Builder schemaBuilder = new GraphRuntime.ParameterSchema.Builder();
        GraphRuntime.BoolKey moving = schemaBuilder.boolKey("moving");
        GraphRuntime.ParameterSchema schema = schemaBuilder.build();
        GraphRuntime.Parameters parameters = schema.create();
        GraphRuntime.StateGraph graph = new GraphRuntime.StateGraph.Builder()
            .state("idle", idle)
            .state("run", run)
            .initial("idle")
            .transition("idle", "run", p -> p.get(moving), 0.2f, 100)
            .build();
        GraphRuntime.Instance instance = graph.createInstance();
        assertEquals("idle", instance.update(0.05f, parameters).stateId());
        parameters.set(moving, true);
        GraphRuntime.GraphSample sample = instance.update(0.05f, parameters);
        assertEquals("run", sample.stateId());
        assertTrue(sample.transitioning());
    }

    @Test
    void compatibilityRegistryUsesPriorityAndNoHardDependency() {
        CompatibilityApi.Registry registry = new CompatibilityApi.Registry();
        registry.register(new CompatibilityApi.Adapter() {
            public String id() { return "low"; }
            public int priority() { return 10; }
            public boolean supportsMod(String modId) { return modId.equals("example"); }
            public Set<CompatibilityApi.Capability> capabilities() { return Set.of(CompatibilityApi.Capability.SKELETAL_POSE); }
        });
        registry.register(new CompatibilityApi.Adapter() {
            public String id() { return "high"; }
            public int priority() { return 50; }
            public boolean supportsMod(String modId) { return modId.equals("example"); }
            public Set<CompatibilityApi.Capability> capabilities() { return Set.of(CompatibilityApi.Capability.ASSET_IMPORT); }
        });
        assertEquals(List.of("high", "low"), registry.resolve("example").stream().map(CompatibilityApi.Adapter::id).toList());
    }

    @Test
    void diagnosticsRecognizeBedrockGeckoStyleAnimationAndRejectBadDuration() {
        String json = "{\"animations\":{\"walk\":{\"animation_length\":-1,\"bones\":{}}}}";
        Diagnostics.Report report = Diagnostics.validateAnimationJson(json);
        assertEquals(Diagnostics.AssetFormat.GECKOLIB_BEDROCK_ANIMATION, report.format());
        assertFalse(report.valid());
        assertTrue(report.issues().stream().anyMatch(issue -> issue.code().equals("duration")));
    }

    @Test
    void numericBedrockGeckoImporterMapsBonesKeyframesAndLoop() {
        Skeleton skeleton = skeleton();
        String json = "{\"animations\":{\"walk\":{\"loop\":true,\"animation_length\":1.0,\"bones\":{\"arm\":{\"position\":{\"0.0\":[0,0,0],\"1.0\":[2,0,0]},\"rotation\":[0,90,0]},\"missing_bone\":{\"position\":[1,2,3]}}}}}";
        BedrockAnimationImporter.ImportResult imported = BedrockAnimationImporter.importAnimations(json, skeleton);
        assertTrue(imported.valid());
        assertTrue(imported.issues().stream().anyMatch(issue -> issue.code().equals("unknown_bone")));
        Clip clip = imported.clips().get("walk");
        assertNotNull(clip);
        assertEquals(LoopMode.LOOP, clip.loopMode());
        Pose pose = new Pose(skeleton);
        clip.sample(skeleton, 0.5f, pose);
        assertEquals(1f, pose.get(1).translation().x(), 1.0e-5f);
        assertTrue(pose.isFinite());
    }

    @Test
    void numericImporterReportsMolangInsteadOfPretendingToSupportIt() {
        String json = "{\"animations\":{\"dynamic\":{\"bones\":{\"arm\":{\"position\":[\"query.anim_time\",0,0]}}}}}";
        BedrockAnimationImporter.ImportResult imported = BedrockAnimationImporter.importAnimations(json, skeleton());
        assertTrue(imported.valid());
        assertTrue(imported.issues().stream().anyMatch(issue -> issue.code().equals("unsupported_channel") || issue.code().equals("unsupported_expression")));
    }

    @Test
    void skeletonRejectsForwardParentReference() {
        assertThrows(IllegalArgumentException.class, () -> new Skeleton(List.of(
            new Bone("root", 1, Transform.IDENTITY),
            new Bone("child", -1, Transform.IDENTITY)
        )));
    }
}
