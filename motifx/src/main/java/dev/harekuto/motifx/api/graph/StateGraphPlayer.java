package dev.harekuto.motifx.api.graph;

import dev.harekuto.motifx.api.animation.BoneMask;
import dev.harekuto.motifx.api.animation.Pose;
import dev.harekuto.motifx.api.animation.Skeleton;

import java.util.Objects;

/** Stateful graph evaluator with reusable transition poses and deterministic priority ordering. */
public final class StateGraphPlayer {
    private final AnimationStateGraph graph;
    private final ParameterStore parameters;
    private final Skeleton skeleton;
    private final Pose sourcePose;
    private final Pose targetPose;
    private final BoneMask fullMask;

    private int currentState;
    private float stateTime;
    private AnimationStateGraph.Transition activeTransition;
    private float transitionElapsed;
    private float sourceTime;
    private float targetTime;

    public StateGraphPlayer(AnimationStateGraph graph, ParameterStore parameters, Skeleton skeleton) {
        this.graph = Objects.requireNonNull(graph, "graph");
        this.parameters = Objects.requireNonNull(parameters, "parameters");
        this.skeleton = Objects.requireNonNull(skeleton, "skeleton");
        this.currentState = graph.initialState();
        this.sourcePose = new Pose(skeleton);
        this.targetPose = new Pose(skeleton);
        this.fullMask = BoneMask.all(skeleton);
    }

    public ParameterStore parameters() { return parameters; }
    public String currentStateName() { return graph.state(currentState).name(); }
    public boolean transitioning() { return activeTransition != null; }
    public float transitionProgress() {
        if (activeTransition == null) return 0.0f;
        if (activeTransition.duration() <= 0.0f) return 1.0f;
        return Math.max(0.0f, Math.min(1.0f, transitionElapsed / activeTransition.duration()));
    }

    public void update(float deltaSeconds) {
        if (!Float.isFinite(deltaSeconds) || deltaSeconds <= 0.0f) return;

        if (activeTransition != null) {
            sourceTime += deltaSeconds;
            targetTime += deltaSeconds;
            transitionElapsed += deltaSeconds;
            if (transitionElapsed >= activeTransition.duration()) {
                currentState = activeTransition.target();
                stateTime = targetTime;
                activeTransition = null;
                transitionElapsed = 0.0f;
            }
            return;
        }

        stateTime += deltaSeconds;
        for (AnimationStateGraph.Transition transition : graph.outgoing(currentState)) {
            if (!transition.condition().test(parameters)) continue;
            if (transition.duration() <= 0.0f) {
                currentState = transition.target();
                stateTime = 0.0f;
            } else {
                activeTransition = transition;
                transitionElapsed = 0.0f;
                sourceTime = stateTime;
                targetTime = 0.0f;
            }
            break;
        }
    }

    public void sample(Pose output) {
        if (output.skeleton() != skeleton) throw new IllegalArgumentException("Output pose belongs to another skeleton");
        if (activeTransition == null) {
            graph.state(currentState).clip().sample(skeleton, stateTime, output);
            return;
        }
        graph.state(activeTransition.source()).clip().sample(skeleton, sourceTime, sourcePose);
        graph.state(activeTransition.target()).clip().sample(skeleton, targetTime, targetPose);
        output.blendFrom(sourcePose, targetPose, transitionProgress(), fullMask);
    }
}
