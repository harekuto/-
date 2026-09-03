package dev.harekuto.motifx.runtime;

import java.util.concurrent.atomic.AtomicLong;

public final class RuntimeMetrics {
    private final AtomicLong poseEvaluations = new AtomicLong();
    private final AtomicLong graphUpdates = new AtomicLong();
    private final AtomicLong compatibilityPasses = new AtomicLong();
    private final AtomicLong validations = new AtomicLong();
    private final AtomicLong validationErrors = new AtomicLong();

    public void poseEvaluated() { poseEvaluations.incrementAndGet(); }
    public void graphUpdated() { graphUpdates.incrementAndGet(); }
    public void compatibilityPass() { compatibilityPasses.incrementAndGet(); }
    public void validation(long errors) { validations.incrementAndGet(); validationErrors.addAndGet(Math.max(0L, errors)); }

    public Snapshot snapshot() {
        return new Snapshot(poseEvaluations.get(), graphUpdates.get(), compatibilityPasses.get(), validations.get(), validationErrors.get());
    }

    public record Snapshot(long poseEvaluations, long graphUpdates, long compatibilityPasses, long validations, long validationErrors) {}
}
