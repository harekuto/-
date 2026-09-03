package dev.harekuto.motifx.api.animation;

import dev.harekuto.motifx.api.math.Quatf;
import dev.harekuto.motifx.api.math.Vec3f;

import java.util.Comparator;
import java.util.List;

/** Immutable per-bone keyframe track. */
public final class BoneTrack {
    public record VecKey(float time, Vec3f value, Interpolation interpolation) {}
    public record RotKey(float time, Quatf value, Interpolation interpolation) {}

    private final List<VecKey> translations;
    private final List<RotKey> rotations;
    private final List<VecKey> scales;

    public BoneTrack(List<VecKey> translations, List<RotKey> rotations, List<VecKey> scales) {
        this.translations = sortedVec(translations);
        this.rotations = sortedRot(rotations);
        this.scales = sortedVec(scales);
    }

    public static BoneTrack empty() {
        return new BoneTrack(List.of(), List.of(), List.of());
    }

    public List<VecKey> translations() {
        return translations;
    }

    public List<RotKey> rotations() {
        return rotations;
    }

    public List<VecKey> scales() {
        return scales;
    }

    public Transform sample(float time, Transform fallback) {
        Vec3f translation = sampleVec(translations, time, fallback.translation());
        Quatf rotation = sampleRot(rotations, time, fallback.rotation());
        Vec3f scale = sampleVec(scales, time, fallback.scale());
        return new Transform(translation, rotation, scale);
    }

    private static List<VecKey> sortedVec(List<VecKey> keys) {
        return keys == null ? List.of() : keys.stream().sorted(Comparator.comparingDouble(VecKey::time)).toList();
    }

    private static List<RotKey> sortedRot(List<RotKey> keys) {
        return keys == null ? List.of() : keys.stream().sorted(Comparator.comparingDouble(RotKey::time)).toList();
    }

    private static Vec3f sampleVec(List<VecKey> keys, float time, Vec3f fallback) {
        if (keys.isEmpty()) return fallback;
        if (time <= keys.get(0).time()) return keys.get(0).value();
        if (time >= keys.get(keys.size() - 1).time()) return keys.get(keys.size() - 1).value();
        int left = segmentVec(keys, time);
        VecKey a = keys.get(left);
        VecKey b = keys.get(left + 1);
        float span = b.time() - a.time();
        float alpha = span <= 0.0f ? 0.0f : (time - a.time()) / span;
        return a.value().lerp(b.value(), a.interpolation().apply(alpha));
    }

    private static Quatf sampleRot(List<RotKey> keys, float time, Quatf fallback) {
        if (keys.isEmpty()) return fallback;
        if (time <= keys.get(0).time()) return keys.get(0).value();
        if (time >= keys.get(keys.size() - 1).time()) return keys.get(keys.size() - 1).value();
        int left = segmentRot(keys, time);
        RotKey a = keys.get(left);
        RotKey b = keys.get(left + 1);
        float span = b.time() - a.time();
        float alpha = span <= 0.0f ? 0.0f : (time - a.time()) / span;
        return a.value().slerp(b.value(), a.interpolation().apply(alpha));
    }

    private static int segmentVec(List<VecKey> keys, float time) {
        int low = 0;
        int high = keys.size() - 2;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (time < keys.get(mid).time()) {
                high = mid - 1;
            } else if (time >= keys.get(mid + 1).time()) {
                low = mid + 1;
            } else {
                return mid;
            }
        }
        return Math.max(0, Math.min(keys.size() - 2, low));
    }

    private static int segmentRot(List<RotKey> keys, float time) {
        int low = 0;
        int high = keys.size() - 2;
        while (low <= high) {
            int mid = (low + high) >>> 1;
            if (time < keys.get(mid).time()) {
                high = mid - 1;
            } else if (time >= keys.get(mid + 1).time()) {
                low = mid + 1;
            } else {
                return mid;
            }
        }
        return Math.max(0, Math.min(keys.size() - 2, low));
    }
}
