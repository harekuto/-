package dev.harekuto.motifx.api.math;

import java.util.Objects;

public final class MathTypes {
    private MathTypes() {}

    public record Vec3f(float x, float y, float z) {
        public static final Vec3f ZERO = new Vec3f(0f, 0f, 0f);
        public static final Vec3f ONE = new Vec3f(1f, 1f, 1f);

        public Vec3f add(Vec3f other) {
            Objects.requireNonNull(other, "other");
            return new Vec3f(x + other.x, y + other.y, z + other.z);
        }

        public Vec3f subtract(Vec3f other) {
            Objects.requireNonNull(other, "other");
            return new Vec3f(x - other.x, y - other.y, z - other.z);
        }

        public Vec3f multiply(float scalar) {
            return new Vec3f(x * scalar, y * scalar, z * scalar);
        }

        public boolean isFinite() {
            return Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z);
        }

        public static Vec3f lerp(Vec3f a, Vec3f b, float alpha) {
            Objects.requireNonNull(a, "a");
            Objects.requireNonNull(b, "b");
            float t = clamp01(alpha);
            return new Vec3f(
                a.x + (b.x - a.x) * t,
                a.y + (b.y - a.y) * t,
                a.z + (b.z - a.z) * t
            );
        }
    }

    public record Quatf(float x, float y, float z, float w) {
        public static final Quatf IDENTITY = new Quatf(0f, 0f, 0f, 1f);

        public Quatf normalized() {
            double lenSq = (double)x * x + (double)y * y + (double)z * z + (double)w * w;
            if (!Double.isFinite(lenSq) || lenSq < 1.0e-12) {
                return IDENTITY;
            }
            float inv = (float)(1.0 / Math.sqrt(lenSq));
            return new Quatf(x * inv, y * inv, z * inv, w * inv);
        }

        public Quatf conjugate() {
            return new Quatf(-x, -y, -z, w);
        }

        public Quatf inverseUnit() {
            return normalized().conjugate();
        }

        public Quatf multiply(Quatf other) {
            Objects.requireNonNull(other, "other");
            return new Quatf(
                w * other.x + x * other.w + y * other.z - z * other.y,
                w * other.y - x * other.z + y * other.w + z * other.x,
                w * other.z + x * other.y - y * other.x + z * other.w,
                w * other.w - x * other.x - y * other.y - z * other.z
            ).normalized();
        }

        public boolean isFinite() {
            return Float.isFinite(x) && Float.isFinite(y) && Float.isFinite(z) && Float.isFinite(w);
        }

        public static Quatf slerp(Quatf a, Quatf b, float alpha) {
            Quatf qa = Objects.requireNonNull(a, "a").normalized();
            Quatf qb = Objects.requireNonNull(b, "b").normalized();
            float t = clamp01(alpha);
            double dot = (double)qa.x * qb.x + (double)qa.y * qb.y + (double)qa.z * qb.z + (double)qa.w * qb.w;

            if (dot < 0.0) {
                qb = new Quatf(-qb.x, -qb.y, -qb.z, -qb.w);
                dot = -dot;
            }

            if (dot > 0.9995) {
                return new Quatf(
                    qa.x + (qb.x - qa.x) * t,
                    qa.y + (qb.y - qa.y) * t,
                    qa.z + (qb.z - qa.z) * t,
                    qa.w + (qb.w - qa.w) * t
                ).normalized();
            }

            double theta0 = Math.acos(Math.max(-1.0, Math.min(1.0, dot)));
            double sinTheta0 = Math.sin(theta0);
            if (Math.abs(sinTheta0) < 1.0e-8) {
                return qa;
            }
            double theta = theta0 * t;
            double s0 = Math.cos(theta) - dot * Math.sin(theta) / sinTheta0;
            double s1 = Math.sin(theta) / sinTheta0;
            return new Quatf(
                (float)(qa.x * s0 + qb.x * s1),
                (float)(qa.y * s0 + qb.y * s1),
                (float)(qa.z * s0 + qb.z * s1),
                (float)(qa.w * s0 + qb.w * s1)
            ).normalized();
        }

        public static Quatf fromEulerDegrees(float xDeg, float yDeg, float zDeg) {
            double x = Math.toRadians(xDeg) * 0.5;
            double y = Math.toRadians(yDeg) * 0.5;
            double z = Math.toRadians(zDeg) * 0.5;
            double cx = Math.cos(x), sx = Math.sin(x);
            double cy = Math.cos(y), sy = Math.sin(y);
            double cz = Math.cos(z), sz = Math.sin(z);
            return new Quatf(
                (float)(sx * cy * cz - cx * sy * sz),
                (float)(cx * sy * cz + sx * cy * sz),
                (float)(cx * cy * sz - sx * sy * cz),
                (float)(cx * cy * cz + sx * sy * sz)
            ).normalized();
        }
    }

    public record Transform(Vec3f translation, Quatf rotation, Vec3f scale) {
        public static final Transform IDENTITY = new Transform(Vec3f.ZERO, Quatf.IDENTITY, Vec3f.ONE);

        public Transform {
            Objects.requireNonNull(translation, "translation");
            Objects.requireNonNull(rotation, "rotation");
            Objects.requireNonNull(scale, "scale");
        }

        public boolean isFinite() {
            return translation.isFinite() && rotation.isFinite() && scale.isFinite();
        }

        public static Transform blend(Transform from, Transform to, float alpha) {
            return new Transform(
                Vec3f.lerp(from.translation, to.translation, alpha),
                Quatf.slerp(from.rotation, to.rotation, alpha),
                Vec3f.lerp(from.scale, to.scale, alpha)
            );
        }

        public static Transform applyAdditive(Transform current, Transform bind, Transform sample, float weight) {
            float w = clamp01(weight);
            Vec3f translationDelta = sample.translation.subtract(bind.translation).multiply(w);
            Vec3f scaleDelta = sample.scale.subtract(bind.scale).multiply(w);
            Quatf rotationDelta = bind.rotation.inverseUnit().multiply(sample.rotation);
            Quatf weightedRotation = Quatf.slerp(Quatf.IDENTITY, rotationDelta, w);
            return new Transform(
                current.translation.add(translationDelta),
                current.rotation.multiply(weightedRotation),
                current.scale.add(scaleDelta)
            );
        }
    }

    public static float clamp01(float value) {
        if (!Float.isFinite(value)) return 0f;
        return Math.max(0f, Math.min(1f, value));
    }
}
