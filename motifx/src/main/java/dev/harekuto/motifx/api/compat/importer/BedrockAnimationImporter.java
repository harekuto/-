package dev.harekuto.motifx.api.compat.importer;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import dev.harekuto.motifx.api.animation.AnimationModel.Pose;
import dev.harekuto.motifx.api.animation.AnimationModel.Skeleton;
import dev.harekuto.motifx.api.animation.AnimationRuntime.BoneTrack;
import dev.harekuto.motifx.api.animation.AnimationRuntime.Clip;
import dev.harekuto.motifx.api.animation.AnimationRuntime.Interpolation;
import dev.harekuto.motifx.api.animation.AnimationRuntime.LoopMode;
import dev.harekuto.motifx.api.animation.AnimationRuntime.Marker;
import dev.harekuto.motifx.api.animation.AnimationRuntime.QuatTrack;
import dev.harekuto.motifx.api.animation.AnimationRuntime.VecTrack;
import dev.harekuto.motifx.api.diagnostics.Diagnostics;
import dev.harekuto.motifx.api.diagnostics.Diagnostics.Issue;
import dev.harekuto.motifx.api.diagnostics.Diagnostics.Severity;
import dev.harekuto.motifx.api.math.MathTypes.Quatf;
import dev.harekuto.motifx.api.math.MathTypes.Vec3f;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Clean importer for the numeric subset shared by common Bedrock/GeckoLib-style animation JSON documents.
 * It deliberately does not execute Molang or arbitrary expressions. Unsupported expressions are reported.
 */
public final class BedrockAnimationImporter {
    private static final int MAX_DOCUMENT_CHARS = 4_000_000;
    private static final int MAX_ANIMATIONS = 4096;
    private static final int MAX_KEYFRAMES_PER_CHANNEL = 65_536;

    private BedrockAnimationImporter() {}

    public enum Channel { POSITION, ROTATION_DEGREES, SCALE }

    @FunctionalInterface
    public interface ChannelMapper {
        Vec3f map(Channel channel, Vec3f value);
    }

    public static final ChannelMapper IDENTITY_MAPPER = (channel, value) -> value;

    public record ImportResult(Map<String, Clip> clips, List<Issue> issues) {
        public ImportResult {
            clips = Collections.unmodifiableMap(new LinkedHashMap<>(clips));
            issues = List.copyOf(issues);
        }

        public boolean valid() {
            return issues.stream().noneMatch(issue -> issue.severity() == Severity.ERROR);
        }

        public long errorCount() {
            return issues.stream().filter(issue -> issue.severity() == Severity.ERROR).count();
        }
    }

    public static ImportResult importAnimations(String json, Skeleton skeleton) {
        return importAnimations(json, skeleton, IDENTITY_MAPPER);
    }

    public static ImportResult importAnimations(String json, Skeleton skeleton, ChannelMapper mapper) {
        Objects.requireNonNull(skeleton, "skeleton");
        Objects.requireNonNull(mapper, "mapper");
        List<Issue> issues = new ArrayList<>();
        Map<String, Clip> clips = new LinkedHashMap<>();

        if (json == null || json.isBlank()) {
            issues.add(error("empty", "$", "Animation JSON is empty"));
            return new ImportResult(clips, issues);
        }
        if (json.length() > MAX_DOCUMENT_CHARS) {
            issues.add(error("too_large", "$", "Animation JSON exceeds the importer safety limit"));
            return new ImportResult(clips, issues);
        }

        final JsonObject root;
        try {
            JsonElement parsed = JsonParser.parseString(json);
            if (!parsed.isJsonObject()) {
                issues.add(error("root_type", "$", "Animation root must be a JSON object"));
                return new ImportResult(clips, issues);
            }
            root = parsed.getAsJsonObject();
        } catch (JsonParseException | IllegalStateException ex) {
            issues.add(error("malformed_json", "$", ex.getMessage() == null ? "Malformed animation JSON" : ex.getMessage()));
            return new ImportResult(clips, issues);
        }

        if (!root.has("animations") || !root.get("animations").isJsonObject()) {
            issues.add(error("animations", "$.animations", "Expected a Bedrock/GeckoLib-style animations object"));
            return new ImportResult(clips, issues);
        }

        JsonObject animations = root.getAsJsonObject("animations");
        if (animations.size() > MAX_ANIMATIONS) {
            issues.add(error("animation_limit", "$.animations", "Animation count exceeds " + MAX_ANIMATIONS));
            return new ImportResult(clips, issues);
        }

        for (Map.Entry<String, JsonElement> entry : animations.entrySet()) {
            importClip(entry.getKey(), entry.getValue(), skeleton, mapper, clips, issues);
        }
        return new ImportResult(clips, issues);
    }

    private static void importClip(String id, JsonElement element, Skeleton skeleton, ChannelMapper mapper,
                                   Map<String, Clip> clips, List<Issue> issues) {
        String base = "$.animations." + id;
        if (!element.isJsonObject()) {
            issues.add(error("animation_type", base, "Animation must be an object"));
            return;
        }
        JsonObject animation = element.getAsJsonObject();
        Float declaredDuration = readOptionalNonNegativeFloat(animation.get("animation_length"), base + ".animation_length", issues);
        float inferredMaxTime = declaredDuration == null ? 0f : declaredDuration;
        LoopMode loopMode = parseLoopMode(animation.get("loop"), base + ".loop", issues);
        List<BoneTrack> tracks = new ArrayList<>();

        if (animation.has("bones")) {
            if (!animation.get("bones").isJsonObject()) {
                issues.add(error("bones_type", base + ".bones", "bones must be an object"));
            } else {
                JsonObject bones = animation.getAsJsonObject("bones");
                for (Map.Entry<String, JsonElement> boneEntry : bones.entrySet()) {
                    String bonePath = base + ".bones." + boneEntry.getKey();
                    int boneIndex = skeleton.indexOf(boneEntry.getKey());
                    if (boneIndex < 0) {
                        issues.add(warning("unknown_bone", bonePath, "Bone '" + boneEntry.getKey() + "' is not present in the target skeleton; its tracks were skipped"));
                        continue;
                    }
                    if (!boneEntry.getValue().isJsonObject()) {
                        issues.add(error("bone_type", bonePath, "Bone animation must be an object"));
                        continue;
                    }
                    JsonObject bone = boneEntry.getValue().getAsJsonObject();
                    VecChannel position = parseVecChannel(bone.get("position"), bonePath + ".position", Channel.POSITION, mapper, issues);
                    VecChannel rotationDegrees = parseVecChannel(bone.get("rotation"), bonePath + ".rotation", Channel.ROTATION_DEGREES, mapper, issues);
                    VecChannel scale = parseVecChannel(bone.get("scale"), bonePath + ".scale", Channel.SCALE, mapper, issues);

                    VecTrack translationTrack = position == null ? null : new VecTrack(position.times, position.values, Interpolation.LINEAR);
                    QuatTrack rotationTrack = rotationDegrees == null ? null : new QuatTrack(
                        rotationDegrees.times,
                        toQuaternions(rotationDegrees.values),
                        Interpolation.LINEAR
                    );
                    VecTrack scaleTrack = scale == null ? null : new VecTrack(scale.times, scale.values, Interpolation.LINEAR);
                    if (translationTrack != null || rotationTrack != null || scaleTrack != null) {
                        tracks.add(new BoneTrack(boneIndex, translationTrack, rotationTrack, scaleTrack));
                    }
                    if (position != null) inferredMaxTime = Math.max(inferredMaxTime, position.maxTime());
                    if (rotationDegrees != null) inferredMaxTime = Math.max(inferredMaxTime, rotationDegrees.maxTime());
                    if (scale != null) inferredMaxTime = Math.max(inferredMaxTime, scale.maxTime());
                }
            }
        }

        List<Marker> markers = new ArrayList<>();
        inferredMaxTime = Math.max(inferredMaxTime, parseMarkers(animation.get("timeline"), base + ".timeline", "timeline", markers, issues));
        inferredMaxTime = Math.max(inferredMaxTime, parseMarkers(animation.get("sound_effects"), base + ".sound_effects", "sound", markers, issues));
        inferredMaxTime = Math.max(inferredMaxTime, parseMarkers(animation.get("particle_effects"), base + ".particle_effects", "particle", markers, issues));

        float duration = declaredDuration == null ? inferredMaxTime : declaredDuration;
        if (declaredDuration != null && inferredMaxTime > declaredDuration + 1.0e-6f) {
            issues.add(warning("duration_extended", base + ".animation_length", "Keyframes/events exceed animation_length; imported duration was extended to preserve data"));
            duration = inferredMaxTime;
        }

        try {
            clips.put(id, new Clip(id, duration, loopMode, tracks, markers));
        } catch (RuntimeException ex) {
            issues.add(error("clip_compile", base, ex.getMessage() == null ? "Failed to compile imported clip" : ex.getMessage()));
        }
    }

    private static VecChannel parseVecChannel(JsonElement element, String path, Channel channel, ChannelMapper mapper, List<Issue> issues) {
        if (element == null || element.isJsonNull()) return null;
        Vec3f direct = readVector(element, path, issues);
        if (direct != null) {
            Vec3f mapped = mapVector(mapper, channel, direct, path, issues);
            return mapped == null ? null : new VecChannel(new float[]{0f}, new Vec3f[]{mapped});
        }
        if (!element.isJsonObject()) {
            issues.add(warning("unsupported_channel", path, "Channel is not a numeric vector or numeric keyframe object; expression data was skipped"));
            return null;
        }

        JsonObject object = element.getAsJsonObject();
        Vec3f objectVector = readVectorFromKeyframeObject(object, path, issues);
        if (objectVector != null) {
            Vec3f mapped = mapVector(mapper, channel, objectVector, path, issues);
            return mapped == null ? null : new VecChannel(new float[]{0f}, new Vec3f[]{mapped});
        }

        List<TimedVec> frames = new ArrayList<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            final float time;
            try {
                time = Float.parseFloat(entry.getKey());
            } catch (NumberFormatException ex) {
                issues.add(warning("unsupported_key", path + "." + entry.getKey(), "Non-numeric keyframe key was skipped"));
                continue;
            }
            if (!Float.isFinite(time) || time < 0f) {
                issues.add(error("keyframe_time", path + "." + entry.getKey(), "Keyframe time must be finite and >= 0"));
                continue;
            }
            Vec3f raw = readVector(entry.getValue(), path + "." + entry.getKey(), issues);
            if (raw == null && entry.getValue().isJsonObject()) {
                JsonObject keyframe = entry.getValue().getAsJsonObject();
                raw = readVectorFromKeyframeObject(keyframe, path + "." + entry.getKey(), issues);
                if (keyframe.has("lerp_mode")) {
                    String mode = safeString(keyframe.get("lerp_mode"));
                    if (mode != null && !mode.equalsIgnoreCase("linear")) {
                        issues.add(warning("interpolation_approximation", path + "." + entry.getKey() + ".lerp_mode", "Interpolation '" + mode + "' is imported as linear in MotifX 0.2"));
                    }
                }
            }
            if (raw == null) {
                issues.add(warning("unsupported_expression", path + "." + entry.getKey(), "Non-numeric/Molang keyframe was skipped"));
                continue;
            }
            Vec3f mapped = mapVector(mapper, channel, raw, path + "." + entry.getKey(), issues);
            if (mapped != null) frames.add(new TimedVec(time, mapped));
            if (frames.size() > MAX_KEYFRAMES_PER_CHANNEL) {
                issues.add(error("keyframe_limit", path, "Channel exceeds " + MAX_KEYFRAMES_PER_CHANNEL + " keyframes"));
                return null;
            }
        }

        if (frames.isEmpty()) return null;
        frames.sort(Comparator.comparingDouble(TimedVec::time));
        float[] times = new float[frames.size()];
        Vec3f[] values = new Vec3f[frames.size()];
        for (int i = 0; i < frames.size(); i++) {
            times[i] = frames.get(i).time();
            values[i] = frames.get(i).value();
        }
        return new VecChannel(times, values);
    }

    private static Vec3f mapVector(ChannelMapper mapper, Channel channel, Vec3f raw, String path, List<Issue> issues) {
        final Vec3f mapped;
        try {
            mapped = mapper.map(channel, raw);
        } catch (RuntimeException ex) {
            issues.add(error("mapper_failure", path, "Channel mapper failed: " + ex.getClass().getSimpleName()));
            return null;
        }
        if (mapped == null || !mapped.isFinite()) {
            issues.add(error("mapped_vector", path, "Channel mapper returned null or a non-finite vector"));
            return null;
        }
        return mapped;
    }

    private static Vec3f readVector(JsonElement element, String path, List<Issue> issues) {
        if (element == null || element.isJsonNull()) return null;
        if (!element.isJsonArray()) return null;
        JsonArray array = element.getAsJsonArray();
        if (array.size() != 3) {
            issues.add(error("vector_size", path, "Numeric transform vector must contain exactly 3 values"));
            return null;
        }
        float[] values = new float[3];
        for (int i = 0; i < 3; i++) {
            JsonElement value = array.get(i);
            if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) return null;
            try {
                values[i] = value.getAsFloat();
            } catch (RuntimeException ex) {
                return null;
            }
            if (!Float.isFinite(values[i])) {
                issues.add(error("vector_finite", path + "[" + i + "]", "Transform value must be finite"));
                return null;
            }
        }
        return new Vec3f(values[0], values[1], values[2]);
    }

    private static Vec3f readVectorFromKeyframeObject(JsonObject object, String path, List<Issue> issues) {
        for (String key : List.of("post", "vector", "pre")) {
            if (object.has(key)) {
                Vec3f value = readVector(object.get(key), path + "." + key, issues);
                if (value != null) return value;
            }
        }
        return null;
    }

    private static Quatf[] toQuaternions(Vec3f[] degrees) {
        Quatf[] values = new Quatf[degrees.length];
        for (int i = 0; i < degrees.length; i++) {
            Vec3f value = degrees[i];
            values[i] = Quatf.fromEulerDegrees(value.x(), value.y(), value.z());
        }
        return values;
    }

    private static LoopMode parseLoopMode(JsonElement element, String path, List<Issue> issues) {
        if (element == null || element.isJsonNull()) return LoopMode.ONCE;
        if (element.isJsonPrimitive()) {
            var primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) return primitive.getAsBoolean() ? LoopMode.LOOP : LoopMode.ONCE;
            if (primitive.isString()) {
                String value = primitive.getAsString();
                if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("loop")) return LoopMode.LOOP;
                if (value.equalsIgnoreCase("false") || value.equalsIgnoreCase("once")) return LoopMode.ONCE;
                if (value.equalsIgnoreCase("hold_on_last_frame") || value.equalsIgnoreCase("hold")) return LoopMode.HOLD;
            }
        }
        issues.add(warning("loop_mode", path, "Unknown loop value; imported as ONCE"));
        return LoopMode.ONCE;
    }

    private static Float readOptionalNonNegativeFloat(JsonElement element, String path, List<Issue> issues) {
        if (element == null || element.isJsonNull()) return null;
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
            issues.add(warning("duration_expression", path, "Non-numeric animation_length is unsupported in the numeric importer; duration will be inferred"));
            return null;
        }
        try {
            float value = element.getAsFloat();
            if (!Float.isFinite(value) || value < 0f) {
                issues.add(error("duration", path, "animation_length must be finite and >= 0"));
                return null;
            }
            return value;
        } catch (RuntimeException ex) {
            issues.add(error("duration", path, "animation_length could not be read as a finite number"));
            return null;
        }
    }

    private static float parseMarkers(JsonElement element, String path, String prefix, List<Marker> markers, List<Issue> issues) {
        if (element == null || element.isJsonNull()) return 0f;
        if (!element.isJsonObject()) {
            issues.add(warning("event_track_type", path, "Event track must be an object keyed by numeric time; it was skipped"));
            return 0f;
        }
        float max = 0f;
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            final float time;
            try {
                time = Float.parseFloat(entry.getKey());
            } catch (NumberFormatException ex) {
                issues.add(warning("event_time", path + "." + entry.getKey(), "Non-numeric event time was skipped"));
                continue;
            }
            if (!Float.isFinite(time) || time < 0f) {
                issues.add(error("event_time", path + "." + entry.getKey(), "Event time must be finite and >= 0"));
                continue;
            }
            String description = describeEvent(entry.getValue());
            String markerId = prefix + ":" + sanitizeMarker(description);
            markers.add(new Marker(time, markerId));
            max = Math.max(max, time);
        }
        return max;
    }

    private static String describeEvent(JsonElement element) {
        if (element == null || element.isJsonNull()) return "event";
        if (element.isJsonPrimitive()) return element.getAsString();
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            for (String key : List.of("effect", "sound", "particle", "event")) {
                if (object.has(key) && object.get(key).isJsonPrimitive()) return object.get(key).getAsString();
            }
        }
        return "event";
    }

    private static String sanitizeMarker(String value) {
        if (value == null || value.isBlank()) return "event";
        String cleaned = value.replaceAll("[^A-Za-z0-9_./:-]", "_");
        if (cleaned.length() > 120) cleaned = cleaned.substring(0, 120);
        return cleaned.isBlank() ? "event" : cleaned;
    }

    private static String safeString(JsonElement element) {
        try {
            return element != null && element.isJsonPrimitive() ? element.getAsString() : null;
        } catch (RuntimeException ex) {
            return null;
        }
    }

    private static Issue error(String code, String path, String message) { return new Issue(Severity.ERROR, code, path, message); }
    private static Issue warning(String code, String path, String message) { return new Issue(Severity.WARNING, code, path, message); }

    private record TimedVec(float time, Vec3f value) {}

    private record VecChannel(float[] times, Vec3f[] values) {
        private VecChannel {
            if (times.length != values.length || times.length == 0) throw new IllegalArgumentException("Channel data mismatch");
        }
        float maxTime() { return times[times.length - 1]; }
    }
}
