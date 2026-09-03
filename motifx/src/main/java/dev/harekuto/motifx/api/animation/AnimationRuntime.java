package dev.harekuto.motifx.api.animation;

import dev.harekuto.motifx.api.animation.AnimationModel.Pose;
import dev.harekuto.motifx.api.animation.AnimationModel.Skeleton;
import dev.harekuto.motifx.api.math.MathTypes;
import dev.harekuto.motifx.api.math.MathTypes.Quatf;
import dev.harekuto.motifx.api.math.MathTypes.Transform;
import dev.harekuto.motifx.api.math.MathTypes.Vec3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class AnimationRuntime {
    private AnimationRuntime() {}

    public enum Interpolation { STEP, LINEAR, SMOOTHSTEP }
    public enum LoopMode { ONCE, HOLD, LOOP, PING_PONG }
    public enum BlendMode { OVERRIDE, ADDITIVE }

    public static final class VecTrack {
        private final float[] times;
        private final Vec3f[] values;
        private final Interpolation interpolation;

        public VecTrack(float[] times, Vec3f[] values, Interpolation interpolation) {
            validateTimes(times, values == null ? -1 : values.length);
            this.times = times.clone();
            this.values = values.clone();
            for (Vec3f value : this.values) {
                if (value == null || !value.isFinite()) throw new IllegalArgumentException("VecTrack values must be finite");
            }
            this.interpolation = Objects.requireNonNull(interpolation, "interpolation");
        }

        public Vec3f sample(float time) {
            Segment segment = segment(times, time);
            if (segment.low == segment.high) return values[segment.low];
            float alpha = curve(segment.alpha, interpolation);
            return Vec3f.lerp(values[segment.low], values[segment.high], alpha);
        }
    }

    public static final class QuatTrack {
        private final float[] times;
        private final Quatf[] values;
        private final Interpolation interpolation;

        public QuatTrack(float[] times, Quatf[] values, Interpolation interpolation) {
            validateTimes(times, values == null ? -1 : values.length);
            this.times = times.clone();
            this.values = values.clone();
            for (Quatf value : this.values) {
                if (value == null || !value.isFinite()) throw new IllegalArgumentException("QuatTrack values must be finite");
            }
            this.interpolation = Objects.requireNonNull(interpolation, "interpolation");
        }

        public Quatf sample(float time) {
            Segment segment = segment(times, time);
            if (segment.low == segment.high) return values[segment.low].normalized();
            float alpha = curve(segment.alpha, interpolation);
            return Quatf.slerp(values[segment.low], values[segment.high], alpha);
        }
    }

    public record BoneTrack(int boneIndex, VecTrack translation, QuatTrack rotation, VecTrack scale) {
        public BoneTrack {
            if (boneIndex < 0) throw new IllegalArgumentException("boneIndex must be >= 0");
            if (translation == null && rotation == null && scale == null) {
                throw new IllegalArgumentException("BoneTrack must animate at least one component");
            }
        }

        Transform sample(float time, Transform bind) {
            return new Transform(
                translation == null ? bind.translation() : translation.sample(time),
                rotation == null ? bind.rotation() : rotation.sample(time),
                scale == null ? bind.scale() : scale.sample(time)
            );
        }
    }

    public record Marker(float time, String id) {
        public Marker {
            if (!Float.isFinite(time) || time < 0f) throw new IllegalArgumentException("Marker time must be finite and >= 0");
            if (id == null || id.isBlank()) throw new IllegalArgumentException("Marker id must not be blank");
        }
    }

    public static final class Clip {
        private final String id;
        private final float duration;
        private final LoopMode loopMode;
        private final List<BoneTrack> tracks;
        private final List<Marker> markers;

        public Clip(String id, float duration, LoopMode loopMode, List<BoneTrack> tracks, List<Marker> markers) {
            if (id == null || id.isBlank()) throw new IllegalArgumentException("Clip id must not be blank");
            if (!Float.isFinite(duration) || duration < 0f) throw new IllegalArgumentException("Clip duration must be finite and >= 0");
            this.id = id;
            this.duration = duration;
            this.loopMode = Objects.requireNonNull(loopMode, "loopMode");
            this.tracks = List.copyOf(Objects.requireNonNull(tracks, "tracks"));
            List<Marker> markerCopy = new ArrayList<>(Objects.requireNonNull(markers, "markers"));
            markerCopy.sort(Comparator.comparingDouble(Marker::time));
            for (Marker marker : markerCopy) {
                if (marker.time() > duration) throw new IllegalArgumentException("Marker outside clip duration: " + marker.id());
            }
            this.markers = List.copyOf(markerCopy);
        }

        public String id() { return id; }
        public float duration() { return duration; }
        public LoopMode loopMode() { return loopMode; }
        public List<Marker> markers() { return markers; }

        public float normalizeTime(float absoluteTime) {
            if (duration <= 0f) return 0f;
            if (!Float.isFinite(absoluteTime)) return 0f;
            return switch (loopMode) {
                case ONCE, HOLD -> Math.max(0f, Math.min(duration, absoluteTime));
                case LOOP -> positiveModulo(absoluteTime, duration);
                case PING_PONG -> {
                    float phase = positiveModulo(absoluteTime, duration * 2f);
                    yield phase <= duration ? phase : duration * 2f - phase;
                }
            };
        }

        public void sample(Skeleton skeleton, float absoluteTime, Pose output) {
            Objects.requireNonNull(skeleton, "skeleton");
            Objects.requireNonNull(output, "output");
            if (output.skeleton() != skeleton) throw new IllegalArgumentException("Output pose skeleton mismatch");
            output.resetToBind();
            float time = normalizeTime(absoluteTime);
            for (BoneTrack track : tracks) {
                if (track.boneIndex() >= skeleton.size()) {
                    throw new IllegalStateException("Clip " + id + " references missing bone index " + track.boneIndex());
                }
                Transform bind = skeleton.bindPose(track.boneIndex());
                output.set(track.boneIndex(), track.sample(time, bind));
            }
        }

        public List<Marker> markersBetween(float from, float to, boolean forward) {
            if (markers.isEmpty() || from == to) return List.of();
            List<Marker> out = new ArrayList<>();
            if (forward) {
                for (Marker marker : markers) if (marker.time() > from && marker.time() <= to) out.add(marker);
            } else {
                for (int i = markers.size() - 1; i >= 0; i--) {
                    Marker marker = markers.get(i);
                    if (marker.time() < from && marker.time() >= to) out.add(marker);
                }
            }
            return List.copyOf(out);
        }
    }

    public static final class Player {
        private Clip clip;
        private float time;
        private float speed = 1f;
        private boolean paused;

        public Player(Clip clip) { this.clip = Objects.requireNonNull(clip, "clip"); }
        public Clip clip() { return clip; }
        public float time() { return time; }
        public float speed() { return speed; }
        public boolean paused() { return paused; }

        public void play(Clip next, boolean resetTime) {
            clip = Objects.requireNonNull(next, "next");
            if (resetTime) time = 0f;
        }

        public void seek(float absoluteTime) { time = Float.isFinite(absoluteTime) ? absoluteTime : 0f; }
        public void setSpeed(float speed) { this.speed = Float.isFinite(speed) ? speed : 1f; }
        public void setPaused(boolean paused) { this.paused = paused; }

        public List<Marker> advance(float deltaSeconds) {
            if (paused || !Float.isFinite(deltaSeconds) || deltaSeconds == 0f || clip.duration() <= 0f) return List.of();
            float oldAbsolute = time;
            float oldLocal = clip.normalizeTime(oldAbsolute);
            time += deltaSeconds * speed;
            float newLocal = clip.normalizeTime(time);
            boolean forward = deltaSeconds * speed >= 0f;

            if (clip.loopMode() == LoopMode.LOOP) {
                if (forward && newLocal < oldLocal) {
                    List<Marker> out = new ArrayList<>(clip.markersBetween(oldLocal, clip.duration(), true));
                    out.addAll(clip.markersBetween(-Float.MIN_VALUE, newLocal, true));
                    return List.copyOf(out);
                }
                if (!forward && newLocal > oldLocal) {
                    List<Marker> out = new ArrayList<>(clip.markersBetween(oldLocal, 0f, false));
                    out.addAll(clip.markersBetween(clip.duration() + Float.MIN_VALUE, newLocal, false));
                    return List.copyOf(out);
                }
            }

            if (clip.loopMode() == LoopMode.PING_PONG) {
                // Playback is supported; marker semantics at a direction reversal are intentionally conservative in 0.2.
                return clip.markersBetween(oldLocal, newLocal, newLocal >= oldLocal);
            }
            return clip.markersBetween(oldLocal, newLocal, forward);
        }

        public void sample(Skeleton skeleton, Pose output) { clip.sample(skeleton, time, output); }
    }

    public static final class BoneMask {
        private final boolean[] enabled;

        private BoneMask(boolean[] enabled) { this.enabled = enabled; }

        public static BoneMask all(int boneCount) {
            if (boneCount <= 0) throw new IllegalArgumentException("boneCount must be > 0");
            boolean[] values = new boolean[boneCount];
            Arrays.fill(values, true);
            return new BoneMask(values);
        }

        public static BoneMask only(int boneCount, int... indices) {
            if (boneCount <= 0) throw new IllegalArgumentException("boneCount must be > 0");
            boolean[] values = new boolean[boneCount];
            for (int index : indices) {
                if (index < 0 || index >= boneCount) throw new IllegalArgumentException("Bone index out of range: " + index);
                values[index] = true;
            }
            return new BoneMask(values);
        }

        public boolean includes(int index) { return enabled[index]; }
        public int size() { return enabled.length; }
    }

    public static final class Layer {
        private final String id;
        private final Clip clip;
        private final int priority;
        private final BlendMode blendMode;
        private final BoneMask mask;
        private float time;
        private float weight;

        public Layer(String id, Clip clip, int priority, BlendMode blendMode, BoneMask mask, float weight) {
            if (id == null || id.isBlank()) throw new IllegalArgumentException("Layer id must not be blank");
            this.id = id;
            this.clip = Objects.requireNonNull(clip, "clip");
            this.priority = priority;
            this.blendMode = Objects.requireNonNull(blendMode, "blendMode");
            this.mask = Objects.requireNonNull(mask, "mask");
            this.weight = MathTypes.clamp01(weight);
        }

        public String id() { return id; }
        public Clip clip() { return clip; }
        public int priority() { return priority; }
        public BlendMode blendMode() { return blendMode; }
        public BoneMask mask() { return mask; }
        public float time() { return time; }
        public float weight() { return weight; }
        public void setTime(float time) { this.time = Float.isFinite(time) ? time : 0f; }
        public void advance(float deltaSeconds) { if (Float.isFinite(deltaSeconds)) time += deltaSeconds; }
        public void setWeight(float weight) { this.weight = MathTypes.clamp01(weight); }
    }

    public static final class Mixer {
        private final Skeleton skeleton;
        private final Pose scratch;
        private final List<Layer> layers = new ArrayList<>();

        public Mixer(Skeleton skeleton) {
            this.skeleton = Objects.requireNonNull(skeleton, "skeleton");
            this.scratch = new Pose(skeleton);
        }

        public void addLayer(Layer layer) {
            Objects.requireNonNull(layer, "layer");
            if (layer.mask().size() != skeleton.size()) throw new IllegalArgumentException("Layer mask skeleton size mismatch");
            layers.removeIf(existing -> existing.id().equals(layer.id()));
            layers.add(layer);
            layers.sort(Comparator.comparingInt(Layer::priority));
        }

        public boolean removeLayer(String id) { return layers.removeIf(layer -> layer.id().equals(id)); }
        public void clear() { layers.clear(); }
        public List<Layer> layers() { return List.copyOf(layers); }

        public void evaluate(Pose output) {
            if (output.skeleton() != skeleton) throw new IllegalArgumentException("Output pose skeleton mismatch");
            output.resetToBind();
            for (Layer layer : layers) {
                if (layer.weight() <= 0f) continue;
                layer.clip().sample(skeleton, layer.time(), scratch);
                for (int bone = 0; bone < skeleton.size(); bone++) {
                    if (!layer.mask().includes(bone)) continue;
                    Transform current = output.get(bone);
                    Transform sampled = scratch.get(bone);
                    Transform result = layer.blendMode() == BlendMode.ADDITIVE
                        ? Transform.applyAdditive(current, skeleton.bindPose(bone), sampled, layer.weight())
                        : Transform.blend(current, sampled, layer.weight());
                    output.set(bone, result);
                }
            }
        }
    }

    private static void validateTimes(float[] times, int valueCount) {
        if (times == null || times.length == 0 || times.length != valueCount) {
            throw new IllegalArgumentException("Track times and values must have the same non-zero length");
        }
        float previous = -Float.MAX_VALUE;
        for (float time : times) {
            if (!Float.isFinite(time) || time < 0f || time < previous) {
                throw new IllegalArgumentException("Track times must be finite, non-negative and sorted");
            }
            previous = time;
        }
    }

    private static Segment segment(float[] times, float time) {
        if (!Float.isFinite(time) || time <= times[0]) return new Segment(0, 0, 0f);
        int last = times.length - 1;
        if (time >= times[last]) return new Segment(last, last, 0f);
        int found = Arrays.binarySearch(times, time);
        if (found >= 0) return new Segment(found, found, 0f);
        int high = -found - 1;
        int low = high - 1;
        float span = times[high] - times[low];
        float alpha = span <= 0f ? 0f : (time - times[low]) / span;
        return new Segment(low, high, alpha);
    }

    private static float curve(float alpha, Interpolation interpolation) {
        float t = MathTypes.clamp01(alpha);
        return switch (interpolation) {
            case STEP -> 0f;
            case LINEAR -> t;
            case SMOOTHSTEP -> t * t * (3f - 2f * t);
        };
    }

    private static float positiveModulo(float value, float divisor) {
        float result = value % divisor;
        return result < 0f ? result + divisor : result;
    }

    private record Segment(int low, int high, float alpha) {}
}
