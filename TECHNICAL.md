# TECHNICAL

## Overview

MinersAdvantage is a modern, shared-core rewrite targeting Fabric and NeoForge across multiple Minecraft versions from one source tree.

The codebase prioritizes:

- loader-neutral gameplay logic in `common`
- thin platform adapters in loader modules
- deterministic service-driven behavior and unit testing
- split configuration roots with multiplayer server authority

## Architecture

### Namespace Layout

- `uk.co.duelmonster.minersadvantage.common`: shared gameplay logic, config, networking contracts, services, and orchestration.
- `uk.co.duelmonster.minersadvantage.fabric`: Fabric bootstrap and integration adapters.
- `uk.co.duelmonster.minersadvantage.neoforge`: NeoForge bootstrap and integration adapters.

### Component Model

Feature behavior is descriptor-driven and lifecycle-managed through core orchestration. Components are registered once, enabled/disabled based on effective config and context, and then ticked through shared processing budgets.

Primary gameplay components:

- Captivation
- Cropination
- Cultivation
- Excavation
- Pathanation
- Illumination
- Lumbination
- Shaftanation
- Substitution
- Veination
- Ventilation

### Shared Service Modules

The rewrite extracts behavior into reusable service modules to avoid loader-specific divergence.

| Module              | Responsibility                                                                 |
| ------------------- | ------------------------------------------------------------------------------ |
| processing-core     | Tick budgets, queue pacing, cancellation boundaries.                           |
| world-query         | Spatial helpers, bounds, and neighborhood lookups.                             |
| harvest-core        | Harvest cadence and maturity/durability decision support.                      |
| drop-core           | Drop capture/aggregation and flush behavior.                                   |
| illumination-core   | Torch placement planning and low-light response.                               |
| inventory-core      | Inventory checks, consumption gates, and availability queries.                 |
| substitution-core   | Tool ranking, rule matching, and switch-back policy.                           |
| tree-core           | Trunk/leaf traversal and tree-shape analysis.                                  |
| farming-core        | Hydration and farmland eligibility logic.                                      |
| sync-core           | Typed config snapshot sync and effective per-player state.                     |
| policy-core         | Validation, clamping, and server-authoritative gameplay policy.                |
| input-core          | Client action state transitions and packet intent routing.                     |
| supreme-vantage     | Deterministic hidden-code reward progression logic.                            |

## Configuration System

### Split Roots and Persistence

Configuration is stored as split TOML roots:

- `minersadvantage-client-config.toml`: client-only presentation and UX options
- `minersadvantage-server-config.toml`: gameplay options that can be server-authoritative

Persistence and parsing are implemented through `MATomlConfigStore`, which reads/writes flattened key-value TOML and applies parsing fallback + clamping rules.

### Runtime Config Models

- `MAClientRootConfig`: explicit client root
- `MAServerRootConfig`: explicit server gameplay root
- `SyncedClientConfig`: transport/effective snapshot shape used for runtime sync

Gameplay config categories in the server root:

- common
- captivation
- cropination
- cultivation
- excavation
- pathanation
- illumination
- lumbination
- shaftanation
- substitution
- veination
- ventilation

### Validation and Clamping

TOML ingestion applies type-safe parsing with range clamping where required. Representative constraints include:

- per-tick and dimension bounds on excavation/shaft/vent operations
- light-level clamp for illumination
- vein distance and harvest modifier bounds for veination
- safe enum fallback for shaft torch placement

If parsing fails, fallback defaults are applied and warnings are emitted without crashing runtime.

### Substitution Rules

Substitution supports rule-based selection profiles using `SelectionRule` entries with:

- action scope (`BREAK`, `INTERACT`, `ATTACK`, `STAT_CHANGE`, `ANY`)
- target kind (`BLOCK_TAG`, `ENTITY_TYPE`, `ANY`)
- priority layers (`targetPriority`, `toolPriority`)
- expression fields (`targetExpression`, `toolExpression`)
- enchant gating (`minSilkTouch`, `minFortune`, `requireMending`, `denyMending`)

When `requireMending` and `denyMending` are both true, sanitization keeps `requireMending` and clears `denyMending`.

## Runtime Data Flow

### Client Input to Gameplay Effect

1. Client key/input actions are captured by input-core services.
2. Intent packets are sent through loader transport registration.
3. Core dispatch resolves feature/component action against effective config.
4. Processing-core executes work within per-tick budgets.
5. Related services (drops, lighting, substitution, sync) are called as needed.

### Multiplayer Authority Model

- Client-local options remain editable on the client.
- Gameplay options are server-authoritative in multiplayer contexts.
- Effective runtime snapshots are synchronized per player through typed sync pathways.

## Networking and Sync

Networking is loader-wired and covers:

- component toggle and abort flows
- player config synchronization
- feature dispatch packets
- SupremeVantage packet transport

The runtime design keeps packet payload handling loader-neutral in common services while adapters own registration and platform hooks.

## Shape and Geometry Surfaces

Shape selection and preview/dispatch behavior share geometry sizing context so held-key preview and server execution footprints remain aligned. Addon extension details are documented in [SHAPE_API.md](SHAPE_API.md).

## Testing and Verification

Unit tests target deterministic service behavior and parity-critical logic, including:

- queue and tick budget behavior
- substitution ranking/policy outcomes
- illumination placement decisions
- farming/harvest decision outputs
- policy clamping and override behavior
- sync snapshot shaping and packet-driven flow

The repository also provides matrix validation scripts for all Stonecutter nodes and documentation validation scripts enforced by hooks.

## Build Instructions

### Prerequisites

1. Git
2. JDK 21 and JDK 25 available locally
3. Gradle via wrapper (`gradlew` / `gradlew.bat`)

Notes:

- The build config uses Java 21 for `1.21.11` nodes and Java 25 for `26.1.2` nodes.
- CI uses the runner `JAVA_HOME`. For local development, you can optionally uncomment `org.gradle.java.home` in [gradle.properties](gradle.properties) to pin a specific JDK path.

### Stonecutter Version Matrix

Configured nodes:

- `1.21.11-fabric`
- `1.21.11-neoforge`
- `26.1.2-fabric`
- `26.1.2-neoforge`

Node-specific properties live under `versions/<node>/gradle.properties`.

### Build All Nodes

```bash
./gradlew chiseledBuild
```

### Run a Development Client

Use the run task for a specific node:

```bash
./gradlew :<version>-<loader>:runClient
```

Examples:

```bash
./gradlew :1.21.11-fabric:runClient
./gradlew :26.1.2-neoforge:runClient
```

### Run Tests

```bash
./gradlew test
```

### Produce Release Jars

Per-node build pipelines finalize with `packageRelease`, and all-node packaging is available through:

```bash
./gradlew chiseledPackageRelease
```

Release jars are copied into the repository `releases/` directory.

### Target a Specific Active Node

Stonecutter active node is controlled in [stonecutter.gradle.kts](stonecutter.gradle.kts):

```kotlin
stonecutter active "1.21.11-fabric"
```

After changing the active node, a regular build targets that node:

```bash
./gradlew build
```

## CI and Release Notes

- `chiseledBuild` is the canonical multi-node compile gate.
- `chiseledPublishAll` and publication blocks support artifact publishing workflows.
- `packageRelease` tasks copy remapped/production jars into `releases/`.

## License

MIT - see [LICENSE.md](LICENSE.md) for the full text.

## Credits

- DuelMonster (original mod and rewrite ownership)
- Stonecutter and Modstitch maintainers
- Fabric, NeoForge, Cloth Config, and ModMenu communities