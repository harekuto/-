# MotifX 0.1.0

MotifX is a source-first Minecraft motion runtime MVP for Forge 1.20.1.

This release intentionally focuses on a stable core instead of pretending to be a complete GeckoLib replacement on day one.

## Implemented in 0.1.0

- loader-independent skeletal data model
- immutable animation clips and bone tracks
- quaternion rotation interpolation
- step, linear and smoothstep curves
- loop, once, hold and ping-pong clip timing
- event markers with bounded loop dispatch
- reusable poses and bone masks
- layered animation mixer
- typed indexed float/boolean graph parameters
- compiled animation state graph with priority transitions
- cross-fade state graph player with reusable scratch poses
- structured validation diagnostics
- JSON clip parser with versioned format
- `/motifx status` and `/motifx demo` commands
- F8 in-game Motif Inspector metrics screen
- JUnit tests for math, clips and graph transitions
- Forge client/server class separation

## Scope

The 0.1.x line establishes the runtime and developer tooling foundation. Custom model rendering, Blockbench tooling, full resource reload integration, networking, IK and VFX belong to later milestones only after this core is proven stable.
