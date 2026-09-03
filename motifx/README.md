# MotifX 0.2.0-dev

MotifX is an experimental Forge 1.20.1 motion/runtime library focused on composable skeletal animation, state graphs, compatibility, diagnostics and developer tooling.

## Verified target

- Minecraft 1.20.1
- Forge 47.4.10
- Java 17
- Mojang official mappings

## Implemented in dev.2

- deterministic skeletal pose runtime and quaternion SLERP
- loop/hold/ping-pong playback and event markers
- bone masks, override layers and bind-relative additive layers
- typed primitive-array graph parameters, priority transitions and cross-fade samples
- pose compositor SPI so integrations can contribute instead of replacing an entire renderer
- compatibility adapter registry with no hard dependency on other animation mods
- clean numeric importer for common Bedrock/GeckoLib-style animation JSON: position/rotation/scale, numeric keyframes, loop/hold, timeline/sound/particle markers, unknown-bone diagnostics and safety limits
- channel-mapper hook for model/loader coordinate-convention adapters
- explicit diagnostics for unsupported Molang/expression keyframes instead of pretending they work
- structured validation, runtime metrics and an F8 client inspector
- dedicated-server-safe common bootstrap

## Important limits

This is not yet a drop-in replacement for GeckoLib. The importer intentionally supports numeric animation data only; full Molang semantics, renderer binding, automatic player/armor integration, hot reload, semantic multiplayer sync and Fabric/NeoForge platform modules remain separate quality-gated milestones.

Build and runtime status is determined by CI. Source presence is not treated as build success.
