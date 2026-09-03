package dev.harekuto.motifx.api.asset;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.harekuto.motifx.api.animation.AnimationClip;
import dev.harekuto.motifx.api.animation.AnimationEventMarker;
import dev.harekuto.motifx.api.animation.BoneTrack;
import dev.harekuto.motifx.api.animation.Interpolation;
import dev.harekuto.motifx.api.animation.LoopMode;
import dev.harekuto.motifx.api.animation.Skeleton;
import dev.harekuto.motifx.api.math.Quatf;
import dev.harekuto.motifx.api.math.Vec3f;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Parser for MotifX clip format version 1. Parsing is separate from Minecraft resource lifecycle. */
public final class MotifClipJsonLoader {
    private MotifClipJsonLoader() {}

    public static AnimationClip load(Reader reader, Skeleton skeleton) {
        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
        int version = root.has("format_version") ? root.get("format_version").getAsInt() : -1;
        if (version != 1) throw new IllegalArgumentException("Unsupported MotifX clip format_version: " + version);

        String name = requiredString(root, "name");
        float duration = requiredFloat(root, "duration");
        LoopMode loop = parseLoop(root.has("loop") ? root.get("loop").getAsString() : "loop");
        Map<Integer, BoneTrack> tracks = new HashMap<>();

        JsonObject bones = root.has("bones") ? root.getAsJsonObject("bones") : new JsonObject();
        for (Map.Entry<String, JsonElement> entry : bones.entrySet()) {
            int boneIndex = skeleton.indexOf(entry.getKey());
            if (boneIndex < 0) throw new IllegalArgumentException("Animation references missing bone: " + entry.getKey());
            JsonObject track = entry.getValue().getAsJsonObject();
            tracks.put(boneIndex, new BoneTrack(
                    parseVecKeys(track.getAsJsonArray("translation")),
                    parseRotKeys(track.getAsJsonArray("rotation")),
                    parseVecKeys(track.getAsJsonArray("scale"))
            ));
        }

        List<AnimationEventMarker> events = new ArrayList<>();
        if (root.has("events")) {
            for (JsonElement element : root.getAsJsonArray("events")) {
                JsonObject event = element.getAsJsonObject();
                events.add(new AnimationEventMarker(requiredString(event, "id"), requiredFloat(event, "time")));
            }
        }
        return new AnimationClip(name, duration, loop, tracks, events);
    }

    private static List<BoneTrack.VecKey> parseVecKeys(JsonArray array) {
        if (array == null) return List.of();
        List<BoneTrack.VecKey> keys = new ArrayList<>();
        for (JsonElement element : array) {
            JsonObject key = element.getAsJsonObject();
            JsonArray value = key.has("value") ? key.getAsJsonArray("value") : null;
            if (value == null || value.size() != 3) throw new IllegalArgumentException("Vector key requires value:[x,y,z]");
            keys.add(new BoneTrack.VecKey(requiredFloat(key, "time"), vec3(value), parseInterpolation(key)));
        }
        return keys;
    }

    private static List<BoneTrack.RotKey> parseRotKeys(JsonArray array) {
        if (array == null) return List.of();
        List<BoneTrack.RotKey> keys = new ArrayList<>();
        for (JsonElement element : array) {
            JsonObject key = element.getAsJsonObject();
            JsonArray euler = key.getAsJsonArray("euler_deg");
            if (euler == null || euler.size() != 3) throw new IllegalArgumentException("Rotation key requires euler_deg:[x,y,z]");
            keys.add(new BoneTrack.RotKey(
                    requiredFloat(key, "time"),
                    Quatf.fromEulerDegrees(euler.get(0).getAsFloat(), euler.get(1).getAsFloat(), euler.get(2).getAsFloat()),
                    parseInterpolation(key)
            ));
        }
        return keys;
    }

    private static Vec3f vec3(JsonArray array) {
        return new Vec3f(array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat());
    }

    private static Interpolation parseInterpolation(JsonObject object) {
        String value = object.has("interpolation") ? object.get("interpolation").getAsString() : "linear";
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "step" -> Interpolation.STEP;
            case "linear" -> Interpolation.LINEAR;
            case "smoothstep" -> Interpolation.SMOOTHSTEP;
            default -> throw new IllegalArgumentException("Unknown MotifX interpolation: " + value);
        };
    }

    private static LoopMode parseLoop(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "once" -> LoopMode.ONCE;
            case "loop" -> LoopMode.LOOP;
            case "hold" -> LoopMode.HOLD;
            case "ping_pong", "ping-pong" -> LoopMode.PING_PONG;
            default -> throw new IllegalArgumentException("Unknown MotifX loop mode: " + value);
        };
    }

    private static String requiredString(JsonObject object, String key) {
        if (!object.has(key)) throw new IllegalArgumentException("Missing required MotifX field: " + key);
        return object.get(key).getAsString();
    }

    private static float requiredFloat(JsonObject object, String key) {
        if (!object.has(key)) throw new IllegalArgumentException("Missing required MotifX field: " + key);
        return object.get(key).getAsFloat();
    }
}
