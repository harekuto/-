package dev.harekuto.motifx;

import dev.harekuto.motifx.api.animation.AnimationClip;
import dev.harekuto.motifx.api.animation.AnimationEventMarker;
import dev.harekuto.motifx.api.animation.LoopMode;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AnimationEventTest {
    @Test
    void loopBoundaryDispatchesZeroTimeMarkerOnNextCycle() {
        AnimationEventMarker zero = new AnimationEventMarker("zero", 0.0f);
        AnimationClip clip = new AnimationClip("loop", 1.0f, LoopMode.LOOP, Map.of(), List.of(zero));
        List<String> events = new ArrayList<>();
        clip.dispatchForwardMarkers(0.9f, 1.1f, event -> events.add(event.id()));
        assertEquals(List.of("zero"), events);
    }

    @Test
    void pingPongDispatchesInteriorMarkerOnForwardAndBackwardPass() {
        AnimationEventMarker middle = new AnimationEventMarker("middle", 0.25f);
        AnimationClip clip = new AnimationClip("ping", 1.0f, LoopMode.PING_PONG, Map.of(), List.of(middle));
        List<String> events = new ArrayList<>();
        clip.dispatchForwardMarkers(0.0f, 2.0f, event -> events.add(event.id()));
        assertEquals(List.of("middle", "middle"), events);
    }
}
