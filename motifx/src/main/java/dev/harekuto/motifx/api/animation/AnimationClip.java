package dev.harekuto.motifx.api.animation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/** Immutable compiled animation clip. */
public final class AnimationClip {
    private static final int MAX_EVENT_CYCLES_PER_ADVANCE = 8;

    private final String name;
    private final float duration;
    private final LoopMode loopMode;
    private final Map<Integer, BoneTrack> tracks;
    private final List<AnimationEventMarker> events;

    public AnimationClip(String name, float duration, LoopMode loopMode,
                         Map<Integer, BoneTrack> tracks, List<AnimationEventMarker> events) {
        this.name = Objects.requireNonNull(name, "name");
        if (name.isBlank()) throw new IllegalArgumentException("Animation name cannot be blank");
        if (!Float.isFinite(duration) || duration <= 0.0f) {
            throw new IllegalArgumentException("Animation duration must be finite and > 0");
        }
        this.duration = duration;
        this.loopMode = Objects.requireNonNull(loopMode, "loopMode");
        this.tracks = Map.copyOf(tracks == null ? Map.of() : tracks);
        List<AnimationEventMarker> sorted = new ArrayList<>(events == null ? List.of() : events);
        sorted.sort(Comparator.comparingDouble(AnimationEventMarker::time));
        this.events = List.copyOf(sorted);
    }

    public String name() { return name; }
    public float duration() { return duration; }
    public LoopMode loopMode() { return loopMode; }
    public Map<Integer, BoneTrack> tracks() { return tracks; }
    public List<AnimationEventMarker> events() { return events; }

    public float sampleTime(float rawTime) {
        if (!Float.isFinite(rawTime)) return 0.0f;
        return switch (loopMode) {
            case ONCE, HOLD -> Math.max(0.0f, Math.min(duration, rawTime));
            case LOOP -> positiveModulo(rawTime, duration);
            case PING_PONG -> {
                float cycle = positiveModulo(rawTime, duration * 2.0f);
                yield cycle <= duration ? cycle : duration * 2.0f - cycle;
            }
        };
    }

    public void sample(Skeleton skeleton, float rawTime, Pose output) {
        if (output.skeleton() != skeleton) {
            throw new IllegalArgumentException("Output pose belongs to another skeleton");
        }
        float time = sampleTime(rawTime);
        output.resetToBindPose();
        for (Map.Entry<Integer, BoneTrack> entry : tracks.entrySet()) {
            int bone = entry.getKey();
            if (bone >= 0 && bone < skeleton.boneCount()) {
                output.set(bone, entry.getValue().sample(time, skeleton.bone(bone).bindPose()));
            }
        }
    }

    /** Dispatches markers for forward playback. Large time jumps are bounded to the newest eight cycles. */
    public void dispatchForwardMarkers(float previousRawTime, float currentRawTime,
                                       Consumer<AnimationEventMarker> consumer) {
        Objects.requireNonNull(consumer, "consumer");
        if (events.isEmpty() || !Float.isFinite(previousRawTime) || !Float.isFinite(currentRawTime)
                || currentRawTime <= previousRawTime) return;

        switch (loopMode) {
            case ONCE, HOLD -> dispatchRange(Math.max(0.0f, previousRawTime), Math.min(duration, currentRawTime), consumer);
            case LOOP -> dispatchLoopMarkers(previousRawTime, currentRawTime, consumer);
            case PING_PONG -> dispatchPingPongMarkers(previousRawTime, currentRawTime, consumer);
        }
    }

    private void dispatchLoopMarkers(float previous, float current, Consumer<AnimationEventMarker> consumer) {
        long firstCycle = (long) Math.floor(previous / duration);
        long lastCycle = (long) Math.floor(current / duration);
        firstCycle = boundedFirstCycle(firstCycle, lastCycle);
        for (long cycle = firstCycle; cycle <= lastCycle; cycle++) {
            double base = cycle * (double) duration;
            for (AnimationEventMarker event : events) {
                double absolute = base + event.time();
                if (absolute > previous && absolute <= current) consumer.accept(event);
            }
        }
    }

    private void dispatchPingPongMarkers(float previous, float current, Consumer<AnimationEventMarker> consumer) {
        double cycleLength = duration * 2.0;
        long firstCycle = (long) Math.floor(previous / cycleLength);
        long lastCycle = (long) Math.floor(current / cycleLength);
        firstCycle = boundedFirstCycle(firstCycle, lastCycle);
        for (long cycle = firstCycle; cycle <= lastCycle; cycle++) {
            double base = cycle * cycleLength;
            for (AnimationEventMarker event : events) {
                double forward = base + event.time();
                if (forward > previous && forward <= current) consumer.accept(event);
                if (event.time() > 0.0f && event.time() < duration) {
                    double backward = base + cycleLength - event.time();
                    if (backward > previous && backward <= current) consumer.accept(event);
                }
            }
        }
    }

    private long boundedFirstCycle(long firstCycle, long lastCycle) {
        if (lastCycle - firstCycle >= MAX_EVENT_CYCLES_PER_ADVANCE) {
            return lastCycle - MAX_EVENT_CYCLES_PER_ADVANCE + 1;
        }
        return firstCycle;
    }

    private void dispatchRange(float startExclusive, float endInclusive, Consumer<AnimationEventMarker> consumer) {
        if (endInclusive <= startExclusive) return;
        for (AnimationEventMarker event : events) {
            if (event.time() > startExclusive && event.time() <= endInclusive) consumer.accept(event);
        }
    }

    private static float positiveModulo(float value, float modulus) {
        float result = value % modulus;
        return result < 0.0f ? result + modulus : result;
    }
}
