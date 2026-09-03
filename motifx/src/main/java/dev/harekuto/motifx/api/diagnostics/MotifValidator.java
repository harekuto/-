package dev.harekuto.motifx.api.diagnostics;

import dev.harekuto.motifx.api.animation.AnimationClip;
import dev.harekuto.motifx.api.animation.AnimationEventMarker;
import dev.harekuto.motifx.api.animation.BoneTrack;
import dev.harekuto.motifx.api.animation.Skeleton;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class MotifValidator {
    private MotifValidator() {}

    public static ValidationReport validate(Skeleton skeleton, AnimationClip clip) {
        ValidationReport report = new ValidationReport();

        for (int i = 0; i < skeleton.boneCount(); i++) {
            Skeleton.Bone bone = skeleton.bone(i);
            if (!bone.bindPose().isFinite()) {
                report.add(new Diagnostic(Diagnostic.Severity.ERROR, "MX-SKELETON-NONFINITE",
                        "bone:" + bone.name(), "Bind pose contains NaN or infinity.",
                        "Repair the bone transform before compiling the animation."));
            }
        }

        for (Map.Entry<Integer, BoneTrack> entry : clip.tracks().entrySet()) {
            int boneIndex = entry.getKey();
            if (boneIndex < 0 || boneIndex >= skeleton.boneCount()) {
                report.add(new Diagnostic(Diagnostic.Severity.ERROR, "MX-CLIP-BONE-RANGE",
                        "clip:" + clip.name(), "Track references invalid bone index " + boneIndex + '.',
                        "Recompile the clip against the correct skeleton."));
                continue;
            }
            validateTrack(report, skeleton.bone(boneIndex).name(), clip, entry.getValue());
        }

        Set<String> markerAtTime = new HashSet<>();
        for (AnimationEventMarker marker : clip.events()) {
            if (marker.time() < 0.0f || marker.time() > clip.duration()) {
                report.add(new Diagnostic(Diagnostic.Severity.ERROR, "MX-EVENT-TIME",
                        "event:" + marker.id(), "Event marker lies outside clip duration.",
                        "Move the marker into the inclusive range 0..duration."));
            }
            String key = marker.id() + '@' + marker.time();
            if (!markerAtTime.add(key)) {
                report.add(new Diagnostic(Diagnostic.Severity.WARNING, "MX-EVENT-DUPLICATE",
                        "event:" + marker.id(), "Duplicate event marker at the same time.",
                        "Remove the duplicate unless it is intentional."));
            }
        }
        return report;
    }

    private static void validateTrack(ValidationReport report, String boneName, AnimationClip clip, BoneTrack track) {
        track.translations().forEach(key -> validateTime(report, boneName, clip, key.time(), key.value().isFinite()));
        track.scales().forEach(key -> validateTime(report, boneName, clip, key.time(), key.value().isFinite()));
        track.rotations().forEach(key -> validateTime(report, boneName, clip, key.time(), key.value().isFinite()));
    }

    private static void validateTime(ValidationReport report, String boneName, AnimationClip clip, float time, boolean finiteValue) {
        if (!Float.isFinite(time) || time < 0.0f || time > clip.duration()) {
            report.add(new Diagnostic(Diagnostic.Severity.ERROR, "MX-KEY-TIME",
                    "bone:" + boneName, "Keyframe time is outside clip duration: " + time,
                    "Keep keyframe times within 0..duration."));
        }
        if (!finiteValue) {
            report.add(new Diagnostic(Diagnostic.Severity.ERROR, "MX-KEY-NONFINITE",
                    "bone:" + boneName, "Keyframe contains NaN or infinity.",
                    "Replace invalid numeric values before loading the asset."));
        }
    }
}
