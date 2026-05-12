# Miners Advantage

Miners Advantage has been modernized into a multi-loader project that keeps legacy gameplay behavior while replacing legacy build and architecture foundations.

## Overview

Miners Advantage is a utility mod suite focused on making repetitive mining and farming workflows faster and more consistent while preserving familiar behavior across supported loaders.

## Features

- Multi-loader support for Fabric and NeoForge.
- Descriptor-driven component architecture for maintainable feature wiring.
- Shared service modules for policy, synchronization, harvesting, farming, and utility flows.
- Deterministic unit-test coverage across parity-critical behavior.

## How It Works

The rewrite uses a common core for gameplay logic and thin loader adapters for platform-specific bootstrap, networking registration, and event wiring.

## Supported Crops

Crop behavior and runtime planning are covered by shared farming and harvest services designed for parity with legacy behavior.

## Installation

### Fabric

Install Fabric Loader for a supported Minecraft version, then place the Miners Advantage Fabric jar in the mods folder.

### NeoForge

Install NeoForge for a supported Minecraft version, then place the Miners Advantage NeoForge jar in the mods folder.

## Configuration

Configuration is managed through the modern shared config model and synced feature settings, with loader-specific screen registration for in-game access.

## Compatibility

Miners Advantage is designed to remain compatible with FastItemFrames and other common quality-of-life client/server stacks where feature overlap does not conflict.

## Technical Documentation

See [TECHNICAL.md](TECHNICAL.md) for implementation details, architecture, and build internals.

## Modernization Status

| Phase | Status | Notes |
| --- | --- | --- |
| 0 Repository Reset | complete | Legacy source archived in releases/original and untracked. |
| 1 Build Foundation | complete | Stonecutter + Modstitch scaffold created for four nodes. |
| 2 Architecture | complete | Component lifecycle, descriptor registry, and loader-neutral orchestration are fully wired. |
| 3 Parity Rewrite | complete | Bootstrap, Client Input, Networking transport, Server Events/dispatch, Config UI, and Assets/localization are now wired across all four nodes. Networking now includes `FeatureDispatchPacket` and `SupremeVantagePacket` transport registration on both loaders. See [Parity Traceability Matrix](.brainbox/plans/traceability-migration-parity.md) for detailed anchors and verification evidence. |
| 4 Shared Modules | complete | Shared modules finalized, including processing-core, world-query, drop-core, substitution-core, illumination-core, inventory-core, tree-core, farming-core, sync-core, policy-core, and harvest-core. |
| 5 Unit Tests | complete | Deterministic service/runtime/policy/input/packet tests cover parity-critical behavior with final cross-feature policy assertions. |
| 6 Docs/CI/Release | complete | Documentation and verification gates are updated through M9, including `chiseledBuild` regression evidence and refreshed parity traceability. |

## Loader and Version Matrix

| Minecraft | Fabric | NeoForge |
| --- | --- | --- |
| 1.21.11 | yes | yes |
| 26.1.x | yes | yes |

## Architecture

The rewrite uses three namespaces:

- common: reusable game logic and services.
- fabric: Fabric-only adapters.
- neoforge: NeoForge-only adapters.

Shared modules introduced in this rewrite:

- processing-core
- world-query
- harvest-core
- drop-core
- illumination-core
- inventory-core
- substitution-core
- tree-core
- farming-core
- sync-core
- policy-core

For deeper implementation details, see [TECHNICAL.md](TECHNICAL.md).

Final strict traceability mapping is documented in [.brainbox/plans/traceability-final-audit.md](.brainbox/plans/traceability-final-audit.md).

## Build

```bash
./gradlew chiseledBuild
```

Build a single active node by switching Stonecutter active version and running:

```bash
./gradlew build
```

## Release Packaging

```bash
./gradlew chiseledPackageRelease
```

Packaged release jars are staged in releases/.

## License

MIT

## Credits

- Original mod and modernization effort: DuelMonster
- Build stack: Stonecutter, Modstitch, Gradle

## SupremeVantage Migration Note

The legacy SupremeVantage packet handler has been deprecated and removed from runtime logic. All SupremeVantage reward and code processing is now handled by SupremeVantagePacket and SupremeVantageService. See TECHNICAL.md for details.
