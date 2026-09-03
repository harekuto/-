package dev.harekuto.motifx.api.animation;

import java.util.Arrays;
import java.util.Objects;

/** Mutable reusable local-space pose. Runtime users should reuse instances in hot paths. */
public final class Pose {
    private final Skeleton skeleton;
    private final Transform[] local;

    public Pose(Skeleton skeleton) {
        this.skeleton = Objects.requireNonNull(skeleton, "skeleton");
        this.local = new Transform[skeleton.boneCount()];
        resetToBindPose();
    }

    public Skeleton skeleton() {
        return skeleton;
    }

    public int size() {
        return local.length;
    }

    public Transform get(int boneIndex) {
        return local[boneIndex];
    }

    public void set(int boneIndex, Transform transform) {
        local[boneIndex] = Objects.requireNonNull(transform, "transform");
    }

    public void resetToBindPose() {
        for (int i = 0; i < local.length; i++) {
            local[i] = skeleton.bone(i).bindPose();
        }
    }

    public void copyFrom(Pose other) {
        requireCompatible(other);
        System.arraycopy(other.local, 0, local, 0, local.length);
    }

    public void blendFrom(Pose source, Pose target, float alpha, BoneMask mask) {
        requireCompatible(source);
        requireCompatible(target);
        if (mask.skeleton() != skeleton) {
            throw new IllegalArgumentException("BoneMask belongs to another skeleton");
        }
        for (int i = 0; i < local.length; i++) {
            local[i] = mask.includes(i) ? source.local[i].blend(target.local[i], alpha) : source.local[i];
        }
    }

    public Transform[] copyTransforms() {
        return Arrays.copyOf(local, local.length);
    }

    private void requireCompatible(Pose other) {
        if (other.skeleton != skeleton) {
            throw new IllegalArgumentException("Pose belongs to another skeleton");
        }
    }
}
