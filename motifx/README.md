# MotifX 0.2.0-dev

MotifX is an experimental Forge 1.20.1 motion/runtime library focused on composable skeletal animation, state graphs, compatibility adapters, diagnostics and developer tooling.

This branch is a development build. The core runtime is deliberately loader-light so future NeoForge/Fabric adapters can reuse the same animation math and graph logic instead of forking it.

## Current verified target

- Minecraft 1.20.1
- Forge 47.4.10
- Java 17
- Mojang official mappings

## Design goals

- deterministic pose composition instead of renderer ownership
- layered override and additive animation
- typed graph parameters and priority transitions
- compatibility-adapter SPI instead of hard dependencies
- JSON format detection and validation for migration tooling
- bounded diagnostics and runtime metrics
- dedicated-server-safe common bootstrap
- optional client inspector

Build and runtime status is determined by GitHub Actions; do not infer success from source presence alone.
