package dev.harekuto.motifx.api.animation;

import dev.harekuto.motifx.api.math.Quatf;
import dev.harekuto.motifx.api.math.Vec3f;

import java.util.Objects;

public record Transform(Vec3f translation, Quatf rotation, Vec3f scale) {
    public static final Transform IDENTITY = new Transform(Vec3f.ZERO, Quatf.IDENTITY, Vec3f.ONE);

    public Transform {
        Objects.requireNonNull(translation, "translation");
        Objects.requireNonNull(rotation, "rotation");
        Objects.requireNonNull(scale, "scale");
    }

    public Transform blend(Transform other, float alpha) {
        return new Transform(
                translation.lerp(other.translation, alpha),
                rotation.slerp(other.rotation, alpha),
                scale.lerp(other.scale, alpha)
        );
    }

    public boolean isFinite() {
        return translation.isFinite() && rotation.isFinite() && scale.isFinite();
    }
}
