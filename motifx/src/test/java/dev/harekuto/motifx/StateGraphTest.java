package dev.harekuto.motifx;

import dev.harekuto.motifx.api.animation.AnimationClip;
import dev.harekuto.motifx.api.animation.LoopMode;
import dev.harekuto.motifx.api.animation.Pose;
import dev.harekuto.motifx.api.animation.Skeleton;
import dev.harekuto.motifx.api.animation.Transform;
import dev.harekuto.motifx.api.graph.AnimationStateGraph;
import dev.harekuto.motifx.api.graph.ParameterLayout;
import dev.harekuto.motifx.api.graph.ParameterStore;
import dev.harekuto.motifx.api.graph.StateGraphPlayer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StateGraphTest {
    @Test
    void typedBooleanParameterStartsCrossFade() {
        Skeleton skeleton = Skeleton.builder().addRoot("root", Transform.IDENTITY).build();
        AnimationClip idle = new AnimationClip("idle", 1, LoopMode.LOOP, Map.of(), List.of());
        AnimationClip active = new AnimationClip("active", 1, LoopMode.LOOP, Map.of(), List.of());
        ParameterLayout.Builder layoutBuilder = ParameterLayout.builder();
        ParameterLayout.BoolParam enabled = layoutBuilder.boolParam("enabled");
        ParameterLayout layout = layoutBuilder.build();
        ParameterStore store = new ParameterStore(layout);
        AnimationStateGraph graph = AnimationStateGraph.builder()
                .state("idle", idle)
                .state("active", active)
                .transition("idle", "active", 0.2f, 10, p -> p.get(enabled))
                .build();
        StateGraphPlayer player = new StateGraphPlayer(graph, store, skeleton);
        store.set(enabled, true);
        player.update(0.05f);
        assertTrue(player.transitioning());
        player.update(0.2f);
        player.sample(new Pose(skeleton));
        assertEquals("active", player.currentStateName());
    }
}
