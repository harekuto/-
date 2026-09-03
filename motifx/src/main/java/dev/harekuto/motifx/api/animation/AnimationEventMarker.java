package dev.harekuto.motifx.api.animation;

import java.util.Objects;

public record AnimationEventMarker(String id, float time) {
    public AnimationEventMarker {
        Objects.requireNonNull(id, "id");
        if (id.isBlank()) throw new IllegalArgumentException("Animation event id cannot be blank");
        if (!Float.isFinite(time)) throw new IllegalArgumentException("Animation event time must be finite");
    }
}
