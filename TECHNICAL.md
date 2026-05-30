# TECHNICAL

## Overview

This repository is a source-driven rewrite of Miners Advantage with a hard reset policy:

- no legacy migration code
- no compatibility adapters for old config formats
- shared reusable logic extracted into common services

## Build Instructions

Run all-node verification from repository root:

```bash
./gradlew chiseledBuild
```

For a single active Stonecutter node, set the active version and run:

```bash
./gradlew build
```

## Diagnostic Logging

Miners Advantage now provides centralized project-wide diagnostic logging through `uk.co.duelmonster.minersadvantage.common.log.LogUtils`.

Enable it by adding `-Dminersadvantage.debugLogging=true` to the JVM args for client or server runs.

When enabled, startup attempts to raise the active logging backend to `DEBUG` through reflection against Log4j2 or Logback and emits debug traces for:

- loader bootstrap
- player login/logout
- feature dispatch and config sync handling
- Fabric and NeoForge tool/use event triggers
- agent creation, queueing, completion, and manager cleanup

## Build System

### Core stack

- Gradle Kotlin DSL
- Stonecutter for node slicing
- Modstitch for unified Fabric and NeoForge wiring

### Nodes

| Node | Minecraft | Loader | Properties file |
| --- | --- | --- | --- |
| 1.21.11-fabric | 1.21.11 | Fabric | versions/1.21.11-fabric/gradle.properties |
| 1.21.11-neoforge | 1.21.11 | NeoForge | versions/1.21.11-neoforge/gradle.properties |
| 26.1.2-fabric | 26.1.2 | Fabric | versions/26.1.2-fabric/gradle.properties |
| 26.1.2-neoforge | 26.1.2 | NeoForge | versions/26.1.2-neoforge/gradle.properties |

### Aggregate tasks

- chiseledBuild
- chiseledPublishAll
- chiseledPackageRelease

## Modernization Progress

### Current Status (v0.3.0)

As of 2026-05-11, milestone execution and verification gates confirm parity wiring across six areas:

1. **Bootstrap** — Both Fabric and NeoForge now bootstrap MinersAdvantageCore across all nodes
2. **Client Input** — Keybindings are registered and client tick polling is wired; input-driven toggle and abort packets now traverse the transport layer
3. **Server Events** — Login/logout, level unload, and entity load/join dispatch are wired across loaders, with NeoForge block tool modification event wiring and Fabric tool dispatch routed via interaction callbacks (no dedicated Fabric tool-modification event)
4. **Networking** — Transport is wired for component toggles, aborts, player sync snapshots, feature dispatch packets, and SupremeVantage packets on both loaders
5. **Config UI** — Cloth Config screen builder is implemented with dedicated client/common/feature categories, Fabric ModMenu factory is wired, and NeoForge config screen extension point registration is active. Remote multiplayer exposes server-authoritative gameplay options as read-only while client-local options remain editable.
6. **Assets** — Base localization/resource structure is migrated with `en_us.json` and `ru_ru.json`, including rewrite-era keybinding/localization keys

For detailed source-level traceability and migration tasks, see [Parity Traceability Matrix](.brainbox/plans/traceability-migration-parity.md).

### Runtime Parity Sweep (2026-05-25)

An additional runtime-consumption sweep completed on 2026-05-25 wired previously inert config paths into active runtime behavior and aligned Fabric/NeoForge dispatch handling more closely.

Highlights:

- Common settings now affect runtime processing and dispatch paths (`mineVeins`, `gatherDrops`, `autoIlluminate`, tick-delay/TPS orchestrator wiring)
- Feature agents were refactored to consume effective per-player config in active code paths (captivation, cropination, cultivation, excavation, illumination, lumbination, pathanation, shaftanation, ventilation, veination)
- Veination allowlists are now enforced at trigger and discovery levels (`ores`, `pickaxeBlacklist`)
- Substitution ATTACK context routing now exists on both loaders, including `ignorePassiveMobs` and `ENTITY_TYPE` selection-rule matching
- NeoForge trigger flow now includes right-click item/block and attack substitutions plus broader feature dispatch parity routes

Validation status:

- `./scripts/validate-compile-matrix.ps1` passed for:
	- `1.21.11-fabric`
	- `1.21.11-neoforge`
	- `26.1.2-fabric`
	- `26.1.2-neoforge`

Known loader-specific caveat:

- NeoForge `26.1.2` does not expose the same `BlockEvent.BreakEvent` type used in newer mappings, so equivalent mining-trigger feature routing is handled in `LeftClickBlock` for cross-node compatibility.

### Modernization Plan

The 9-phase governance-enforced milestone plan (started 2026-05-11) is now complete:

- M1: Baseline & governance setup
- M2: Bootstrap completion (NeoForge + Fabric client)
- M3: Keybindings & client input runtime
- M4: Networking transport layer (completed)
- M5: Server events & dispatch (completed)
- M6: Config UI (Cloth Config + ModMenu + NeoForge) (completed)
- M7: Assets & localization (completed)
- M8: Documentation signoff (completed)
- M9: Verification & regression gate (completed)

All commits follow semantic commit standards, CHANGELOG discipline, version bump rules, and optimization passes. Each milestone has explicit governance gates.

---

## Code Architecture

### Package split

- uk.co.duelmonster.minersadvantage.common
- uk.co.duelmonster.minersadvantage.fabric
- uk.co.duelmonster.minersadvantage.neoforge

### Lifecycle contract

All components implement:

- register
- enable
- disable
- tick
- cleanup

Descriptor-driven registration is handled by ComponentRegistry.

### Shared modules and ownership

| Module | Class | Responsibility |
| --- | --- | --- |
| processing-core | ProcessingCoreService | queue limits, per-tick budget, cancellation through clear |
| world-query | WorldQueryService | geometry bounds and area helpers |
| harvest-core | HarvestCoreService | maturity and durability cadence helpers |
| drop-core | DropCoreService | capture and flush semantics for drops |
| illumination-core | IlluminationCoreService | auto/manual torch placement planning and depletion signaling |
| inventory-core | InventoryCoreService | inventory presence and consumption |
| substitution-core | SubstitutionCoreService | mining/combat tool policy ranking and switch-back decisions |
| tree-core | TreeCoreService | trunk/leaf heuristics |
| farming-core | FarmingCoreService | hydration proximity checks |
| cropination-core | CropinationCoreService | crop maturity, replant, and durability action decisions |
| lumbination-core | LumbinationCoreService | trunk/leaf traversal planning and sapling replant intent |
| captivation-core | CaptivationCoreService | item capture eligibility, GUI gate, and radius policy |
| sync-core | SyncCoreService | typed per-player client/server config snapshots and effective merged state |
| policy-core | PolicyCoreService | config range clamping and server-authoritative gameplay sanitization |
| input-core | ClientInputService | client key-action state transitions and toggle packet intent generation |
| supreme-vantage | SupremeVantageService | hidden code tracking and deterministic reward sequence progression |

### Config model

- `MAClientRootConfig` and `MAServerRootConfig` are the first-class split roots used across storage, sync, and UI paths.
- TOML is the canonical persisted format with separate client and server files under the Miners Advantage config directory.
- `SyncedClientConfig` remains as the transport/effective snapshot shape while split roots drive persisted state.
- Legacy JSON config cleanup paths and legacy `SyncType.ClientConfig` synchronization handling have been removed.

### Substitution Rule Engine

`SubstitutionConfig` includes `selectionRules`, which drive action/target-aware rule resolution for the substitution pipeline.

Rule resolution order:

1. Filter by `action` (`ANY` matches all)
2. Filter by structural target match (`targetKind` + `targetId`)
3. Evaluate `targetExpression` (`AND` / `OR` / `NOT` with parentheses)
4. Choose best rule by `targetPriority`, then target specificity

Candidate selection then applies the autoswitch-style comparator:

1. `targetPriority`
2. `targetMatch` multi-level rating
3. `toolPriority`
4. `toolMatch` multi-level rating
5. currently selected slot
6. smallest slot index

Expression atoms currently supported by the substitution parser:

- Target atoms:
	- `block_tag:<id>`
	- `target_block:<namespace:id>`
	- `action:<name>`
	- `requires_correct_tool`
	- `mineable_pickaxe`, `mineable_axe`, `mineable_shovel`, `mineable_hoe`
- Tool atoms:
	- `tool_kind:<kind>` (`pickaxe`, `axe`, `shovel`, `hoe`)
	- `item:<namespace:id>`
	- `action:<name>`
	- `correct_tool`
	- `mining_enchantable`
	- `required_kind`

Per-rule data selectors:

- `minSilkTouch`
- `minFortune`
- `requireMending`
- `denyMending`

If both `requireMending` and `denyMending` are set in a rule, config sanitization keeps `requireMending` and clears `denyMending`.

Invalid expressions are rejected for that rule match path and logged via `LogUtils` when evaluated.

### Input surface

- `KeyBindings` now carries a loader-neutral catalog of legacy-parity client actions and default keys.
- `ClientInputService` models toggle semantics for feature enablement, excavation hold/toggle modes, shaft vent hold state, illumination actions, and abort requests.
- `ComponentTogglePacket` is now handled by `MinersAdvantageCore` so input-driven feature enablement has a real runtime path.

### SupremeVantage

- Decision: preserve.
- The rewrite keeps the hidden excavation code path and represents rewards as deterministic `RewardGrant` data through `SupremeVantageService` and `SupremeVantagePacket`.

## Parity Signoff

- Final strict appendix traceability mapping is recorded in [.brainbox/plans/traceability-final-audit.md](.brainbox/plans/traceability-final-audit.md).
- Milestone 6 closure includes explicit cross-feature policy assertions in [PolicyCoreServiceTest.java](src/test/java/uk/co/duelmonster/minersadvantage/common/services/PolicyCoreServiceTest.java).
- Full matrix validation is confirmed by `./gradlew chiseledBuild` during final signoff.

## Shape Addon API

Addon shape registration and processor authoring guidance is documented in [SHAPE_API.md](SHAPE_API.md).
Runtime agent dispatch and held-key preview now share `MAShapeDimensions` context sizing helpers so client previews match server execution geometry.

## Shape Manual Verification Checklist

Use this checklist when validating gameplay parity in a debug client.

- Enable Excavation and Shaftanation, then confirm shape cycle keybinds change the selected shape for each feature independently.
- Hold the Excavation mode key and verify preview particles move to the newly selected Excavation shape without requiring a reconnect.
- Hold the Shaft/Vent key and verify preview particles move to the newly selected Shaftanation shape.
- Trigger Excavation on a horizontal block face and confirm broken blocks match the held-key preview footprint.
- Trigger Shaftanation on a horizontal face and confirm carved blocks follow the selected shape (shaft/stair up/stair down).
- Trigger Shaft/Vent on a vertical face and confirm ventilation behavior still wins where expected and does not desync selected Shaftanation shape state.
- Recycle shape indexes past the last shape in each feature and confirm wrap-around behavior returns to index `0`.
- Restart client, rejoin world, and confirm selected shape indexes resync from client state updates after first key interaction.

## Feature descriptors

The core bootstrap registers these components:

- CAPTIVATION
- CROPINATION
- CULTIVATION
- EXCAVATION
- PATHANATION
- ILLUMINATION
- LUMBINATION
- SHAFTANATION
- SUBSTITUTION
- VEINATION
- VENTILATION

## Unit Testing Strategy

Tests are unit-only and currently target deterministic services:

- queue boundaries and tick budgets
- geometry helpers
- substitution ranking preference behavior
- illumination placement decisions
- policy clamping and overrides
- drop capture/flush semantics
- component bootstrap coverage
- farming/harvest/captivation runtime planning and decision outputs
- illumination manual placement and inventory depletion outputs
- substitution combat/mining policy and switch-back outputs
- config override merging, typed sync snapshots, and player sync packet flow
- input toggle state transitions, keybinding metadata, and SupremeVantage reward packet flow

## CI and Release

- CI workflow builds all nodes through chiseledBuild
- Release workflow builds, stages jars, and publishes artifacts

## License

MIT

## Credits

- DuelMonster
- Stonecutter, Modstitch, Gradle contributors

### SupremeVantage Migration Note

The legacy SupremeVantage packet handler (`PacketSupremeVantage`) has been deprecated and removed. All SupremeVantage reward logic and packet transport is now handled by `SupremeVantagePacket` and `SupremeVantageService`, which are registered and dispatched in a loader-neutral manner. This ensures deterministic reward progression and test coverage across all supported nodes.

### Intentional Compatibility No-Ops

Two worker compatibility types intentionally keep default no-op tick behavior:

- `Agent` default `tick(Object worldContext)`
- `AbstractAgent` inherited `tick(Object worldContext)`

These defaults are retained only as migration scaffolding for legacy extension points and do not participate in the modern component dispatch path. Active runtime behavior is implemented through feature components and core services.
