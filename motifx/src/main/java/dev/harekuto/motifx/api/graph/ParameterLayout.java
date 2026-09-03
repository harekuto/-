package dev.harekuto.motifx.api.graph;

import java.util.HashSet;
import java.util.Set;

/** Build-time parameter registry. Runtime lookup uses integer indexes rather than Map<String,Object>. */
public final class ParameterLayout {
    public record FloatParam(String name, int index) {}
    public record BoolParam(String name, int index) {}

    private final int floatCount;
    private final int boolCount;

    private ParameterLayout(int floatCount, int boolCount) {
        this.floatCount = floatCount;
        this.boolCount = boolCount;
    }

    public static Builder builder() { return new Builder(); }
    public int floatCount() { return floatCount; }
    public int boolCount() { return boolCount; }

    public static final class Builder {
        private final Set<String> names = new HashSet<>();
        private int floats;
        private int bools;

        public FloatParam floatParam(String name) {
            requireUnique(name);
            return new FloatParam(name, floats++);
        }

        public BoolParam boolParam(String name) {
            requireUnique(name);
            return new BoolParam(name, bools++);
        }

        public ParameterLayout build() {
            return new ParameterLayout(floats, bools);
        }

        private void requireUnique(String name) {
            if (name == null || name.isBlank()) throw new IllegalArgumentException("Parameter name cannot be blank");
            if (!names.add(name)) throw new IllegalArgumentException("Duplicate MotifX parameter: " + name);
        }
    }
}
