package dev.harekuto.motifx.api.animation;

import java.util.Objects;
import java.util.function.Consumer;

/** Stateful clip player. One instance is intended per animated runtime object/controller. */
public final class AnimationPlayer {
    private AnimationClip clip;
    private float rawTime;
    private float speed = 1.0f;
    private boolean playing = true;

    public AnimationPlayer(AnimationClip clip) {
        this.clip = Objects.requireNonNull(clip, "clip");
    }

    public AnimationClip clip() { return clip; }
    public float rawTime() { return rawTime; }
    public float speed() { return speed; }
    public boolean playing() { return playing; }

    public void setClip(AnimationClip clip, boolean resetTime) {
        this.clip = Objects.requireNonNull(clip, "clip");
        if (resetTime) rawTime = 0.0f;
        playing = true;
    }

    public void setSpeed(float speed) {
        if (!Float.isFinite(speed) || speed < 0.0f) {
            throw new IllegalArgumentException("Animation speed must be finite and >= 0 in MotifX 0.1.x");
        }
        this.speed = speed;
    }

    public void seek(float rawTime) {
        this.rawTime = Float.isFinite(rawTime) ? rawTime : 0.0f;
    }

    public void play() { playing = true; }
    public void pause() { playing = false; }

    public void advance(float deltaSeconds, Consumer<AnimationEventMarker> markerConsumer) {
        if (!playing || deltaSeconds <= 0.0f || !Float.isFinite(deltaSeconds)) return;
        float previous = rawTime;
        rawTime += deltaSeconds * speed;

        if (speed > 0.0f && markerConsumer != null) {
            clip.dispatchForwardMarkers(previous, rawTime, markerConsumer);
        }

        if (clip.loopMode() == LoopMode.ONCE && rawTime >= clip.duration()) {
            rawTime = clip.duration();
            playing = false;
        } else if (clip.loopMode() == LoopMode.HOLD && rawTime >= clip.duration()) {
            rawTime = clip.duration();
        }
    }

    public void sample(Skeleton skeleton, Pose output) {
        clip.sample(skeleton, rawTime, output);
    }
}
