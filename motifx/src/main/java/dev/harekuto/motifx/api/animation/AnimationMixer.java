package dev.harekuto.motifx.api.animation;

import dev.harekuto.motifx.api.math.Quatf;
import dev.harekuto.motifx.api.math.Vec3f;

import java.util.List;
import java.util.Objects;

/** Stateless layered pose mixer. Scratch poses are supplied by the caller to avoid hidden allocations. */
public final class AnimationMixer {
    private static final float SCALE_EPSILON = 1.0e-6f;

    public record Layer(AnimationClip clip, float time, float weight, BoneMask mask, boolean additive) {
        public Layer {
            Objects.requireNonNull(clip, "clip");
            Objects.requireNonNull(mask, "mask");
        }
    }

    private AnimationMixer() {}

    public static void mix(Skeleton skeleton, Pose base, Pose output, Pose scratch, List<Layer> layers) {
        if (base.skeleton() != skeleton || output.skeleton() != skeleton || scratch.skeleton() != skeleton) {
            throw new IllegalArgumentException("All poses must belong to the mixer skeleton");
        }
        output.copyFrom(base);
        for (Layer layer : layers) {
            if (layer.mask().skeleton() != skeleton) {
                throw new IllegalArgumentException("Layer mask belongs to another skeleton");
            }
            float weight = Vec3f.clamp01(layer.weight());
            if (weight <= 0.0f) continue;
            layer.clip().sample(skeleton, layer.time(), scratch);
            for (int bone = 0; bone < skeleton.boneCount(); bone++) {
                if (!layer.mask().includes(bone)) continue;
                Transform current = output.get(bone);
                Transform sampled = scratch.get(bone);
                output.set(bone, layer.additive()
                        ? applyAdditive(current, sampled, skeleton.bone(bone).bindPose(), weight)
                        : current.blend(sampled, weight));
            }
        }
    }

    private static Transform applyAdditive(Transform current, Transform sampled, Transform bind, float weight) {
        Vec3f translationDelta = sampled.translation().subtract(bind.translation());
        Vec3f translation = current.translation().add(translationDelta.multiply(weight));

        Quatf rotationDelta = bind.rotation().inverse().multiply(sampled.rotation()).normalized();
        Quatf weightedRotationDelta = Quatf.IDENTITY.slerp(rotationDelta, weight);
        Quatf rotation = current.rotation().multiply(weightedRotationDelta).normalized();

        Vec3f scaleRatio = new Vec3f(
                safeRatio(sampled.scale().x(), bind.scale().x()),
                safeRatio(sampled.scale().y(), bind.scale().y()),
                safeRatio(sampled.scale().z(), bind.scale().z())
        );
        Vec3f weightedScaleRatio = Vec3f.ONE.lerp(scaleRatio, weight);
        Vec3f scale = current.scale().multiply(weightedScaleRatio);
        return new Transform(translation, rotation, scale);
    }

    private static float safeRatio(float sampled, float bind) {
        return Math.abs(bind) < SCALE_EPSILON ? 1.0f : sampled / bind;
    }
}
