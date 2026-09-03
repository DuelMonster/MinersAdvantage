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

| Module            | Responsibility                                                  |
| ----------------- | --------------------------------------------------------------- |
| processing-core   | Tick budgets, queue pacing, cancellation boundaries.            |
| world-query       | Spatial helpers, bounds, and neighborhood lookups.              |
| harvest-core      | Harvest cadence and maturity/durability decision support.       |
| drop-core         | Drop capture/aggregation and flush behavior.                    |
| illumination-core | Torch placement planning and low-light response.                |
| inventory-core    | Inventory checks, consumption gates, and availability queries.  |
| substitution-core | Tool ranking, rule matching, and switch-back policy.            |
| tree-core         | Trunk/leaf traversal and tree-shape analysis.                   |
| farming-core      | Hydration and farmland eligibility logic.                       |
| sync-core         | Typed config snapshot sync and effective per-player state.      |
| policy-core       | Validation, clamping, and server-authoritative gameplay policy. |
| input-core        | Client action state transitions and packet intent routing.      |
| supreme-vantage   | Deterministic hidden-code reward progression logic.             |

## Configuration System

### Split Roots and Persistence

Configuration is stored as split TOML roots:

- `minersadvantage-client-config.toml`: client-only presentation, UX options, and debug logging toggle
- `minersadvantage-server-config.toml`: gameplay options that can be server-authoritative

Persistence and parsing are implemented through `MATomlConfigStore`, which reads/writes flattened key-value TOML and applies parsing fallback + clamping rules.

### Runtime Config Models

- `MAClientRootConfig`: explicit client root
- `MAServerRootConfig`: explicit server gameplay root
- `SyncedClientConfig`: transport/effective snapshot shape used for runtime sync

`MAServerRootConfig.withFeatureEnabled(FeatureId, boolean)` returns a copy with a single feature's `enabled` flag replaced and every other value preserved. It is the only supported way to flip a feature flag outside the config screen, and it returns the same instance when the value is already correct so no redundant save is triggered.

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

Each block-affecting feature config (Cropination, Cultivation, Excavation, Illumination, Lumbination, Pathanation, Shaftanation, Veination, Ventilation) also controls its own agent scheduler behavior: an enforce-limit flag and a maximum concurrent-instance cap, both applied by `AgentManager` across its active and pending queues. When the enforce-limit flag is disabled, that feature may queue an unlimited number of concurrent agents regardless of the max value. Defaults enable enforcement and allow four concurrent instances per agent type. The in-game configuration screen exposes these controls inline within each feature's own settings screen, under a `Scheduling` section. Captivation and Substitution don't affect blocks and don't expose these controls; they use a fixed internal cap instead.

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

### Feature Toggle Keybinds

`KeyBindings.all()` declares one unbound toggle spec per `FeatureId`. Specs with a `null` default key are still registered, using `InputConstants.UNKNOWN`, so they appear in the vanilla Controls screen without stealing a key.

When a toggle fires, `ClientInputService` flips the in-memory feature state and emits a `ComponentTogglePacket`. Both loader tick handlers then call `ClientActionInputSupport.applyFeatureToggles`, which shows the player an action-bar confirmation and, in single player only, writes the new `enabled` flag through `MAServerRootConfig.withFeatureEnabled`. Persistence is skipped entirely when the client option `disable_keybind_config_persistence` is set, and in multiplayer, where the server config stays authoritative.

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

- The build config uses Java 21 for `1.21.11` nodes and Java 25 for `26.x` nodes.
- VS Code debug tasks run through `scripts/run-gradle-java25.ps1`, which sets `JAVA_HOME` to a detected JDK 25 install for Gradle/plugin resolution.
- Set `JAVA25_HOME` (or `JDK25`) to override auto-detection if your JDK 25 location is custom.
- CI uses the runner `JAVA_HOME`. For local development, you can optionally uncomment `org.gradle.java.home` in [gradle.properties](gradle.properties) to pin a specific JDK path.

### Stonecutter Version Matrix

Configured nodes:

- `1.21.11-fabric`
- `1.21.11-neoforge`
- `26.1.2-fabric`
- `26.1.2-neoforge`
- `26.2-fabric`
- `26.2-neoforge`

Node-specific properties live under `versions/<node>/gradle.properties`. That file is the only thing a node directory owns; everything else under `versions/` is generated build output and is not tracked.

### Source Layout

All Java sources, including loader-specific ones, live in the single shared tree at `src/main/java` and are preprocessed by Stonecutter. Never add sources under `versions/<node>/src` — those bypass preprocessing and silently drift between nodes. `validate-compile-matrix` fails immediately if any `versions/<node>/src` directory exists, before it runs any Gradle task and regardless of `MA_SKIP_COMPILE_MATRIX`.

Loader- and version-specific code is expressed with Stonecutter comment conditions:

| Condition           | Meaning                                                 |
| ------------------- | ------------------------------------------------------- |
| `//? if fabric {`   | Fabric nodes only.                                      |
| `//? if neoforge {` | NeoForge nodes only.                                    |
| `//? if mc1 {`      | Minecraft `1.21.x` nodes only.                          |
| `//? if mc26 {`     | Minecraft `26.x` nodes only.                            |
| `//? if >=26.2 {`   | Version predicate, compared against the node's version. |

The `fabric`/`neoforge` and `mc1`/`mc26` constants are declared in [build.gradle.kts](build.gradle.kts); version predicates need no declaration.

A file that only applies to one loader wraps its whole body in a single condition, with a short note in the `else` branch. `FabricNetworkEvents`, `NeoForgeNetworkEvents`, `NeoForgeClientEvents` and `NeoForgeBreakEvents` all follow that shape.

Stonecutter comments out the inactive branch, so an inactive block cannot contain `/* */` comments of its own. Nested inactive branches use `/^ ^/` instead, and loader-specific files avoid Javadoc for the same reason. Editing these files by hand is easier if you keep the branch that is true for the active node uncommented.

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
./gradlew :26.2-fabric:runClient
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
- See [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md) for end-to-end pre-release and publish gating.

Release notes uploaded to Modrinth and CurseForge are not the whole `CHANGELOG.md`. At configuration time the build queries the public Modrinth version list (`https://api.modrinth.com/v2/project/<id>/version`) for the highest `version_number` already released, strips the `+<mc>-<loader>` suffix, and uploads every `## <version>` section newer than it. That covers the case where `mod_version` is bumped several times between publishes. The lookup runs once per build and is shared across all Stonecutter nodes.

CurseForge is not used as the source of truth: the upload API authenticates uploads but cannot be queried for existing files with the upload token, so it would require a separate CurseForge Core API key.

If the lookup fails (offline, missing/blank project ID, unpublished project), the build logs a notice and falls back to the newest `## <version>` section only.

Preview what would be uploaded without publishing:

```bash
./gradlew :1.21.11-fabric:printReleaseChangelog
```

The post-2.21 optimization replay extension (Commits 15-20) is closed in `2.22.0`.
Runtime optimization commits keep mandatory in-game stop-and-wait gates; docs-only synchronization commits proceed after standard validation gates pass.

## License

MIT - see [LICENSE.md](LICENSE.md) for the full text.

## Credits

- DuelMonster (original mod and rewrite ownership)
- Stonecutter and Modstitch maintainers
- Fabric, NeoForge, Cloth Config, and ModMenu communities