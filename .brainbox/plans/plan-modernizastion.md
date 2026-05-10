## Plan: MinersAdvantage Modernization and Full Rewrite (Source-Driven)

Goal
- Rewrite MinersAdvantage into a clean, modern Stonecutter + Modstitch project with full behavioral parity for all existing gameplay features.
- Supported runtime matrix: 1.21.11 and 26.1.x, each with Fabric and NeoForge.
- Hard reset policy: no backward compatibility, no legacy config migration, no fallback adapters.
- Test policy: unit tests only.
- Architectural rule: any reusable logic must exist in shared services, never trapped inside one component.

## Plan Execution Governance (.brainbox)

This governance applies to every phase in this document.

Execution checklist companion file
1. Required companion checklist: `.brainbox/plans/plan-modernization-execution-checklist.md`.
2. All phase work must be tracked in that checklist while work is in progress.
3. A phase cannot be marked complete in this plan unless its checklist section is fully completed with evidence notes.

Mandatory enforcement rules
1. Treat `.brainbox/guides/**` and `.brainbox/rules/**` as hard requirements during plan execution.
2. Before starting each phase, explicitly review applicable guides/rules and list them in the phase worklog/checklist.
3. During implementation, if a planned step conflicts with a guide/rule, update the step to comply before coding continues.
4. During phase signoff, include a compliance check that confirms all applicable guides/rules were followed.
5. Do not close any phase until the compliance check is completed and recorded.

Execution checkpoints
1. Phase kickoff checkpoint: identify relevant `.brainbox/guides` and `.brainbox/rules` entries.
2. In-phase checkpoint: verify ongoing implementation remains compliant.
3. Phase completion checkpoint: document compliance evidence in the phase acceptance notes.
4. Checklist checkpoint: update `.brainbox/plans/plan-modernization-execution-checklist.md` at kickoff, during execution, and at signoff for every phase.

## Phase 0: Repository Reset and Fresh Working Base

This phase is mandatory and happens first.

1. Create a new branch named develop from current HEAD.
2. Create releases/original if missing.
3. Move all current project files into releases/original so the root becomes a fresh modernization workspace.
4. Preserve git history by using git mv for tracked files.
5. Keep .git metadata untouched.
6. Ensure no recursive move of releases/original into itself.
7. Ensure releases is git-ignored immediately after move by adding releases/ to .gitignore.
8. Force archived originals to be untracked after the move:
   - run git rm -r --cached releases/original
   - keep files physically present on disk under releases/original
9. Validate structure and git state after move:
	- releases/original contains old project state.
	- root contains only new modernization scaffold files created in later phases.
	- git status shows no tracked content under releases/original.

Recommended execution sequence
1. git checkout -b develop
2. mkdir -p releases/original
3. Move tracked files with git-aware operations into releases/original
4. Move remaining untracked project files into releases/original
5. Add releases/ to .gitignore
6. Run git rm -r --cached releases/original
7. Confirm clean top-level baseline for rewrite scaffolding

Acceptance criteria
1. Branch is develop.
2. Legacy code is fully archived in releases/original.
3. releases/ is ignored by git.
4. All files under releases/original are untracked.
5. New root is clean and ready for modernized project bootstrap.
6. Applicable `.brainbox/guides` and `.brainbox/rules` were reviewed and enforced during execution.

## Phase 1: Modern Build System Foundation

Forget-Me-Crops reference guidance for this phase
1. Mirror Stonecutter + Modstitch bootstrap patterns from Forget-Me-Crops before introducing MinersAdvantage-specific changes.
2. Use Forget-Me-Crops layout as the default reference for settings.gradle.kts, stonecutter.gradle.kts, central build.gradle.kts, and versions/*/gradle.properties split.
3. Reuse aggregate task naming/intent patterns (all-node build, publish, packaging) and adapt only identifiers that are mod-specific.
4. Reuse metadata template token strategy from Forget-Me-Crops for fabric/neoforge template generation.

1. Replace legacy ForgeGradle/Groovy with Kotlin DSL and Modstitch central scripting.
2. Add Stonecutter project model with four nodes only:
	- 1.21.11-fabric
	- 1.21.11-neoforge
	- 26.1.x-fabric
	- 26.1.x-neoforge
3. Keep shared values in root gradle.properties.
4. Keep node-specific dependencies in versions/*/gradle.properties.
5. Add template-driven metadata generation for both loaders from src/main/templates.
6. Add aggregate multi-node tasks for build, publish, and release packaging.
7. Add CI workflows for build and publish across all modern nodes.

Acceptance criteria
1. Each node builds independently.
2. Aggregate all-node build succeeds.
3. Published metadata reflects correct loader/version matrix.
4. Applicable `.brainbox/guides` and `.brainbox/rules` were reviewed and enforced during execution.

## Phase 2: Rewrite Architecture for Reuse and Extensibility

Forget-Me-Crops reference guidance for this phase
1. Use Forget-Me-Crops common-vs-loader separation as the baseline for package boundaries and platform adapters.
2. Copy the spirit of its modern config ergonomics and synchronization flow, then map MinersAdvantage-specific categories and policies.
3. Prefer the same style of centralized helpers/services over feature-local utilities when patterns overlap.
4. Follow its conventions for minimizing loader-specific logic leakage into common modules.

Target package layout
1. common: pure gameplay logic and reusable services.
2. fabric: Fabric bootstrap/adapters only.
3. neoforge: NeoForge bootstrap/adapters only.

Core architecture contracts
1. Component lifecycle contract:
	- register
	- enable
	- disable
	- handle event hooks
	- tick if needed
	- cleanup
2. Shared service layer (must be consumed by all components):
	- traversal and flood-fill service
	- area and geometry service
	- tool and weapon scoring service
	- placement service for torches/ladders/paths
	- drop interception and respawn service
	- config policy and validation service
	- per-player runtime state service
	- cross-component orchestration/event bus service
3. Component registration must be descriptor-driven, not custom one-off boot wiring.

Strict anti-duplication rule
1. If logic is used by 2+ components, it must be moved to shared services immediately.
2. Component packages may contain only feature policy and orchestration.

Phase completion compliance rule
1. Record `.brainbox/guides`/`.brainbox/rules` checks in phase notes before marking architecture work complete.

## Phase 3: Source-Derived Parity Deep Dive and Rewrite Requirements

The following requirements come directly from current source behavior and must be replicated.

### Global runtime behavior parity

1. Client->server sync loop parity:
	- Sync player variable state regularly.
	- Sync player config regularly.
2. Per-player runtime flags parity:
	- excavation toggle
	- single-layer toggle
	- shaft/vent toggle
	- active processing states for each feature
3. Agent-style asynchronous processing parity:
	- blocks per tick limit
	- block limit cap
	- optional TPS guard timing behavior
	- cancellation/abort support
4. Drop interception parity:
	- while feature workers run, captured item and XP spawns are recorded then respawned.
	- gatherDrops behavior controls spawn-at-origin vs native spawn.
5. Hunger guard parity:
	- processing can stop when player is effectively starving.
6. Particle/sound progress feedback parity for active processing.
7. Server override policy parity:
	- per-feature server enforcement switches must exist.

### Captivation parity

Required behavior
1. Auto-pickup both ItemEntity and ExperienceOrb within configured box.
2. Independent horizontal and vertical radii.
3. Respect allow-in-GUI behavior.
4. Respect whitelist/blacklist mode logic.
5. Respect unconditional blacklist behavior so blacklisted items can be blocked even on direct pickup.

Config parity to preserve
1. enabled
2. allowInGUI
3. radiusHorizontal
4. radiusVertical
5. isWhitelist
6. unconditionalBlacklist
7. blacklist item list

### Cropination parity (Crop Harvest + Auto Till split)

Sub-feature A: Cropination harvest/replant
1. Triggered on hoe interaction with crop-like plantables.
2. Mature-only harvest checks based on age properties.
3. Replant simulation removes one seed-equivalent from drops/inventory as required.
4. Optional harvestSeeds behavior.
5. Applies reduced durability cadence for harvesting runs.

Sub-feature B: Cultivation tilling
1. Triggered on hoe till action on dirt-like blocks.
2. Water-source-aware area cultivation based on nearby hydration source search.
3. Converts to moist farmland where valid.

Config parity to preserve
1. enabled
2. harvestSeeds

### Excavation parity

Required behavior
1. Connected block excavation with area limits and queue processing.
2. Single-layer mode that constrains excavation to origin Y.
3. Toggle-mode and hold-mode behavior parity.
4. Ignore-block-variants option parity.
5. Block whitelist/blacklist policy parity.
6. Tool correctness checks for drops.
7. Optional ore-vein delegation when common mineVeins is enabled.
8. Works with auto-illumination orchestration when enabled.

Config parity to preserve
1. enabled
2. toggleMode
3. ignoreBlockVariants
4. isBlockWhitelist
5. blockBlacklist

### Pathanation parity

Required behavior
1. Triggered from shovel flatten workflow.
2. Uses path width and path length geometry aligned to facing direction.
3. Converts valid dirt-like blocks into path blocks.
4. Supports up/down terrain progression behavior consistent with current implementation intent.

Config parity to preserve
1. enabled
2. pathWidth
3. pathLength

### Illumination parity

Required behavior
1. Manual single-torch placement mode.
2. Manual area illumination mode around player.
3. Auto-illumination dispatch after compatible worker completion.
4. Torch placement strategy support:
	- floor
	- left wall
	- right wall
	- both walls
5. Special-case wall placement behavior for ventilation-style flows.
6. Torch inventory tracking and depletion notification behavior.
7. Light-threshold placement logic parity.

Config parity to preserve
1. enabled
2. useBlockLight
3. lowestLightLevel

### Lumbination parity

Required behavior
1. Tree identification from log plus nearby leaf detection.
2. Trunk area inference and trunk connectivity processing.
3. Leaf harvesting policy controls:
	- destroyLeaves
	- leavesAffectDurability
	- useShearsOnLeaves
4. Sapling replant policy using drops/inventory matching.
5. Custom logs/leaves/axes lists respected.
6. Leaf and trunk range configuration respected.
7. Chop-below behavior preserved as configured.

Config parity to preserve
1. enabled
2. chopTreeBelow
3. destroyLeaves
4. leavesAffectDurability
5. replantSaplings
6. useShearsOnLeaves
7. leafRange
8. trunkRange
9. logs
10. leaves
11. axes

### Shaftanation parity

Required behavior
1. Directional shaft geometry by length, height, width.
2. Tool-correctness and queue processing.
3. Optional ore-vein delegation when common mineVeins is enabled.
4. Auto-illumination orchestration with configurable torch placement mode.

Config parity to preserve
1. enabled
2. shaftLength
3. shaftHeight
4. shaftWidth
5. torchPlacement

### Substitution parity

Required behavior
1. Block mining substitution:
	- ranks hotbar tools by speed and enchantment policy.
	- supports silk-touch preference ordering.
	- supports fortune preference ordering.
	- optional ignore-if-current-tool-valid behavior.
	- optional switch-back after action.
2. Combat substitution:
	- weapon comparison logic with damage and enchantment heuristics.
	- optional ignorePassiveMobs policy.
3. Blacklist filtering for tool candidates.

Config parity to preserve
1. enabled
2. switchBack
3. favourSilkTouch
4. favourFortune
5. ignoreIfValidTool
6. ignorePassiveMobs
7. blacklist

### Veination parity

Required behavior
1. Auto-ore vein mining on ore break trigger.
2. Ore recognition by tag plus configurable ore list.
3. Works both as direct feature and as delegation target from other features.

Config parity to preserve
1. enabled
2. ores

### Ventilation parity

Required behavior
1. Vertical shaft generation with configurable diameter and depth.
2. Supports direction inversion based on face-hit orientation.
3. Optional ladder placement workflow while excavating.
4. Ladder placement uses valid-face checks and inventory availability checks.
5. Optional ore-vein delegation when common mineVeins is enabled.
6. Auto-illumination orchestration with wall placement policy.

Config parity to preserve
1. enabled
2. ventDiameter
3. ventDepth
4. placeLadders

### Cross-component orchestration parity matrix

1. Excavation -> Veination when common mineVeins and ore detected.
2. Excavation -> Illumination when common autoIlluminate.
3. Shaftanation -> Veination when common mineVeins and ore detected.
4. Shaftanation -> Illumination when common autoIlluminate.
5. Ventilation -> Veination when common mineVeins and ore detected.
6. Ventilation -> Illumination when common autoIlluminate.
7. Client toggle/input state -> server packet dispatch -> worker processing pipeline.
8. Abort command cancels all active per-player workers and flushes drops.

### Legacy hidden behavior decision

SupremeVantage exists as hidden code-driven gear grant behavior in current source.

Decision required for rewrite scope
1. Preserve exactly as legacy parity.
2. Remove intentionally in clean reset.
3. Preserve behind explicit debug/developer flag only.

Default for this plan
1. Preserve behavior unless explicitly removed by design signoff.

## Phase 4: Modernized Implementation Structure

Forget-Me-Crops reference guidance for this phase
1. Treat Forget-Me-Crops service/helper extraction style as the primary modernization template when deciding what belongs in shared modules.
2. If a new shared module is ambiguous, compare with Forget-Me-Crops first and choose the structure closest to its established pattern.
3. Reuse naming and layering conventions where practical so cross-repo maintenance stays predictable.

Required new shared modules
1. processing-core: queue, timing guard, limits, cancellation.
2. world-query: area checks, connected positions, geometric ranges.
3. harvest-core: safe break operations, snapshot/update wrapper.
4. drop-core: capture/flush item and XP strategy.
5. illumination-core: torch candidate selection and placement policy.
6. inventory-core: item lookup, slot resolution, consumption helpers.
7. substitution-core: ranking and weapon comparison services.
8. tree-core: trunk/leaf analysis and replant logic.
9. farming-core: hydration and farm patch helpers.
10. sync-core: per-player state/config replication.
11. policy-core: server override enforcement.

Component implementation rule
1. Components call services; components do not own reusable algorithms.

## Phase 5: Unit Test Strategy (Unit Tests Only)

No runtime integration tests and no full game launch tests in CI.

Service-level unit tests
1. Traversal and queue boundaries.
2. Area geometry calculations for shaft/vent/path/excavate.
3. Tool ranking outcomes for substitution.
4. Torch and ladder placement validity checks.
5. Config validation and range enforcement.
6. Drop capture and flush semantics.

Component-level unit tests
1. Captivation filtering and pickup radius behavior.
2. Cropination mature harvesting and seed policy behavior.
3. Cultivation hydration-constrained till behavior.
4. Excavation single-layer and variant handling.
5. Pathanation direction, width, and length behavior.
6. Illumination single placement and area placement behavior.
7. Lumbination trunk/leaf policy combinations and sapling replant behavior.
8. Shaftanation dimensions and cross-calls.
9. Substitution mining and combat decisions.
10. Veination trigger and ore matching behavior.
11. Ventilation dimensions, direction, and ladder behavior.

Cross-component unit tests with mocked orchestration bus
1. Excavation, Shaftanation, Ventilation dispatch to Illumination under policy.
2. Excavation, Shaftanation, Ventilation dispatch to Veination under policy.
3. Abort flow stops workers and flushes recorded drops.

## Phase 6: Documentation, CI, and Release Flow

Forget-Me-Crops reference guidance for this phase
1. Use Forget-Me-Crops documentation scope split as a template: user-facing README, deep technical guide, and release-facing workflow notes.
2. Model CI job structure and ordering after Forget-Me-Crops multi-node validation flow, then adapt matrix values to MinersAdvantage targets.
3. Model publish automation and release packaging flow after Forget-Me-Crops, keeping platform metadata and artifact naming consistent with modern conventions.

1. README rewrite with modern loader/version matrix and all feature/sub-feature behavior notes.
2. TECHNICAL rewrite with architecture map, extension guide, and service ownership rules.
3. CI includes multi-node build and unit tests for all modern nodes.
4. Publish automation to Modrinth and CurseForge for Fabric and NeoForge artifacts.

## Forget-Me-Crops Reference Matrix

Use this matrix during implementation to map each modernization phase item to concrete reference files and sections from Forget-Me-Crops.

Reference root path for all files in this matrix: `D:/Mod_Source/Forget-Me-Crops`

Path usage rule for this section:
1. Treat every relative file path below as anchored to `D:/Mod_Source/Forget-Me-Crops`.
2. Resolve section anchors by the named headings/subsections in each referenced file.

| Phase item | Forget-Me-Crops reference | What to copy/adapt |
|---|---|---|
| Phase 1: Plugin/repository bootstrap | `settings.gradle.kts` (pluginManagement + plugins + stonecutter block) | Plugin repositories, Stonecutter plugin setup, centralized script model (`centralScript`). |
| Phase 1: Stonecutter controller and aggregate tasks | `stonecutter.gradle.kts` (`stonecutter active`, `chiseledBuild`, `chiseledPublishAll`, `chiseledPackageRelease`) | Active node control, cross-node aggregate task conventions, shared repository declarations for all nodes. |
| Phase 1: Central Modstitch build script | `build.gradle.kts` (`plugins`, `modstitch {}`, `loom {}`, `moddevgradle {}`, `mixin {}`, `dependencies`) | Unified loader abstraction via Modstitch, per-loader toolchain configuration, dependency wiring patterns. |
| Phase 1: Metadata tokenization | `build.gradle.kts` (`modstitch.metadata.replacementProperties`) + `src/main/templates/fabric.mod.json` + `src/main/templates/META-INF/neoforge.mods.toml` | Token-driven metadata generation and consistent property naming. |
| Phase 1: Version-node dependency split | `versions/1.21.11-fabric/gradle.properties` and `versions/1.21.11-neoforge/gradle.properties` | Node-local dependency pinning and `modstitch.platform` selection pattern. |
| Phase 1: JVM/gradle baseline | `gradle.properties` (root) | Shared properties only at root (`mod_version`, JVM args, parallelism), with node specifics outside root. |
| Phase 1: CI build matrix pattern | `.github/workflows/ci.yml` (`Build all Stonecutter nodes` step) | Single-command all-node validation flow (`chiseledBuild`) and artifact upload conventions. |
| Phase 1 and 6: Release/publish pipeline | `.github/workflows/release.yml` (`Build all Stonecutter nodes`, `Stage release JARs`, publish steps, GitHub release step) | Tag/manual release triggers, staged release jars, mod platform publish flow, release asset publishing. |
| Phase 2: Common-vs-loader separation | `TECHNICAL.md` (Architecture Overview table + Package Structure table) | Package boundary rules and responsibility split between shared logic and loader-specific adapters. |
| Phase 2: Platform abstraction and conditional wiring | `TECHNICAL.md` (Stonecutter Condition Syntax) + `build.gradle.kts` (`stonecutter.constants.match`) | Conditional source slicing strategy and loader-constant matching in build pipeline. |
| Phase 2: Config UX and synchronization style | `TECHNICAL.md` (Architecture + config-related sections) + `README.md` (user-facing config guidance) | Modern config organization, user-facing documentation style, and shared-client/server config expectations. |
| Phase 3: Algorithm deep-dive documentation format | `TECHNICAL.md` (Scan Model, Farm Maintenance, Crop Handling, Hoe Handling sections) | How to document feature internals with strict behavior-level detail and implementation rationale. |
| Phase 4: Shared service extraction style | `TECHNICAL.md` (Package Structure entries like `util/*`, `frame/*`, `platform/*`) | Decompose reusable logic into dedicated utility/service modules instead of feature-local duplication. |
| Phase 4: Runtime orchestration pattern | `TECHNICAL.md` (Frame Discovery and Registry + Scan Model + incremental execution sections) | Registry/task orchestration approach, per-tick work budgeting, and lifecycle management conventions. |
| Phase 5: Unit-test targeting guidance | `TECHNICAL.md` (sections describing deterministic helpers and scan units) | Build unit tests around deterministic service units and policy decisions, not full runtime launch tests. |
| Phase 6: User vs technical docs split | `README.md` (user-facing install/config/features) + `TECHNICAL.md` (internal architecture and algorithms) | Keep user docs and engineering docs distinct; mirror this split in MinersAdvantage rewrite docs. |
| Phase 6: Release artifact staging | `.github/workflows/release.yml` (`chiseledPackageRelease`, `files: releases/*.jar`) | Standardized release staging folder and release-attachment flow for built jars. |
| Traceability process support | `TECHNICAL.md` (high-detail sectioned documentation style) | Maintain one-to-one mapping between parity requirements and modern implementation notes during execution. |

## Final Verification Checklist

1. Branch and archive reset complete exactly as defined in Phase 0.
2. Build matrix complete for 1.21.11 and 26.1.x on both loaders.
3. Every feature and sub-feature listed in Phase 3 has explicit implementation task coverage.
4. Cross-component orchestration matrix behaviors are covered by unit tests.
5. No legacy compatibility code exists.
6. No reusable logic remains isolated within any component implementation.
7. `.brainbox/plans/plan-modernization-execution-checklist.md` is fully completed with evidence notes for all phases.

## Strict Traceability Appendix (Parity Requirement -> Source Anchor)

Forget-Me-Crops usage note
1. This appendix traces behavior parity to legacy MinersAdvantage source.
2. During implementation, pair each parity item with an explicit modern implementation reference in Forget-Me-Crops (build, architecture, config, CI, or release flow) to ensure modernization decisions are grounded in an existing working model.

Legend
- Each requirement below is bound to concrete current-source anchors.
- Rewrite acceptance requires preserving behavior represented by these anchors unless explicitly de-scoped.

### A. Global runtime and orchestration

1. Event entrypoints and dispatch priority
	- src/main/java/uk/co/duelmonster/minersadvantage/events/server/ServerEventHandler.java:79 (server tick)
	- src/main/java/uk/co/duelmonster/minersadvantage/events/server/ServerEventHandler.java:123 (entity spawn interception)
	- src/main/java/uk/co/duelmonster/minersadvantage/events/server/ServerEventHandler.java:153 (block break routing)
	- src/main/java/uk/co/duelmonster/minersadvantage/events/server/ServerEventHandler.java:210 (tool modification routing)
	- src/main/java/uk/co/duelmonster/minersadvantage/events/client/ClientEventHandler.java:40 (client tick)
	- src/main/java/uk/co/duelmonster/minersadvantage/events/client/KeyInputEvents.java:17 (key state routing)

2. Per-player variable and config synchronization
	- src/main/java/uk/co/duelmonster/minersadvantage/common/Variables.java:49 (syncToPlayer)
	- src/main/java/uk/co/duelmonster/minersadvantage/common/Variables.java:59 (syncToServer)
	- src/main/java/uk/co/duelmonster/minersadvantage/config/MAConfig_Base.java:58 (syncPlayerConfigToServer)
	- src/main/java/uk/co/duelmonster/minersadvantage/network/packets/PacketSynchronization.java:55 (sync packet handler)

3. Worker lifecycle and completion signaling
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/AgentProcessor.java:39 (fireAgentTicks)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/AgentProcessor.java:101 (startProcessing)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/AgentProcessor.java:115 (stopProcessing)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/AgentProcessor.java:153 (completion state updates)

4. Drop/XP interception and respawn semantics
	- src/main/java/uk/co/duelmonster/minersadvantage/events/server/ServerEventHandler.java:123 (spawn cancellation while agents active)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/DropsSpawner.java:20 (recordedDrops)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/DropsSpawner.java:48 (spawnDrops with gatherDrops behavior)

5. Abort flow
	- src/main/java/uk/co/duelmonster/minersadvantage/events/client/KeyInputEvents.java:118 (abort key sends packet)
	- src/main/java/uk/co/duelmonster/minersadvantage/network/packets/PacketAbortAgents.java:28 (abort handler)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/AgentProcessor.java:115 (stopProcessing implementation)

6. Hunger/TPS/limit guards
	- src/main/java/uk/co/duelmonster/minersadvantage/common/Functions.java:77 (IsPlayerStarving)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/ExcavationAgent.java:65 (hunger gate)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/ShaftanationAgent.java:57 (hunger gate)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/VentilationAgent.java:63 (hunger gate)

### B. Feature parity anchors

1. Captivation
	- src/main/java/uk/co/duelmonster/minersadvantage/events/client/ClientEventHandler.java:62 (periodic Captivate packet)
	- src/main/java/uk/co/duelmonster/minersadvantage/network/packets/PacketCaptivate.java:35 (server-side capture handler)
	- src/main/java/uk/co/duelmonster/minersadvantage/network/packets/PacketCaptivate.java:41 (AABB inflate by configured radii)
	- src/main/java/uk/co/duelmonster/minersadvantage/events/client/ClientEventHandler.java:137 (unconditional blacklist pickup cancel)
	- src/main/java/uk/co/duelmonster/minersadvantage/config/categories/MAConfig_Captivation.java:11 (config category)

2. Cropination harvest
	- src/main/java/uk/co/duelmonster/minersadvantage/network/packets/PacketCropinate.java:53 (agent start)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/CropinationAgent.java:50 (tick loop)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/CropinationAgent.java:142 (isFullyGrown age checks)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/CropinationAgent.java:95 (harvestSeeds logic)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/CropinationAgent.java:160 (decreaseSeedsInInventory)

3. Cultivation auto-till
	- src/main/java/uk/co/duelmonster/minersadvantage/network/packets/PacketCultivate.java:53 (agent start)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/CultivationAgent.java:24 (farmable land area)
	- src/main/java/uk/co/duelmonster/minersadvantage/helpers/FarmingHelper.java:12 (getFarmableLand)
	- src/main/java/uk/co/duelmonster/minersadvantage/helpers/FarmingHelper.java:23 (getWaterSource)

4. Excavation
	- src/main/java/uk/co/duelmonster/minersadvantage/network/packets/PacketExcavate.java:54 (agent start)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/ExcavationAgent.java:36 (single layer + auto illuminate init)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/ExcavationAgent.java:113 (direction-aware queue expansion)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/ExcavationAgent.java:87 (mineVeins delegation)
	- src/main/java/uk/co/duelmonster/minersadvantage/config/categories/MAConfig_Excavation.java:10 (config category)

5. Pathanation
	- src/main/java/uk/co/duelmonster/minersadvantage/network/packets/PacketPathanate.java:41 (agent start)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/PathanationAgent.java:90 (setupPath geometry)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/PathanationAgent.java:105 (pathLength)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/PathanationAgent.java:108 (pathWidth parity math)

6. Illumination
	- src/main/java/uk/co/duelmonster/minersadvantage/events/client/KeyInputEvents.java:85 (manual PlaceTorch trigger)
	- src/main/java/uk/co/duelmonster/minersadvantage/events/client/KeyInputEvents.java:89 (manual IlluminateArea trigger)
	- src/main/java/uk/co/duelmonster/minersadvantage/helpers/IlluminationHelper.java:87 (PlaceTorch)
	- src/main/java/uk/co/duelmonster/minersadvantage/helpers/IlluminationHelper.java:142 (IlluminateArea)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/AgentProcessor.java:82 (shaft auto-illumination dispatch)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/AgentProcessor.java:84 (ventilation wall torch dispatch)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/IlluminationAgent.java:85 (autoIlluminate)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/IlluminationAgent.java:130 (face placement validation)

7. Lumbination
	- src/main/java/uk/co/duelmonster/minersadvantage/network/packets/PacketLumbinate.java:54 (agent start)
	- src/main/java/uk/co/duelmonster/minersadvantage/helpers/LumbinationHelper.java:92 (identifyTree)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/LumbinationAgent.java:41 (tree area initialization)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/LumbinationAgent.java:100 (useShearsOnLeaves path)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/LumbinationAgent.java:115 (leavesAffectDurability path)
	- src/main/java/uk/co/duelmonster/minersadvantage/helpers/LumbinationHelper.java:282 (replantSaplings)

8. Shaftanation
	- src/main/java/uk/co/duelmonster/minersadvantage/network/packets/PacketShaftanate.java:54 (agent start)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/ShaftanationAgent.java:101 (setupShaft geometry)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/ShaftanationAgent.java:33 (auto illuminate enable)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/ShaftanationAgent.java:78 (mineVeins delegation)

9. Substitution
	- src/main/java/uk/co/duelmonster/minersadvantage/events/client/ClientEventHandler.java:94 (block substitution packet)
	- src/main/java/uk/co/duelmonster/minersadvantage/network/packets/PacketSubstituteTool.java:54 (server tool substitution entry)
	- src/main/java/uk/co/duelmonster/minersadvantage/helpers/SubstitutionHelper.java:61 (processToolSubtitution)
	- src/main/java/uk/co/duelmonster/minersadvantage/helpers/SubstitutionHelper.java:219 (processWeaponSubtitution)
	- src/main/java/uk/co/duelmonster/minersadvantage/events/client/ClientEventHandler.java:120 (attack-stage substitution behavior)

10. Veination
	- src/main/java/uk/co/duelmonster/minersadvantage/events/server/ServerEventHandler.java:181 (ore validity branch)
	- src/main/java/uk/co/duelmonster/minersadvantage/network/packets/PacketVeinate.java:54 (agent start)
	- src/main/java/uk/co/duelmonster/minersadvantage/common/Functions.java:288 (isValidOre)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/Agent.java:264 (excavateOreVein back-dispatch)

11. Ventilation
	- src/main/java/uk/co/duelmonster/minersadvantage/network/packets/PacketVentilate.java:54 (agent start)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/VentilationAgent.java:107 (setupVent geometry)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/VentilationAgent.java:141 (placeLadder logic)
	- src/main/java/uk/co/duelmonster/minersadvantage/helpers/VentilationHelper.java:22 (playerHasLadders)
	- src/main/java/uk/co/duelmonster/minersadvantage/helpers/VentilationHelper.java:56 (isLadderablePosition)

### C. Cross-feature routing matrix anchors

1. Block-break routing order
	- src/main/java/uk/co/duelmonster/minersadvantage/events/server/ServerEventHandler.java:181 (Veination first)
	- src/main/java/uk/co/duelmonster/minersadvantage/events/server/ServerEventHandler.java:185 (Shaft/Vent branch)
	- src/main/java/uk/co/duelmonster/minersadvantage/events/server/ServerEventHandler.java:196 (Excavation/Single-layer branch)
	- src/main/java/uk/co/duelmonster/minersadvantage/events/server/ServerEventHandler.java:202 (Lumbination fallback)

2. Post-completion illumination handoff
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/AgentProcessor.java:74 (shouldAutoIlluminate gate)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/AgentProcessor.java:82 (shaft torch placement mode)
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/AgentProcessor.java:84 (ventilation both-wall mode)

3. Shared mineVeins behavior used by multiple workers
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/ExcavationAgent.java:85
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/ShaftanationAgent.java:76
	- src/main/java/uk/co/duelmonster/minersadvantage/workers/VentilationAgent.java:82

### D. Configuration and override parity anchors

1. Unified synced config payload model
	- src/main/java/uk/co/duelmonster/minersadvantage/config/SyncedClientConfig.java:12
	- src/main/java/uk/co/duelmonster/minersadvantage/config/SyncedClientConfig.java:35

2. Category definitions
	- src/main/java/uk/co/duelmonster/minersadvantage/config/categories/MAConfig_Common.java:9
	- src/main/java/uk/co/duelmonster/minersadvantage/config/categories/MAConfig_Captivation.java:11
	- src/main/java/uk/co/duelmonster/minersadvantage/config/categories/MAConfig_Cropination.java:7
	- src/main/java/uk/co/duelmonster/minersadvantage/config/categories/MAConfig_Excavation.java:10
	- src/main/java/uk/co/duelmonster/minersadvantage/config/categories/MAConfig_Pathanation.java:7
	- src/main/java/uk/co/duelmonster/minersadvantage/config/categories/MAConfig_Illumination.java:8
	- src/main/java/uk/co/duelmonster/minersadvantage/config/categories/MAConfig_Lumbination.java:11
	- src/main/java/uk/co/duelmonster/minersadvantage/config/categories/MAConfig_Shaftanation.java:9
	- src/main/java/uk/co/duelmonster/minersadvantage/config/categories/MAConfig_Substitution.java:10
	- src/main/java/uk/co/duelmonster/minersadvantage/config/categories/MAConfig_Veination.java:9
	- src/main/java/uk/co/duelmonster/minersadvantage/config/categories/MAConfig_Ventilation.java:8

3. Server override enforcement controls
	- src/main/java/uk/co/duelmonster/minersadvantage/config/MAConfig_Server.java:17
	- src/main/java/uk/co/duelmonster/minersadvantage/config/MAConfig_Server.java:40
	- src/main/java/uk/co/duelmonster/minersadvantage/config/categories/MAConfig_BaseCategory.java:38

4. Default values baseline
	- src/main/java/uk/co/duelmonster/minersadvantage/config/defaults/MAConfig_Defaults.java:22

### E. Input surface parity anchors

1. Default key mappings and toggle controls
	- src/main/java/uk/co/duelmonster/minersadvantage/client/KeyBindings.java:19
	- src/main/java/uk/co/duelmonster/minersadvantage/client/KeyBindings.java:43
	- src/main/java/uk/co/duelmonster/minersadvantage/events/client/KeyInputEvents.java:35

2. Excavation toggle and single-layer toggle semantics
	- src/main/java/uk/co/duelmonster/minersadvantage/events/client/KeyInputEvents.java:39
	- src/main/java/uk/co/duelmonster/minersadvantage/events/client/KeyInputEvents.java:54

3. Shaft/Vent toggle semantics
	- src/main/java/uk/co/duelmonster/minersadvantage/events/client/KeyInputEvents.java:102