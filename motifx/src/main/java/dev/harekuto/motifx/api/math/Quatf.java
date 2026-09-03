package dev.harekuto.motifx.api.math;

/** Immutable quaternion with numerically guarded normalization and spherical interpolation. */
public record Quatf(float x, float y, float z, float w) {
    public static final Quatf IDENTITY = new Quatf(0.0f, 0.0f, 0.0f, 1.0f);
    private static final float EPSILON = 1.0e-8f;

    public static Quatf fromEulerDegrees(float xDegrees, float yDegrees, float zDegrees) {
        Quatf qx = axisAngle(1.0f, 0.0f, 0.0f, xDegrees);
        Quatf qy = axisAngle(0.0f, 1.0f, 0.0f, yDegrees);
        Quatf qz = axisAngle(0.0f, 0.0f, 1.0f, zDegrees);
        return qz.multiply(qy).multiply(qx).normalized();
    }

    private static Quatf axisAngle(float ax, float ay, float az, float degrees) {
        double half = Math.toRadians(degrees) * 0.5;
        float sin = (float) Math.sin(half);
        return new Quatf(ax * sin, ay * sin, az * sin, (float) Math.cos(half));
    }

    public Quatf multiply(Quatf other) {
        return new Quatf(
                w * other.x + x * other.w + y * other.z - z * other.y,
                w * other.y - x * other.z + y * other.w + z * other.x,
                w * other.z + x * other.y - y * other.x + z * other.w,
                w * other.w - x * other.x - y * other.y - z * other.z
        );
    }

    public float dot(Quatf other) {
        return x * other.x + y * other.y + z * other.z + w * other.w;
    }

    public Quatf normalized() {
        if (!isFinite()) return IDENTITY;
        float lengthSquared = lengthSquared();
        if (lengthSquared < EPSILON) return IDENTITY;
        float inv = 1.0f / (float) Math.sqrt(lengthSquared);
        return new Quatf(x * inv, y * inv, z * inv, w * inv);
    }

    public Quatf conjugated() {
        return new Quatf(-x, -y, -z, w);
    }

    public Quatf inverse() {
        if (!isFinite()) return IDENTITY;
        float lengthSquared = lengthSquared();
        if (lengthSquared < EPSILON) return IDENTITY;
        Quatf conjugate = conjugated();
        float inv = 1.0f / lengthSquared;
        return new Quatf(conjugate.x * inv, conjugate.y * inv, conjugate.z * inv, conjugate.w * inv);
    }

    public Quatf negated() {
        return new Quatf(-x, -y, -z, -w);
    }

    public Quatf slerp(Quatf target, float alpha) {
        float t = Vec3f.clamp01(alpha);
        Quatf a = normalized();
        Quatf b = target.normalized();
        float dot = a.dot(b);

        if (dot < 0.0f) {
            b = b.negated();
            dot = -dot;
        }

        if (dot > 0.9995f) {
            return new Quatf(
                    a.x + (b.x - a.x) * t,
                    a.y + (b.y - a.y) * t,
                    a.z + (b.z - a.z) * t,
                    a.w + (b.w - a.w) * t
            ).normalized();
        }

        dot = Math.max(-1.0f, Math.min(1.0f, dot));
        double theta0 = Math.acos(dot);
        double theta = theta0 * t;
        double sinTheta = Math.sin(theta);
        double sinTheta0 = Math.sin(theta0);
        float s0 = (float) (Math.cos(theta) - dot * sinTheta / sinTheta0);
        float s1 = (float) (sinTheta / sinTheta0);

        return new Quatf(
                a.x * s0 + b.x * s1,
                a.y * s0 + b.y * s1,
                a.z * s0 + b.z * s1,
                a.w * s0 + b.w * s1
        ).normalized();
    }

    public boolean isFinite() {
        return Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z) && Float.isFinite(w);
    }

    private float lengthSquared() {
        return x * x + y * y + z * z + w * w;
    }
}
