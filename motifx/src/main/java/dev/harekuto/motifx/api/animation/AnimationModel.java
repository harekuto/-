package dev.harekuto.motifx.api.animation;

import dev.harekuto.motifx.api.math.MathTypes.Transform;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class AnimationModel {
    private AnimationModel() {}

    public record Bone(String name, int parentIndex, Transform bindPose) {
        public Bone {
            if (name == null || name.isBlank()) throw new IllegalArgumentException("Bone name must not be blank");
            Objects.requireNonNull(bindPose, "bindPose");
            if (!bindPose.isFinite()) throw new IllegalArgumentException("Bone bind pose must be finite: " + name);
        }
    }

    public static final class Skeleton {
        private final List<Bone> bones;
        private final Map<String, Integer> indices;

        public Skeleton(List<Bone> input) {
            Objects.requireNonNull(input, "input");
            if (input.isEmpty()) throw new IllegalArgumentException("Skeleton must contain at least one bone");
            List<Bone> copy = new ArrayList<>(input.size());
            Map<String, Integer> names = new HashMap<>();
            for (int i = 0; i < input.size(); i++) {
                Bone bone = Objects.requireNonNull(input.get(i), "bone[" + i + "]");
                if (bone.parentIndex() >= i || bone.parentIndex() < -1) {
                    throw new IllegalArgumentException("Bone parent must be -1 or precede the bone: " + bone.name());
                }
                if (names.putIfAbsent(bone.name(), i) != null) {
                    throw new IllegalArgumentException("Duplicate bone name: " + bone.name());
                }
                copy.add(bone);
            }
            this.bones = List.copyOf(copy);
            this.indices = Collections.unmodifiableMap(names);
        }

        public int size() { return bones.size(); }
        public Bone bone(int index) { return bones.get(index); }
        public Transform bindPose(int index) { return bones.get(index).bindPose(); }
        public List<Bone> bones() { return bones; }
        public int indexOf(String name) { return indices.getOrDefault(name, -1); }
    }

    public static final class Pose {
        private final Skeleton skeleton;
        private final Transform[] local;

        public Pose(Skeleton skeleton) {
            this.skeleton = Objects.requireNonNull(skeleton, "skeleton");
            this.local = new Transform[skeleton.size()];
            resetToBind();
        }

        public Skeleton skeleton() { return skeleton; }
        public int size() { return local.length; }
        public Transform get(int index) { return local[index]; }

        public void set(int index, Transform transform) {
            Objects.requireNonNull(transform, "transform");
            if (!transform.isFinite()) throw new IllegalArgumentException("Pose transform must be finite at bone " + index);
            local[index] = transform;
        }

        public void resetToBind() {
            for (int i = 0; i < local.length; i++) local[i] = skeleton.bindPose(i);
        }

        public void copyFrom(Pose other) {
            Objects.requireNonNull(other, "other");
            if (other.skeleton != this.skeleton) throw new IllegalArgumentException("Pose skeleton mismatch");
            System.arraycopy(other.local, 0, local, 0, local.length);
        }

        public Pose copy() {
            Pose out = new Pose(skeleton);
            out.copyFrom(this);
            return out;
        }

        public boolean isFinite() {
            for (Transform transform : local) if (transform == null || !transform.isFinite()) return false;
            return true;
        }
    }
}
