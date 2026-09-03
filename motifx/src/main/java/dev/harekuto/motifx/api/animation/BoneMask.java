package dev.harekuto.motifx.api.animation;

import java.util.BitSet;
import java.util.Objects;

public final class BoneMask {
    private final Skeleton skeleton;
    private final BitSet included;

    private BoneMask(Skeleton skeleton, BitSet included) {
        this.skeleton = skeleton;
        this.included = (BitSet) included.clone();
    }

    public static BoneMask all(Skeleton skeleton) {
        BitSet bits = new BitSet(skeleton.boneCount());
        bits.set(0, skeleton.boneCount());
        return new BoneMask(skeleton, bits);
    }

    public static Builder builder(Skeleton skeleton) {
        return new Builder(skeleton);
    }

    public Skeleton skeleton() { return skeleton; }
    public boolean includes(int boneIndex) { return included.get(boneIndex); }

    public static final class Builder {
        private final Skeleton skeleton;
        private final BitSet bits;

        private Builder(Skeleton skeleton) {
            this.skeleton = Objects.requireNonNull(skeleton, "skeleton");
            this.bits = new BitSet(skeleton.boneCount());
        }

        public Builder include(String boneName, boolean includeDescendants) {
            int root = skeleton.requireIndex(boneName);
            bits.set(root);
            if (includeDescendants) {
                for (int i = 0; i < skeleton.boneCount(); i++) {
                    if (skeleton.isDescendantOf(i, root)) bits.set(i);
                }
            }
            return this;
        }

        public Builder exclude(String boneName, boolean excludeDescendants) {
            int root = skeleton.requireIndex(boneName);
            bits.clear(root);
            if (excludeDescendants) {
                for (int i = 0; i < skeleton.boneCount(); i++) {
                    if (skeleton.isDescendantOf(i, root)) bits.clear(i);
                }
            }
            return this;
        }

        public BoneMask build() {
            return new BoneMask(skeleton, bits);
        }
    }
}
