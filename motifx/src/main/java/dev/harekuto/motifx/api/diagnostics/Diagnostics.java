package dev.harekuto.motifx.api.diagnostics;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

public final class Diagnostics {
    private static final int MAX_JSON_CHARS = 4_000_000;
    private static final int MAX_BONES_PER_ANIMATION = 4096;

    private Diagnostics() {}

    public enum AssetFormat { MOTIFX, GECKOLIB_BEDROCK_ANIMATION, BEDROCK_GEOMETRY, UNKNOWN }
    public enum Severity { INFO, WARNING, ERROR }

    public record Issue(Severity severity, String code, String path, String message) {}

    public record Report(AssetFormat format, List<Issue> issues) {
        public Report { issues = List.copyOf(issues); }
        public long errorCount() { return issues.stream().filter(issue -> issue.severity() == Severity.ERROR).count(); }
        public boolean valid() { return errorCount() == 0; }
    }

    public static AssetFormat detectFormat(String json) {
        if (json == null || json.isBlank() || json.length() > MAX_JSON_CHARS) return AssetFormat.UNKNOWN;
        try {
            JsonElement rootElement = JsonParser.parseString(json);
            if (!rootElement.isJsonObject()) return AssetFormat.UNKNOWN;
            JsonObject root = rootElement.getAsJsonObject();
            if (root.has("format_version") && root.has("clips")) return AssetFormat.MOTIFX;
            if (root.has("animations") && root.get("animations").isJsonObject()) return AssetFormat.GECKOLIB_BEDROCK_ANIMATION;
            if (root.has("minecraft:geometry")) return AssetFormat.BEDROCK_GEOMETRY;
            return AssetFormat.UNKNOWN;
        } catch (JsonParseException | IllegalStateException ex) {
            return AssetFormat.UNKNOWN;
        }
    }

    public static Report validateAnimationJson(String json) {
        List<Issue> issues = new ArrayList<>();
        if (json == null || json.isBlank()) {
            issues.add(new Issue(Severity.ERROR, "empty", "$", "Animation JSON is empty"));
            return new Report(AssetFormat.UNKNOWN, issues);
        }
        if (json.length() > MAX_JSON_CHARS) {
            issues.add(new Issue(Severity.ERROR, "too_large", "$", "Animation JSON exceeds the 4,000,000 character safety limit"));
            return new Report(AssetFormat.UNKNOWN, issues);
        }

        final JsonObject root;
        try {
            JsonElement element = JsonParser.parseString(json);
            if (!element.isJsonObject()) {
                issues.add(new Issue(Severity.ERROR, "root_type", "$", "Animation root must be a JSON object"));
                return new Report(AssetFormat.UNKNOWN, issues);
            }
            root = element.getAsJsonObject();
        } catch (JsonParseException ex) {
            issues.add(new Issue(Severity.ERROR, "malformed_json", "$", ex.getMessage() == null ? "Malformed JSON" : ex.getMessage()));
            return new Report(AssetFormat.UNKNOWN, issues);
        }

        AssetFormat format = detectFormat(json);
        if (format == AssetFormat.UNKNOWN) {
            issues.add(new Issue(Severity.WARNING, "unknown_format", "$", "The asset does not match a currently recognized MotifX/Bedrock animation layout"));
            return new Report(format, issues);
        }

        if (format == AssetFormat.MOTIFX) {
            JsonElement version = root.get("format_version");
            if (!version.isJsonPrimitive() || !version.getAsJsonPrimitive().isNumber()) {
                issues.add(new Issue(Severity.ERROR, "format_version", "$.format_version", "format_version must be numeric"));
            }
            JsonElement clips = root.get("clips");
            if (!clips.isJsonObject() || clips.getAsJsonObject().size() == 0) {
                issues.add(new Issue(Severity.ERROR, "clips", "$.clips", "clips must be a non-empty object"));
            }
        }

        if (format == AssetFormat.GECKOLIB_BEDROCK_ANIMATION) {
            JsonObject animations = root.getAsJsonObject("animations");
            if (animations.size() == 0) issues.add(new Issue(Severity.ERROR, "animations_empty", "$.animations", "animations must not be empty"));
            for (var entry : animations.entrySet()) {
                String base = "$.animations." + entry.getKey();
                if (!entry.getValue().isJsonObject()) {
                    issues.add(new Issue(Severity.ERROR, "animation_type", base, "Animation must be an object"));
                    continue;
                }
                JsonObject animation = entry.getValue().getAsJsonObject();
                if (animation.has("animation_length")) {
                    try {
                        float duration = animation.get("animation_length").getAsFloat();
                        if (!Float.isFinite(duration) || duration < 0f) issues.add(new Issue(Severity.ERROR, "duration", base + ".animation_length", "animation_length must be finite and >= 0"));
                    } catch (RuntimeException ex) {
                        issues.add(new Issue(Severity.ERROR, "duration_type", base + ".animation_length", "animation_length must be numeric"));
                    }
                }
                if (animation.has("bones")) {
                    if (!animation.get("bones").isJsonObject()) {
                        issues.add(new Issue(Severity.ERROR, "bones_type", base + ".bones", "bones must be an object"));
                    } else if (animation.getAsJsonObject("bones").size() > MAX_BONES_PER_ANIMATION) {
                        issues.add(new Issue(Severity.ERROR, "bones_limit", base + ".bones", "Animation exceeds the 4096-bone safety limit"));
                    }
                }
            }
        }

        if (format == AssetFormat.BEDROCK_GEOMETRY) {
            issues.add(new Issue(Severity.INFO, "geometry_only", "$.minecraft:geometry", "Geometry format detected; this validator currently validates animation documents only"));
        }
        return new Report(format, issues);
    }
}
