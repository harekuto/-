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
        requireLayout(parameter.layoutId(), parameter.name());
        return floats[parameter.index()];
    }

    public void set(ParameterLayout.FloatParam parameter, float value) {
        requireLayout(parameter.layoutId(), parameter.name());
        if (!Float.isFinite(value)) throw new IllegalArgumentException("Float parameter must be finite: " + parameter.name());
        floats[parameter.index()] = value;
    }

    public boolean get(ParameterLayout.BoolParam parameter) {
        requireLayout(parameter.layoutId(), parameter.name());
        return bools[parameter.index()];
    }

    public void set(ParameterLayout.BoolParam parameter, boolean value) {
        requireLayout(parameter.layoutId(), parameter.name());
        bools[parameter.index()] = value;
    }

    private void requireLayout(int layoutId, String parameterName) {
        if (layoutId != layout.id()) {
            throw new IllegalArgumentException("Parameter '" + parameterName + "' belongs to another ParameterLayout");
        }
    }
}
