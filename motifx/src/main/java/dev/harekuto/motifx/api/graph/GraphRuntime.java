package dev.harekuto.motifx.api.graph;

import dev.harekuto.motifx.api.animation.AnimationRuntime.Clip;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class GraphRuntime {
    private GraphRuntime() {}

    public static final class FloatKey {
        private final Object owner;
        private final String name;
        private final int index;
        private FloatKey(Object owner, String name, int index) { this.owner = owner; this.name = name; this.index = index; }
        public String name() { return name; }
    }

    public static final class BoolKey {
        private final Object owner;
        private final String name;
        private final int index;
        private BoolKey(Object owner, String name, int index) { this.owner = owner; this.name = name; this.index = index; }
        public String name() { return name; }
    }

    public static final class IntKey {
        private final Object owner;
        private final String name;
        private final int index;
        private IntKey(Object owner, String name, int index) { this.owner = owner; this.name = name; this.index = index; }
        public String name() { return name; }
    }

    public static final class ParameterSchema {
        private final Object owner;
        private final int floatCount;
        private final int boolCount;
        private final int intCount;

        private ParameterSchema(Object owner, int floatCount, int boolCount, int intCount) {
            this.owner = owner;
            this.floatCount = floatCount;
            this.boolCount = boolCount;
            this.intCount = intCount;
        }

        public Parameters create() { return new Parameters(owner, floatCount, boolCount, intCount); }

        public static final class Builder {
            private final Object owner = new Object();
            private final Set<String> names = new HashSet<>();
            private int floatCount;
            private int boolCount;
            private int intCount;

            public FloatKey floatKey(String name) {
                requireUnique(name);
                return new FloatKey(owner, name, floatCount++);
            }

            public BoolKey boolKey(String name) {
                requireUnique(name);
                return new BoolKey(owner, name, boolCount++);
            }

            public IntKey intKey(String name) {
                requireUnique(name);
                return new IntKey(owner, name, intCount++);
            }

            public ParameterSchema build() { return new ParameterSchema(owner, floatCount, boolCount, intCount); }

            private void requireUnique(String name) {
                if (name == null || name.isBlank()) throw new IllegalArgumentException("Parameter name must not be blank");
                if (!names.add(name)) throw new IllegalArgumentException("Duplicate parameter name: " + name);
            }
        }
    }

    public static final class Parameters {
        private final Object owner;
        private final float[] floats;
        private final boolean[] bools;
        private final int[] ints;

        private Parameters(Object owner, int floatCount, int boolCount, int intCount) {
            this.owner = owner;
            this.floats = new float[floatCount];
            this.bools = new boolean[boolCount];
            this.ints = new int[intCount];
        }

        public float get(FloatKey key) { require(key.owner); return floats[key.index]; }
        public boolean get(BoolKey key) { require(key.owner); return bools[key.index]; }
        public int get(IntKey key) { require(key.owner); return ints[key.index]; }
        public void set(FloatKey key, float value) { require(key.owner); floats[key.index] = Float.isFinite(value) ? value : 0f; }
        public void set(BoolKey key, boolean value) { require(key.owner); bools[key.index] = value; }
        public void set(IntKey key, int value) { require(key.owner); ints[key.index] = value; }

        private void require(Object keyOwner) {
            if (owner != keyOwner) throw new IllegalArgumentException("Parameter key belongs to another schema");
        }
    }

    @FunctionalInterface
    public interface Condition { boolean test(Parameters parameters); }

    public record State(String id, Clip clip) {
        public State {
            if (id == null || id.isBlank()) throw new IllegalArgumentException("State id must not be blank");
            Objects.requireNonNull(clip, "clip");
        }
    }

    private record Transition(int from, int to, Condition condition, float duration, int priority) {}

    public record GraphSample(Clip currentClip, float currentTime, Clip previousClip, float previousTime, float blendAlpha, String stateId) {
        public boolean transitioning() { return previousClip != null && blendAlpha < 1f; }
    }

    public static final class StateGraph {
        private final List<State> states;
        private final List<Transition> transitions;
        private final int initialState;

        private StateGraph(List<State> states, List<Transition> transitions, int initialState) {
            this.states = states;
            this.transitions = transitions;
            this.initialState = initialState;
        }

        public Instance createInstance() { return new Instance(this); }

        public static final class Builder {
            private final List<State> states = new ArrayList<>();
            private final Map<String, Integer> indices = new HashMap<>();
            private final List<PendingTransition> pending = new ArrayList<>();
            private String initial;

            public Builder state(String id, Clip clip) {
                if (indices.containsKey(id)) throw new IllegalArgumentException("Duplicate state: " + id);
                indices.put(id, states.size());
                states.add(new State(id, clip));
                if (initial == null) initial = id;
                return this;
            }

            public Builder initial(String id) { this.initial = id; return this; }

            public Builder transition(String from, String to, Condition condition, float duration, int priority) {
                pending.add(new PendingTransition(from, to, condition, duration, priority, false));
                return this;
            }

            public Builder anyTransition(String to, Condition condition, float duration, int priority) {
                pending.add(new PendingTransition(null, to, condition, duration, priority, true));
                return this;
            }

            public StateGraph build() {
                if (states.isEmpty()) throw new IllegalStateException("Graph must contain states");
                Integer initialIndex = indices.get(initial);
                if (initialIndex == null) throw new IllegalStateException("Unknown initial state: " + initial);
                List<Transition> transitions = new ArrayList<>(pending.size());
                for (PendingTransition p : pending) {
                    Integer to = indices.get(p.to);
                    if (to == null) throw new IllegalStateException("Unknown transition target: " + p.to);
                    int from = -1;
                    if (!p.any) {
                        Integer resolved = indices.get(p.from);
                        if (resolved == null) throw new IllegalStateException("Unknown transition source: " + p.from);
                        if (resolved.equals(to)) throw new IllegalStateException("Self transition is not allowed in 0.2: " + p.from);
                        from = resolved;
                    }
                    if (!Float.isFinite(p.duration) || p.duration < 0f) throw new IllegalStateException("Transition duration must be finite and >= 0");
                    transitions.add(new Transition(from, to, Objects.requireNonNull(p.condition, "condition"), p.duration, p.priority));
                }
                transitions.sort(Comparator.comparingInt(Transition::priority).reversed());
                return new StateGraph(List.copyOf(states), List.copyOf(transitions), initialIndex);
            }

            private record PendingTransition(String from, String to, Condition condition, float duration, int priority, boolean any) {}
        }
    }

    public static final class Instance {
        private final StateGraph graph;
        private int current;
        private int previous = -1;
        private float currentTime;
        private float previousTime;
        private float transitionTime;
        private float transitionDuration;

        private Instance(StateGraph graph) {
            this.graph = graph;
            this.current = graph.initialState;
        }

        public String stateId() { return graph.states.get(current).id(); }

        public GraphSample update(float deltaSeconds, Parameters parameters) {
            Objects.requireNonNull(parameters, "parameters");
            float dt = Float.isFinite(deltaSeconds) && deltaSeconds > 0f ? deltaSeconds : 0f;
            currentTime += dt;
            if (previous >= 0) {
                previousTime += dt;
                transitionTime += dt;
                if (transitionTime >= transitionDuration) previous = -1;
            }

            if (previous < 0) {
                for (Transition transition : graph.transitions) {
                    if ((transition.from == -1 || transition.from == current) && transition.to != current && transition.condition.test(parameters)) {
                        previous = current;
                        previousTime = currentTime;
                        current = transition.to;
                        currentTime = 0f;
                        transitionTime = 0f;
                        transitionDuration = transition.duration;
                        if (transitionDuration <= 0f) previous = -1;
                        break;
                    }
                }
            }

            State currentState = graph.states.get(current);
            if (previous < 0) return new GraphSample(currentState.clip(), currentTime, null, 0f, 1f, currentState.id());
            State previousState = graph.states.get(previous);
            float alpha = transitionDuration <= 0f ? 1f : Math.max(0f, Math.min(1f, transitionTime / transitionDuration));
            return new GraphSample(currentState.clip(), currentTime, previousState.clip(), previousTime, alpha, currentState.id());
        }
    }
}
