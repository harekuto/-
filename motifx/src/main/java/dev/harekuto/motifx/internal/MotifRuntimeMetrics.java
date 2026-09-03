package dev.harekuto.motifx.internal;

import java.util.concurrent.atomic.AtomicLong;

public final class MotifRuntimeMetrics {
    public record Snapshot(long evaluations, long totalEvaluationNanos, long graphTransitions, long assetFailures) {
        public double averageEvaluationMicros() {
            return evaluations == 0 ? 0.0 : (totalEvaluationNanos / 1000.0) / evaluations;
        }
    }

    private static final AtomicLong EVALUATIONS = new AtomicLong();
    private static final AtomicLong EVALUATION_NANOS = new AtomicLong();
    private static final AtomicLong GRAPH_TRANSITIONS = new AtomicLong();
    private static final AtomicLong ASSET_FAILURES = new AtomicLong();

    private MotifRuntimeMetrics() {}

    public static void recordEvaluation(long nanos) {
        EVALUATIONS.incrementAndGet();
        EVALUATION_NANOS.addAndGet(Math.max(0L, nanos));
    }

    public static void recordTransition() { GRAPH_TRANSITIONS.incrementAndGet(); }
    public static void recordAssetFailure() { ASSET_FAILURES.incrementAndGet(); }

    public static Snapshot snapshot() {
        return new Snapshot(EVALUATIONS.get(), EVALUATION_NANOS.get(), GRAPH_TRANSITIONS.get(), ASSET_FAILURES.get());
    }
}
