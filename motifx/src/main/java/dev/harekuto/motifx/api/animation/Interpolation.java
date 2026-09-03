package dev.harekuto.motifx.api.animation;

public enum Interpolation {
    STEP {
        @Override
        public float apply(float alpha) {
            return 0.0f;
        }
    },
    LINEAR {
        @Override
        public float apply(float alpha) {
            return clamp(alpha);
        }
    },
    SMOOTHSTEP {
        @Override
        public float apply(float alpha) {
            float t = clamp(alpha);
            return t * t * (3.0f - 2.0f * t);
        }
    };

    public abstract float apply(float alpha);

    private static float clamp(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
