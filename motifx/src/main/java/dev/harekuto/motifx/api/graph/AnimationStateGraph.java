package dev.harekuto.motifx.api.graph;

import dev.harekuto.motifx.api.animation.AnimationClip;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Immutable compiled state graph. Names are resolved to indexes during build. */
public final class AnimationStateGraph {
    @FunctionalInterface
    public interface Condition {
        boolean test(ParameterStore parameters);
    }

    public record State(String name, AnimationClip clip) {}
    public record Transition(int source, int target, float duration, int priority, Condition condition) {}

    private final List<State> states;
    private final List<List<Transition>> outgoing;
    private final int initialState;

    private AnimationStateGraph(List<State> states, List<List<Transition>> outgoing, int initialState) {
        this.states = List.copyOf(states);
        this.outgoing = outgoing.stream().map(List::copyOf).toList();
        this.initialState = initialState;
    }

    public static Builder builder() { return new Builder(); }
    public State state(int index) { return states.get(index); }
    public int stateCount() { return states.size(); }
    public int initialState() { return initialState; }
    public List<Transition> outgoing(int stateIndex) { return outgoing.get(stateIndex); }

    public static final class Builder {
        private record PendingTransition(String source, String target, float duration, int priority, Condition condition) {}

        private final List<State> states = new ArrayList<>();
        private final Map<String, Integer> indexByName = new HashMap<>();
        private final List<PendingTransition> transitions = new ArrayList<>();
        private String initialName;

        public Builder state(String name, AnimationClip clip) {
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(clip, "clip");
            if (name.isBlank()) throw new IllegalArgumentException("State name cannot be blank");
            if (indexByName.containsKey(name)) throw new IllegalArgumentException("Duplicate state: " + name);
            indexByName.put(name, states.size());
            states.add(new State(name, clip));
            if (initialName == null) initialName = name;
            return this;
        }

        public Builder initial(String name) {
            initialName = Objects.requireNonNull(name, "name");
            return this;
        }

        public Builder transition(String source, String target, float duration, int priority, Condition condition) {
            if (!Float.isFinite(duration) || duration < 0.0f) throw new IllegalArgumentException("Transition duration must be finite and >= 0");
            transitions.add(new PendingTransition(source, target, duration, priority, Objects.requireNonNull(condition, "condition")));
            return this;
        }

        public AnimationStateGraph build() {
            if (states.isEmpty()) throw new IllegalStateException("State graph requires at least one state");
            Integer initial = indexByName.get(initialName);
            if (initial == null) throw new IllegalStateException("Unknown initial state: " + initialName);

            List<List<Transition>> outgoing = new ArrayList<>(states.size());
            for (int i = 0; i < states.size(); i++) outgoing.add(new ArrayList<>());

            for (PendingTransition pending : transitions) {
                Integer source = indexByName.get(pending.source());
                Integer target = indexByName.get(pending.target());
                if (source == null) throw new IllegalStateException("Unknown transition source: " + pending.source());
                if (target == null) throw new IllegalStateException("Unknown transition target: " + pending.target());
                outgoing.get(source).add(new Transition(source, target, pending.duration(), pending.priority(), pending.condition()));
            }
            outgoing.forEach(list -> list.sort(Comparator.comparingInt(Transition::priority).reversed()));
            return new AnimationStateGraph(states, outgoing, initial);
        }
    }
}
