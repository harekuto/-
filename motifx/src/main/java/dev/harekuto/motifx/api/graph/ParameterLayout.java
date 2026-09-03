package dev.harekuto.motifx.api.graph;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/** Build-time parameter registry. Runtime lookup uses integer indexes rather than Map<String,Object>. */
public final class ParameterLayout {
    private static final AtomicInteger NEXT_LAYOUT_ID = new AtomicInteger(1);

    public record FloatParam(String name, int index, int layoutId) {}
    public record BoolParam(String name, int index, int layoutId) {}

    private final int id;
    private final int floatCount;
    private final int boolCount;

    private ParameterLayout(int id, int floatCount, int boolCount) {
        this.id = id;
        this.floatCount = floatCount;
        this.boolCount = boolCount;
    }

    public static Builder builder() { return new Builder(); }
    int id() { return id; }
    public int floatCount() { return floatCount; }
    public int boolCount() { return boolCount; }

    public static final class Builder {
        private final int layoutId = NEXT_LAYOUT_ID.getAndIncrement();
        private final Set<String> names = new HashSet<>();
        private int floats;
        private int bools;
        private boolean built;

        public FloatParam floatParam(String name) {
            requireOpen();
            requireUnique(name);
            return new FloatParam(name, floats++, layoutId);
        }

        public BoolParam boolParam(String name) {
            requireOpen();
            requireUnique(name);
            return new BoolParam(name, bools++, layoutId);
        }

        public ParameterLayout build() {
            requireOpen();
            built = true;
            return new ParameterLayout(layoutId, floats, bools);
        }

        private void requireOpen() {
            if (built) throw new IllegalStateException("ParameterLayout.Builder cannot be reused after build()");
        }

        private void requireUnique(String name) {
            if (name == null || name.isBlank()) throw new IllegalArgumentException("Parameter name cannot be blank");
            if (!names.add(name)) throw new IllegalArgumentException("Duplicate MotifX parameter: " + name);
        }
    }
}
