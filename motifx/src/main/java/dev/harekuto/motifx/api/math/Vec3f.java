package dev.harekuto.motifx.api.math;

/** Lightweight immutable 3D vector used by MotifX's loader-independent runtime. */
public record Vec3f(float x, float y, float z) {
    public static final Vec3f ZERO = new Vec3f(0.0f, 0.0f, 0.0f);
    public static final Vec3f ONE = new Vec3f(1.0f, 1.0f, 1.0f);

    public Vec3f lerp(Vec3f other, float alpha) {
        float t = clamp01(alpha);
        return new Vec3f(
                x + (other.x - x) * t,
                y + (other.y - y) * t,
                z + (other.z - z) * t
        );
    }

    public Vec3f add(Vec3f other) {
        return new Vec3f(x + other.x, y + other.y, z + other.z);
    }

    public Vec3f multiply(float scalar) {
        return new Vec3f(x * scalar, y * scalar, z * scalar);
    }

    public boolean isFinite() {
        return Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z);
    }

    public static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
