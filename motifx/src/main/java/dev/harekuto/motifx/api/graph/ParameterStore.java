package dev.harekuto.motifx.api.graph;

import java.util.Objects;

public final class ParameterStore {
    private final ParameterLayout layout;
    private final float[] floats;
    private final boolean[] bools;

    public ParameterStore(ParameterLayout layout) {
        this.layout = Objects.requireNonNull(layout, "layout");
        this.floats = new float[layout.floatCount()];
        this.bools = new boolean[layout.boolCount()];
    }

    public ParameterLayout layout() { return layout; }

    public float get(ParameterLayout.FloatParam parameter) {
        return floats[parameter.index()];
    }

    public void set(ParameterLayout.FloatParam parameter, float value) {
        if (!Float.isFinite(value)) throw new IllegalArgumentException("Float parameter must be finite: " + parameter.name());
        floats[parameter.index()] = value;
    }

    public boolean get(ParameterLayout.BoolParam parameter) {
        return bools[parameter.index()];
    }

    public void set(ParameterLayout.BoolParam parameter, boolean value) {
        bools[parameter.index()] = value;
    }
}
