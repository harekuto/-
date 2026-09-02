# Voxline Voice 0.7.0 — Groups & Comfort

Forge 1.20.1 / Forge 47.4.10 / Java 17.

Major changes:
- 48 kHz / 20 ms Opus voice using pure-Java Concentus bundled with Forge Jar-in-Jar.
- Separate Proximity PTT (V) and Group PTT (G); holding both transmits one encoded frame to both server routes.
- Server-authoritative groups with create, invite, accept/decline, leave, kick, promote and disband.
- Group voice ignores distance and dimension; the server multicasts one incoming frame to online members.
- Adaptive noise floor, smooth suppression, AGC, compressor, clarity stage, limiter, VAD hysteresis, 40 ms pre-roll and configurable hangover.
- FEC/PLC playback recovery, jitter cushion, bounded capture/playback queues and stale-frame dropping.
- Two-layer Minecraft heads (base face + hat/second skin layer) in HUD, overlay editor, group screen and player mixer.
- Per-player local 0–200% volume/mute and separate group master volume.
- Studio settings UI, Overlay Studio, Group UI and status HUD.

Build: `gradle clean jarJar reobfJarJar` using Gradle 8.8 and JDK 17.
