package dev.harekuto.motifx;

import dev.harekuto.motifx.api.math.Quatf;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QuatfTest {
    @Test
    void slerpRemainsNormalizedAndFinite() {
        Quatf half = Quatf.IDENTITY.slerp(Quatf.fromEulerDegrees(0, 180, 0), 0.5f);
        float length = (float) Math.sqrt(half.x() * half.x() + half.y() * half.y() + half.z() * half.z() + half.w() * half.w());
        assertTrue(half.isFinite());
        assertEquals(1.0f, length, 1.0e-4f);
    }
}
