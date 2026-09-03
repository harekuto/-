package dev.harekuto.motifx.api.animation;

import dev.harekuto.motifx.api.math.Vec3f;

import java.util.List;
import java.util.Objects;

/** Stateless layered pose mixer. Scratch poses are supplied by the caller to avoid hidden allocations. */
public final class AnimationMixer {
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
                if (layer.additive()) {
                    Transform bind = skeleton.bone(bone).bindPose();
                    Transform additiveTarget = new Transform(
                            current.translation().add(sampled.translation().add(bind.translation().multiply(-1.0f)).multiply(weight)),
                            current.rotation().slerp(current.rotation().multiply(bind.rotation().normalized().negated()).multiply(sampled.rotation()).normalized(), weight),
                            current.scale().lerp(sampled.scale(), weight)
                    );
                    output.set(bone, additiveTarget);
                } else {
                    output.set(bone, current.blend(sampled, weight));
                }
            }
        }
    }
}
