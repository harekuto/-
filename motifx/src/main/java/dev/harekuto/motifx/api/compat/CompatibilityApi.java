package dev.harekuto.motifx.api.compat;

import dev.harekuto.motifx.api.animation.AnimationModel.Pose;
import dev.harekuto.motifx.api.animation.AnimationModel.Skeleton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public final class CompatibilityApi {
    private CompatibilityApi() {}

    public enum Capability {
        SKELETAL_POSE,
        PLAYER_POSE,
        ITEM_POSE,
        ARMOR_POSE,
        BLOCK_ENTITY_POSE,
        EVENT_MARKERS,
        VFX_ATTACHMENTS,
        ASSET_IMPORT,
        RUNTIME_INSPECTION
    }

    public interface Adapter {
        String id();
        int priority();
        boolean supportsMod(String modId);
        Set<Capability> capabilities();
    }

    public static final class Registry {
        private final CopyOnWriteArrayList<Adapter> adapters = new CopyOnWriteArrayList<>();

        public void register(Adapter adapter) {
            Objects.requireNonNull(adapter, "adapter");
            if (adapter.id() == null || adapter.id().isBlank()) throw new IllegalArgumentException("Adapter id must not be blank");
            for (Adapter existing : adapters) if (existing.id().equals(adapter.id())) throw new IllegalArgumentException("Duplicate adapter id: " + adapter.id());
            adapters.add(adapter);
            adapters.sort(Comparator.comparingInt(Adapter::priority).reversed());
        }

        public List<Adapter> resolve(String modId) {
            if (modId == null || modId.isBlank()) return List.of();
            List<Adapter> result = new ArrayList<>();
            for (Adapter adapter : adapters) if (adapter.supportsMod(modId)) result.add(adapter);
            return List.copyOf(result);
        }

        public List<Adapter> snapshot() { return List.copyOf(adapters); }
    }

    public record PoseContext(Skeleton skeleton, Pose pose, float deltaSeconds, Object subject) {
        public PoseContext {
            Objects.requireNonNull(skeleton, "skeleton");
            Objects.requireNonNull(pose, "pose");
            if (pose.skeleton() != skeleton) throw new IllegalArgumentException("Pose skeleton mismatch");
        }
    }

    public interface PoseContributor {
        String id();
        int priority();
        void apply(PoseContext context);
    }

    public static final class PosePipeline {
        private final CopyOnWriteArrayList<PoseContributor> contributors = new CopyOnWriteArrayList<>();

        public void register(PoseContributor contributor) {
            Objects.requireNonNull(contributor, "contributor");
            if (contributor.id() == null || contributor.id().isBlank()) throw new IllegalArgumentException("Contributor id must not be blank");
            for (PoseContributor existing : contributors) if (existing.id().equals(contributor.id())) throw new IllegalArgumentException("Duplicate contributor id: " + contributor.id());
            contributors.add(contributor);
            contributors.sort(Comparator.comparingInt(PoseContributor::priority));
        }

        public boolean unregister(String id) { return contributors.removeIf(contributor -> contributor.id().equals(id)); }
        public List<PoseContributor> snapshot() { return List.copyOf(contributors); }

        public void apply(PoseContext context) {
            for (PoseContributor contributor : contributors) contributor.apply(context);
        }
    }
}
