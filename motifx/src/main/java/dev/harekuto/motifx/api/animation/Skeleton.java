package dev.harekuto.motifx.api.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable indexed skeleton. Parent bones must be declared before their children. */
public final class Skeleton {
    public record Bone(String name, int parentIndex, Transform bindPose) {}

    private final List<Bone> bones;
    private final Map<String, Integer> indexByName;

    private Skeleton(List<Bone> bones, Map<String, Integer> indexByName) {
        this.bones = List.copyOf(bones);
        this.indexByName = Map.copyOf(indexByName);
    }

    public static Builder builder() {
        return new Builder();
    }

    public int boneCount() {
        return bones.size();
    }

    public Bone bone(int index) {
        return bones.get(index);
    }

    public int indexOf(String name) {
        Integer index = indexByName.get(name);
        return index == null ? -1 : index;
    }

    public int requireIndex(String name) {
        int index = indexOf(name);
        if (index < 0) {
            throw new IllegalArgumentException("Unknown MotifX bone: " + name);
        }
        return index;
    }

    public boolean isDescendantOf(int candidate, int ancestor) {
        if (candidate < 0 || candidate >= bones.size() || ancestor < 0 || ancestor >= bones.size()) {
            return false;
        }
        int current = candidate;
        while (current >= 0) {
            if (current == ancestor) {
                return true;
            }
            current = bones.get(current).parentIndex();
        }
        return false;
    }

    public static final class Builder {
        private final List<Bone> bones = new ArrayList<>();
        private final Map<String, Integer> indexByName = new HashMap<>();

        public Builder addRoot(String name, Transform bindPose) {
            return add(name, null, bindPose);
        }

        public Builder add(String name, String parentName, Transform bindPose) {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(bindPose, "bindPose");
            if (name.isBlank()) {
                throw new IllegalArgumentException("Bone name cannot be blank");
            }
            if (indexByName.containsKey(name)) {
                throw new IllegalArgumentException("Duplicate MotifX bone: " + name);
            }
            int parentIndex = -1;
            if (parentName != null) {
                Integer resolved = indexByName.get(parentName);
                if (resolved == null) {
                    throw new IllegalArgumentException("Parent bone must be declared first: " + parentName);
                }
                parentIndex = resolved;
            }
            int index = bones.size();
            bones.add(new Bone(name, parentIndex, bindPose));
            indexByName.put(name, index);
            return this;
        }

        public Skeleton build() {
            if (bones.isEmpty()) {
                throw new IllegalStateException("A MotifX skeleton requires at least one bone");
            }
            return new Skeleton(bones, indexByName);
        }
    }
}
