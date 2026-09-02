## 2.24.0

- Add per-agent scheduler controls: a deduplication toggle and a maximum concurrent instance limit for every agent type, both defaulting to enabled and four respectively.
- Add an Agent Limits config category with individual capacity controls for each agent type.
- Reduce repeated Substitution restore-defer debug messages.
- Publish only the `CHANGELOG.md` version sections newer than the release already live on Modrinth as the Modrinth and CurseForge release notes, instead of uploading the entire file.
- Resolve the last published version from the public Modrinth version list, falling back to the newest changelog section when the lookup is unavailable.
- Add `printReleaseChangelog` to preview the release notes without publishing.
- Document the release-notes range in TECHNICAL and add a release-notes preview step to the release checklist.
- Align the build and governance tooling with the Forget-Me-Crops repository so both projects behave identically.
- Upgrade the mod publish plugin to `2.2.0` and switch the CurseForge environment flags to the current API.
- Add a `verifyJarBytecode` task that checks production jar class-file versions against the node's Java target.
- Simplify `.vscode/launch.json` by removing generated NeoForge profiles and duplicate `neoForgeIdeSync` disablement.
- Register `chiseledPublishAllPublicationsToLocalRepository` and the GitHub Packages equivalent so the release workflow resolves the tasks it invokes.
- Correct the release workflow to call `chiseledPublishAll` instead of the non-existent `chiseledPublishMods`.
- Adopt the richer CI and release workflows, including multi-JDK setup and split platform publish steps.
- Add `AGENTS.md` describing the repository workflow rules for coding agents.

## 2.23.0

- Fix NeoForge illumination keybind packet flow by wiring client tick illumination dispatch and payload registration/handling across supported NeoForge versions.
- Reduce NeoForge/Fabric held-preview render-thread load by throttling unchanged outline resubmission for short frame windows.
- Extract preview render-throttle decision logic into a dedicated helper and add regression coverage for unchanged-vs-changed signature behavior.
- Reduce substitution restore debug spam by rate-limiting repeated deferred-reason logs during switch-back idle polling.

## 2.22.0

- Start the optimization rebuild sequence from the 2.21 baseline by introducing a diagnostics envelope for ghost-block and credits-roll repro tracking.
- Add client screen transition diagnostics and server break-face cache hit/miss diagnostics to support staged NeoForge/Fabric parity investigation.
- Add a commit-2750 replay inventory to the tuning matrix, classifying retain-now, retain-later, and hold-back optimization deltas for staged reapplication.
- Optimize shared client runtime compatibility by caching reflected current-screen accessor lookup instead of scanning methods on every fallback access.
- Refactor common event dispatch guard checks into shared helper paths to reduce repeated feature-enabled branching while preserving behavior.
- Improve throughput tuning matrix table readability formatting.
- Harden break-face caching to per-player/per-position storage in both Fabric and NeoForge paths to keep rapid adjacent trigger orientation state from being evicted.
- Align throughput configuration terminology and effective clamp documentation across README, tuning matrix, and config-screen labels/ranges.
- Optimize throughput processing internals by tightening `ProcessingCoreService` queue drain loops and adding a `WorkerRuntimeService` TPS-guard fast path with no behavior change.
- Optimize tick-delay cadence checks in `ServerTickOrchestrator` by caching the effective processing interval while preserving runtime behavior.
- Optimize `WorkerRuntimeService` TPS-guard tick handling by short-circuiting no-worker/guarded ticks before allocation-heavy paths and lazily allocating completion tracking when needed.
- Normalize `WorkerRuntimeServiceTest` formatting after the TPS-guard optimization stage so staged regression coverage remains hook-clean and style-consistent.
- Optimize Excavation ordered-shape queue sorting by precomputing layer/spiral sort keys in `ExcavationAgent`, reducing repeated lookup work while preserving traversal targets and ordering semantics.
- Optimize Veination discovery by introducing a bounded BFS cursor consumed by `VeinationAgent` tick updates, spreading expensive discovery work across ticks while preserving discovered vein membership.
- Optimize Lumbination hot-path matching and queue churn by caching normalized log/leaf allowlists and deduplicating pending traversal entries before processing.
- Optimize NeoForge adapter client tick handling by reusing a shared `ClientInputService` instance across 1.21.11/26.1.2/26.2 adapter event bridges to avoid per-tick service allocation churn.
- Finalize replay docs/test sync with a staged validation summary in tuning docs and added helper-semantic coverage for veination/lumbination parity checks.
- Clarify replay workflow docs so in-game validation gates apply to optimization/runtime commits, while docs-only follow-up commits proceed after standard validation checks.
- Add a dedicated release checklist document and link it from technical docs to standardize pre-release gates, matrix checks, and publish handoff steps.
- Extend the staged optimization replay plan with post-14 commits covering remaining 2750 performance surfaces (shape/predicate caches, substitution paths, packet process support, and Supreme Vantage runtime), each with commit-isolated in-game confirmation gates.
- Optimize shape/predicate lookup hot paths by restoring direct shape id/index caches in `MAShapeRegistry` and adding bounded block-state lookup caching in `RegistryPredicates` while preserving behavior.
- Optimize substitution core candidate selection by replacing allocation-heavy stream pipelines with a single-pass filtered selector while preserving ranking and decision outcomes.
- Optimize substitution agent runtime paths by caching reflection-member discovery per player class and replacing stream-based candidate max selection with a comparator-driven single-pass loop.
- Optimize packet processing hot paths by restoring cached state-id to block-id resolution and per-class reflected method lookup in `PacketProcessSupport` while preserving dispatch authority semantics.
- Optimize Supreme Vantage runtime reward/materialization paths by restoring precomputed digit/enchantment constants and removing per-tick/per-call stream/list allocation churn.
- Fix substitution switch-back ghost-block regressions by restoring field-first game-mode carrier resolution order while keeping reflection-member caching.
- Fix Fabric integrated-server end-tick crashes during player list mutation by iterating a stable `ServerPlayer` snapshot instead of the live level player collection.
- Add packet dispatch regression coverage that locks fallback `minecraft:air` block/tool ids when player context lookups are unavailable.
- Finalize Commit-20 extension closure docs across README, TECHNICAL, and tuning matrix with full validation gate confirmation.
- Replace common throughput pacing `blocks_per_tick` with `ticks_per_block` plus `max_blocks_per_tick`, including config UI, policy clamps/guardrails, and agent runtime cadence/budget handling.
- Set common pacing defaults to `ticks_per_block=1` and `max_blocks_per_tick=1`.
- Fix NeoForge shape cycling feedback parity by restoring scroll-wheel shape-name overlay messaging and direct held-key shape index cycling behavior.
- Fix NeoForge shape preview outlines by wiring block-outline render callbacks to pass submit-context handles into `ShapePreviewRenderer` and resolving direct submit/buffer render contexts in runtime compat.

## 2.21.0

- Fix publish post-summary output to report each node's actual publish artifact instead of scanning stale jars in the shared `releases/` folder.
- Fix Single Layer Excavation to avoid crossing air gaps by requiring contiguous in-envelope block connectivity at runtime, and prevent preview/render crashes by keeping world-dependent connectivity checks out of shape precompute.
- Restore Fabric mouse-wheel shape cycling by wiring `minersadvantage.mixins.json` into Fabric metadata and re-enabling the client scroll interceptor path.
- Apply live runtime config updates immediately by reloading feature components when authoritative sync config changes.
- Prevent startup/menu lockup regressions by keeping client sync snapshots lazily initialized until in-world tick flow.
- Add focused regression coverage for runtime Captivation disable sync behavior and Fabric-only startup snapshot safety.
- Gate feature dispatch entry points and packet routing on live enabled state so disabled features cannot keep dispatching work.
- Persist cultivation hydration distance and substitution selection rules across config reloads, with UI copy and round-trip coverage updates.
- Clear stale agent workers when features are disabled and add cleanup coverage for type-based agent removal.
- Add regression coverage for cultivation hydration distance above the old cap and substitution selection-rule TOML round-trips.
- Clear stale agents by type when disabling features and clean up the agent-manager test fixture to avoid compile-time Unsafe imports.
- Stop server-tick Captivation agent respawn when Captivation is disabled so disabled state is fully respected at runtime.

## 2.20.0

- Fix startup/menu lockups by hardening client initialization and render-hook behavior.
- Defer client outline hook registration until in-world and skip render-thread shape warmup work at startup to keep title-screen initialization responsive.
- Fix Shaftanation height/runtime trigger regressions and restore expected shaft semantics.
- Align shaft generation/runtime clamping with configured height and block-origin triggers to prevent extra-layer carving and recursive retrigger chains.
- Fix Substitution switch-back timing so active automation jobs complete before slot restore.
- Gate Substitution switch-back on active automation agents so tool restoration cannot interrupt in-progress harvesting chains.
- Fix CurseForge publish compatibility by marking uploads as both client and server supported to satisfy environment-group version validation.

## 2.19.0

- MC 26.2 KNOWN ISSUES:
  - Shape outline not rendering
  - Shape change info display not shown
  - Config screens: returning to the Feature list works for the first opened Feature config, but then it starts to create a history of screens.

- Fix 26.2 NeoForge outline rendering by honoring the 26.2 `CustomBlockOutlineRenderer` return contract.
- Fix 26.2 NeoForge feature config screens by preferring `setScreenAndShow` when opening Cloth Config sub-screens.
- Update 26.2 support to use released Cloth Config 26.2.155, Fabric Loader 0.19.3, and NeoForge 26.2.0.7-beta.

- Fix NeoForge Supreme Vantage parity so 1.21.11 and 26.1.2 client tick flow grants rewards and syncs state like Fabric.
- Fix NeoForge 1.21.11 feature config sub-screen launching by resolving the active GUI screen from `Minecraft.screen` before opening nested screens.
- Fix shared shape preview renderer cross-version compile output by restoring the missing reflective `Method` import.
- Fix Excavation shapeless runtime boundaries to bias width away from player heading and preserve origin-seeded traversal when the trigger block is already air.
- Fix Excavation tool durability application by persisting post-break forced-tool state back to main hand and stopping excavation when the trigger tool breaks.
- Fix preview/runtime parity by routing Excavation, Shaftanation, and Ventilation outline position generation through `MAShapePrecomputeCache.compute(...)`.
- Optimize preview and excavation code paths by removing redundant shapeless preview-only helper pipelines and simplifying duplicated queue/shape state logic.

## 2.18.0

- Restrict Captivation player-drop cooldown filtering to verifiable player-owned drops by requiring a resolved dropper UUID match and removing pickup-delay fallback heuristics.
- Harden Captivation dropper reflection member caching so compatible getter/field paths are retained even when first-observed runtime values are null.
- Add Captivation regression test coverage for player-drop cooldown rules and tick-path item skip decisions.

## 2.17.0

- Tighten Supreme Vantage reward flow by tracking remaining reward budget client-side, stopping packet emission when exhausted, and enforcing finite per-player reward sequence grants.
- Wire packet handling through explicit server-player context across Fabric and NeoForge network event routes.
- Update client input/runtime glue and supporting core/config/shape cache paths needed by the Supreme Vantage and action-key handling flow.
- Add and keep local workspace/snapshot artifacts for ongoing debugging (`.vscode/settings.json`, `.ollamassist`, and `_tmp_*` source snapshots).
- Tune workspace editor defaults in `.vscode/settings.json` for 2-space indentation, wider wrapping, and format-on-save/paste consistency during current refactor work.
- Start MC 26.2 porting

## 2.15.0

- Fix veination ore classification to check cross-loader ore tags (`c:ores`, `neoforge:ores`, `forge:ores`) before falling back to vanilla ore tags and `_ore`/`ancient_debris` id heuristics.
- Fix production runtime keybind torch placement regressions by replacing mapping-name method reflection with signature-based game mode interaction lookup.
- Harden additional reflection paths across client and common utility flows (input handling, packet support, substitution/captivation helpers, and config-screen runtime fallbacks) to reduce dev-vs-production mapping break risk.
- Replace stale internal guide wording that referenced YACL with Cloth Config terminology and remove stale YACL-related crash-report artifacts from local run outputs.
- Update internal changelog rule guide wording to reference Cloth Config instead of YACL.
- Respect tool context and enchant behavior across queued/programmatic block breaks by routing agent break paths through a shared tool-aware break helper, and prevent duplicate excavation worker spawning from retriggered break events.
- Fix Wide Cuboid excavation shaping to preserve configured side depth and apply full-volume per-layer spiral ordering instead of collapsing traversal to a width-limited cube subset.
- Stop excavation, shaft, and ventilation traversal when a whole ordered depth layer is already air, so workers abort cleanly instead of iterating empty downstream layers.
- Guard runtime tests behind block-registry availability assumptions and refresh synthetic context assertions for farming, mining, utility, excavation, and illumination coverage.
- Add a dedicated full-test-suite pre-commit validator and wire hook setup scripts to install and report the new test gate alongside existing validators.

## 2.14.0

- Source `ModCommon.MOD_VERSION` from Gradle-generated metadata constants wired to `gradle.properties`.
- Generate `GeneratedModMetadata` during Java compile setup and wire the generated source directory into the main source set.
- Add shape precompute cache integration and bounded runtime shape processing for excavation and shaft flows.
- Harmonize even-size excavation and shaft shape centering with a shared top/right bias across built-in processors and face geometry helpers.
- Fix shaft and excavation auto-illumination to derive torch candidate Y levels from carved shape geometry depth slices instead of broad heuristic base scans.
- Enforce deterministic excavation and shaft traversal ordering by depth-first layer progression with clockwise per-layer spiral processing.
- Fix shapeless runtime origin-air seeding so connected excavation traversal starts correctly after trigger block break.
- Correct staircase shaft ordering by normalizing vertical layer indices per depth step before clockwise spiral ranking.
- Update worker runtime, policy, packet flow, and shape parity tests to cover the precompute and ordering pipeline changes.

## 2.13.0

- Fix client keybind registration by skipping specs with no default key so unbound toggle actions do not trigger key-token parsing failures.

## 2.12.0

- Refine substitution trigger/runtime behavior by removing interaction-trigger substitution paths, simplifying hotbar slot sync to stable APIs, and expanding substitution diagnostics.
- Add client-configurable debug logging, persist it in client TOML config, and route runtime logging checks through the persisted setting.
- Update default gameplay tuning for excavation dimensions, path width, substitution preference toggles, and max veination distance.
- Update README and TECHNICAL docs for the new client debug logging option.
- Remove default key assignments and labels for shape-cycling actions while keeping keybinding action definitions available.

## 2.11.0

- Move bundled branding images into `assets/minersadvantage/` and point Fabric/NeoForge mod metadata icon fields to the shared `icon.png` path.
- Fix NeoForge config screen feature sub-screens not opening by removing reflective Cloth Config footer-button injection that prevented feature screens from rendering.
- Add NeoForge outline preview renderer using `ExtractBlockOutlineRenderStateEvent` and `addCustomRenderer`, with reflective camera coordinate extraction for cross-version compatibility.
- Fix NeoForge shapes (shaft, vent, excavation, veination, lumbination) being triggered on block click rather than block break by moving all carve-agent creation into a block-break listener.
- Fix NeoForge shaft vs. vent routing so the block face hit at break time correctly determines vent (top/bottom face) vs. shaft (side face), using per-player remembered break-face state.
- Fix NeoForge event bus rejection of abstract `Event` listener by introducing version-specific `NeoForgeBreakEvents` bridge classes with typed `BlockEvent.BreakEvent` (1.21.11) and `BreakBlockEvent` (26.1.2) handlers.
- Fix Fabric and NeoForge keybind registration to use a dedicated MinersAdvantage category again, using direct category registration approach across 1.21.11 and 26.1.2, and restore the missing translated labels for shape-cycling keybinds.
- Harden reflective network payload identifier construction with multi-strategy constructor/factory fallback chain for cross-version robustness.
- Fix Fabric outline preview compatibility on 26.1.2 by supporting both `LevelRenderEvents` (`poseStack`) and legacy `WorldRenderEvents` (`matrices`) hook paths via a reflective context bridge.
- Add `scripts/run-gradle-java25.ps1` and route VS Code debug tasks through the wrapper so local Gradle runs consistently resolve with Java 25.
- Align VS Code Gradle import and debug-task JVM selection to use the workspace Java 25 setting, preventing local Java 21 plugin-resolution failures.
- Update TECHNICAL notes with Java 25 debug-task behavior and local environment override guidance.

## 2.10.0

- Fix CI Gradle startup failure on Linux runners by removing the hardcoded local `org.gradle.java.home` path and relying on runner-provided `JAVA_HOME`.
- Track Stonecutter node `versions/*/gradle.properties` files in git so CI receives required `modstitch.platform` values for all Fabric/NeoForge nodes.
- Apply workspace-wide comment updates across Java sources and tests, and fix malformed comment syntax regressions in NeoForge/Fabric generation paths to restore full compile-matrix gate success.
- Complete full-workspace optimization pass with shared helper extraction across client input, excavation and shaft shape placement, config-screen readonly entries, and test fixture setup; include structural cleanup and duplicate reduction across main/test/versioned source roots.
- Add client-configurable preview outline foreground and see-through colors, persist them in `client-config.toml`, and expose both settings in the client config UI using alpha color fields.
- Implement dual-pass shape outline rendering matching LiteMiner: translucent NO_DEPTH_TEST pass (occluded bounds visible through blocks) plus opaque depth-tested foreground pass using `mc.renderBuffers().bufferSource()` directly with Stonecutter-gated mc1/mc26 pipeline API paths.
- Fix Fabric outline preview startup/runtime compatibility by replacing fragile `LevelRenderer` mixin targeting with a version-tolerant Fabric render event bridge.
- Refine shape preview outline rendering by combining selected blocks into a merged voxel outline and drawing line passes against the combined shape.
- Correct Excavation 3x3 targeting to use a strict one-layer plane oriented by the clicked face axis.
- Add focused 3x3 orientation regression coverage for north/top/side face geometry expectations.
- Expand shape parity test assertions to lock 3x3 face-axis offset membership and single-layer depth behavior.
- Reorient remaining Excavation shapes to use hit-face driven depth with rotated tangent axes for north/south, top/bottom, and east/west interactions.
- Migrate Excavation and Ventilation config models, runtime paths, and TOML keys from radius-based fields to explicit `width`/`height`/`depth` dimensions.
- Rename Shaftanation config dimensions to `width`/`height`/`depth` for naming consistency and align defaults and UI bindings with the new field names.
- Correct default Excavation dimensions to `3x3x3`.
- Update mining/config parity tests to validate the migrated `width`/`height`/`depth` signatures and defaults.
- Adjust Shaftanation shaft-floor anchoring to use the `playerFeetY + height - 1` threshold so high-origin tunnels preserve intended floor alignment.
- Fix Shaftanation floor anchoring to allow origins below player feet to tunnel below foot level while still anchoring origins inside the shaft-height window.
- Add Ventilation held-key outline preview for vertical-face targeting and include the origin block in the vent-height preview count.
- Show selected Excavation/Shaft shape names on the HUD when shapes are cycled and when the corresponding mode keybind becomes active.
- Add a new default Excavation `Shapeless` shape using connected 18-neighbor flood-fill ordering.
- Add focused Shapeless helper and registry parity test coverage for default selection and traversal invariants.
- Refresh README and TECHNICAL notes for shapeless default behavior and shape-system parity guidance.

## 2.9.0

- Scaffold a new common Shape API package with registerable shape definitions, context contracts, processor interfaces, and registry helpers.
- Extend client and server player-state sync models with selected Excavation and Shaftanation shape indexes.
- Add initial root-level Shape API addon guide and cross-link it from README and TECHNICAL documentation.
- Publish the standalone `SHAPE_API.md` addon-authoring reference with quickstart and troubleshooting guidance.
- Add built-in Excavation and Shaftanation shape processor implementations and bootstrap registration at core startup.
- Wire Excavation and Shaftanation runtime agent dispatch to consume per-player selected shape indexes during execution.
- Fix NeoForge left-click Excavation shape-face propagation so all compile matrix nodes build cleanly.
- Add focused shape registry/bootstrap parity tests covering built-in counts, index wrapping, and idempotent initialization.
- Add client keybind actions for Excavation/Shaftanation shape cycling and sync selected shape index changes to the server.
- Add held-key client shape preview rendering and align NeoForge hold/sync behavior with Fabric shape-selection input flow.
- Replace held-key shape preview particles with true block-outline rendering through client highlight hooks.
- Add mouse-wheel shape switching while Excavation/Shaftanation hold mode is active, with immediate state sync to the server.
- Fix Fabric client startup crash from `LevelRenderer` mixin descriptor drift by moving outline preview hook registration to a version-tolerant Fabric render event bridge.
- Align shape preview visuals by combining selected blocks into a merged voxel outline before drawing line passes.
- Centralize shared shape dimension math for runtime and preview context building and add focused regression tests for that parity path.
- Update keybinding parity tests to cover the new shape-cycling default bindings.
- Refresh README and TECHNICAL documentation with shape-cycling and held-key preview parity notes.
- Add a Technical manual in-game verification checklist for shape cycling, preview parity, and runtime execution checks.
- Harden git hook version-bump enforcement to require a real version increase when advancing the daily bump date.
- Require version bumps to match expected `Next Version` and force state `Next Version` to advance past the bumped version.
- Reorder pre-commit validators to run in this sequence: version-bump, optimization, docs, compile-matrix, changelog.
- Relax README section validation by no longer requiring `## Supported Crops` and `## Compatibility` headings.

## 2.8.0

- Remove excavation single-layer mode, including keybind/state/sync wiring, and always use configured vertical radius.
- Remove Single Layer Excavation references from CurseForge documentation.
- Make veination respect the shared `blocks_per_tick` limit instead of using a hardcoded per-agent cap.
- Make `ABORT_WORKERS` clear active and queued agents immediately, not just worker-runtime state.
- Respect `gather_drops` when relocating veination item drops so disabling drop gathering keeps items at their original block positions.
- Make excavation veination fan-out trigger from connected exposed ore discovery instead of requiring the broken origin-matching block itself to be ore.
- Consolidate veination fan-out logic into a shared agent helper and wire shaft/vent flows through the common path.
- Make post-excavation auto-illumination target the carved excavation gap instead of a player-centered area.
- Share connected-neighbor traversal helpers so excavation, lumbination, and veination all use the same 3x3 connectivity rule.
- Fix excavation to keep single-layer mode tied only to the single-layer toggle and trigger auto-illumination after completion for any exit reason.
- Center `ILLUMINATION_AREA` execution on the player's position instead of the targeted block while keeping area mode floor-placement behavior.
- Centralize synced default construction through `MAConfig_Defaults` so `SyncedClientConfig.defaults()` no longer hard-codes per-feature values.
- Canonicalize `MAConfig_Defaults` feature field names and expand missing entries (cultivation, excavation, pathanation, lumbination, shaftanation, substitution, ventilation).
- Align illumination synced defaults to `MAConfig_Defaults` values, including `lowestLightLevel = 1` and radius defaults `(8, 4)`.
- Fix shaft wall auto-illumination to check shaft-floor light levels instead of wall-target light levels.
- Fix shaft wall auto-illumination left/right placement so wall modes follow the shaft-facing direction correctly.
- Add focused regression coverage for shaft wall torch geometry and floor-light sampling.

## 2.7.0

- Fix Cropination harvest execution to use vanilla block-break flow before replanting mature crops.
- Keep Cropination scan expansion active from immature crop targets while harvesting only mature crop states.
- Allow Cropination to run without nearby water for water-independent crops such as nether wart while keeping water-required crops gated.
- Remove Cultivation block-limit cutoff so hydrated patch processing no longer stops at a fixed block ceiling.
- Improve Cultivation floating-plant cleanup by combining replaceable-above break handling with delayed follow-up block updates.
- Rename Lumbination `use_shears_on_leaves` mode to `use_canopy_tool` across runtime, config storage, and UI labels.
- Allow excavation, shaftanation, and ventilation agents to fan out into veination when `common.mine_veins` is enabled and ore eligibility checks pass.
- Remove veination queue caps so connected ore discovery and processing are no longer limited by a fixed block ceiling.
- Capture the triggering tool stack for agent-driven veination fan-out so substitution-driven hand changes do not suppress ore chaining.
- Fix `AgentManager` tick-time concurrent modification by deferring agent additions during iteration and flushing queued agents after the tick pass.
- Cap veination distance settings to a maximum of 12 and treat deepslate/stone ore variants as a shared ore family during vein discovery.
- Fix Pathanation to start from the clicked origin, follow player-facing horizontal direction, and only trigger/place on `BlockTags.DIRT` blocks.
- Prevent Pathanation and Cultivation from processing under blocked headspace by sharing an `Agent` helper that requires air-or-replaceable blocks above the target.
- Rework ventilation ladder/torch placement sequencing to carve first, place in reverse order, respect per-tick budgets, include origin ladders, and emit reliable placement sounds.

## 2.6.0

- Remove excavation, shaftanation, and ventilation trigger block/tool restrictions so activation can start from arbitrary targets and held items.
- Rework Lumbination into a logs-first then leaves-second harvest flow to stop interleaved trunk/canopy breaking.
- Replace leaf flood-fill traversal with a bounded canopy candidate pass derived from trunk bounds to reduce neighboring-tree bleed.
- Remove Lumbination's common block-limit cutoff so large trees do not stop mid-harvest and leave floating canopies.
- Add `ignore_player_placed_leaves` Lumbination config wiring across defaults, TOML storage, policy sanitization, sync defaults, and config UI.
- Restore Lumbination log/leaf parity guards: require a valid origin leaf, restrict fallback log matching to origin log type, and enforce origin leaf block matching.
- Improve Lumbination sapling replanting to use detected trunk base targets, support all-or-nothing 2x2 planting, and delay planting until full tree harvest completion.
- Change Lumbination sapling consumption order to inventory-first with shears-mode inventory-only consumption.
- Fix captivation recent-drop detection to keep player-thrown items exempt for the full 160-tick cooldown window.
- Restore captivation XP orb attraction so the magnet path now targets experience orbs again instead of item entities only.
- Stop captivation from immediately re-pulling freshly dropped player items by honoring recent-drop ownership and pickup-delay signals.
- Ignore newly dropped nearby items in captivation until drop ownership or pickup-delay windows have expired.
- Restrict agent ticking to the active world dimension so cross-dimension agents do not progress on the wrong server tick.
- Keep agent queues dimension-scoped so server ticks do not advance agents from unloaded or different worlds.
- Route manual illumination placement through the clicked face, add a dedicated placement agent, and require a solid supporting face before consuming torches.
- Tighten shaft auto-illumination to respect the configured light threshold, wait for carve light updates, and reject invalid torch supports.
- Make manual illumination place torches at the clicked face target instead of offsetting blindly above the selected block.
- Share torch survival validation between illumination and shaft agents so invalid supports do not consume inventory.
- Complete runtime config-option parity wiring across Fabric and NeoForge event routes, including right/left click dispatch and server tick orchestration gates.
- Make substitution switch tools on the initiating click instead of the following tick.
- Stop manual torch placement from auto-triggering illumination placement bursts.
- Require the shaft/vent hold state before right-click stone ventilation can start.
- Restore the Fabric illumination keybind by sending an explicit client illumination action packet.
- Apply excavation single-layer mode by collapsing the vertical radius only while that hold is active.
- Route shaft/vent pickaxe block-break activation by hit face so horizontal faces start shafts and vertical faces start vents.
- Rewrite ShaftanationAgent to dig horizontally in the player's facing direction instead of downward, with correct cross-section axis based on direction.
- Rewrite ShaftanationAgent to dig horizontally in the player's facing direction instead of downward, with correct cross-section axis based on direction.
- Rewrite VentilationAgent to dig a 1×1 vertical column (up or down) from the triggered face, with per-block ladder placement that draws from player inventory.
- Fix shaft floor height sitting one block too high by shifting the cross-section base down one block so the floor aligns with the player's foot level.
- Fix shaft auto-illumination placing floor torches in mid-air for wall-placement modes by using WALL_TORCH blocks with correct facing instead of freestanding torches.
- Fix shaft generation to anchor floor height to the player's feet and include the initial x/z layer when carving.
- Restore shaft auto-illumination to carve first, then place torches from furthest to nearest using current block light checks.
- Enforce synchronized common-feature behavior for veination, captivation, substitution, cultivation, illumination, pathanation, shaftanation, ventilation, excavation, and lumbination agents.
- Add veination ore allowlist runtime checks and substitution ATTACK context support with entity-type-aware rule targeting.
- Improve cropination replant/seed behavior and lumbination leaves/shears/durability/replant handling for closer legacy behavior alignment.
- Update TECHNICAL documentation with the 2026-05-25 runtime parity sweep and cross-node compatibility caveat notes.
- Clarify NeoForge 26.1.2 left-click routing notes in TECHNICAL parity caveat guidance.
- Rework the Features tab into dedicated per-feature launcher rows with live status coloring, per-feature reset confirmation, and stable save/back behavior.
- Migrate configuration to split `MAClientRootConfig` and `MAServerRootConfig` roots with TOML-backed persistence for client/server state.
- Simplify player sync transport and policy flow to server-authoritative gameplay configuration with typed root snapshots.
- Replace legacy config category wrappers with unified root-based UI bindings and remote-multiplayer authority lock messaging.
- Add focused split-root coverage for TOML storage, sync core state, packet flow, and policy authority behavior.
- Refresh README and TECHNICAL documentation for split-root config categories and multiplayer authority semantics.
- Remove remaining legacy config compatibility paths (`MAConfig` facade, JSON cleanup branch, and `SyncType.ClientConfig` sync handling).
- Migrate config UI dependency and screen implementation to Cloth Config for FMC parity.
- Add workspace VS Code Java configuration sync setting for consistent local project import behavior.
- Change default light level to `0` for auto illumination

## 2.5.0

- Rework Veination discovery/runtime wiring to support AFTER-break origins, optional origin-state hints, and connected-vein queueing through a shared runtime service.
- Add optional reflective Fabric Collective dig-speed callback registration so missing Collective dependencies do not break startup.
- Align Veination defaults to harvest-without-sneak across synced defaults, legacy constructors, and policy/config plumbing.
- Persist legacy global config to `config/minersadvantage-client-config.json` and load it for UI bootstrap, login sync seeding, and player state sync updates.
- Reduce Fabric player-state sync spam by sending activation-state updates only when tracked toggle state changes.
- Add Veination config parity tests for legacy constructor defaults and modifier clamping.
- Refresh VS Code launch/task definitions for the full Fabric/NeoForge debug matrix and simplified Java environment handling.
- Align workspace validation, hook setup, and documentation rules with stricter parity checks.
- Trigger substitution on block-attack start instead of post-break so tool selection applies before mining completes.
- Restrict substitution candidate selection to hotbar slots and switch held tool by selected slot change rather than stack swapping.
- Add start-trigger de-duplication and activity tracking to avoid repeated queueing while mining or interacting continuously.
- Improve switch-back timing so restore waits for break-target activity to end instead of reverting mid-action.

## 2.4.0

- Rework substitution selection to autoswitch-style candidate ordering with multi-level ratings and deterministic tie-breaks.
- Add action-aware substitution rule tables for BREAK/INTERACT with per-rule target and tool priorities.
- Add boolean expression support (`AND`/`OR`/`NOT`) for target/tool rule matching and enforce per-rule enchant constraints (silk, fortune, mending).
- Wire substitution runtime with active-hand context and synchronized rule propagation across policy/default/category config paths.
- Expand README and TECHNICAL with substitution rule expression syntax and practical rule authoring examples.

## 2.3.0

- Phase 3 parity rewrite is complete across all four nodes with documentation and verification signoff.
- Add a full documentation standards guide and enforce README/TECHNICAL coverage checks in pre-commit.
- Add tracked daily version-bump enforcement so `mod_version` changes are blocked more than once per day without an explicit override.

**Milestone Completion:**
- ✅ M1: Traceability audit complete - Baseline matrix created
- ✅ M2: Bootstrap complete - Both loaders initialize MinersAdvantageCore
- ✅ M3: Keybindings & Client Input complete - Fabric and NeoForge client input wired across all four nodes
- ✅ M4: Networking transport layer complete - component toggle, abort workers, player sync, feature dispatch, and SupremeVantage packet transport wired across Fabric and NeoForge
- ✅ M5: Server events & dispatch complete - login/logout, level unload, entity load/join handling, and NeoForge tool-modification hook wired
- ✅ M6: Config UI complete - shared Cloth Config screen implemented, Fabric ModMenu factory wired, NeoForge config screen extension point registered
- ✅ M7: Assets/Localization migration complete - migrated `en_us.json` and `ru_ru.json` into modern resources and added rewrite keybinding/localization keys
- ✅ M8: Documentation signoff complete - traceability matrix refreshed to current milestone status
- ✅ M9: Verification/regression gate complete - `chiseledBuild` and targeted NeoForge test gates pass

## 2.2.0

- Add final strict traceability audit matrix mapping all appendix anchors (A-E) to modern implementation references.
- Add cross-feature policy enforcement coverage for common shared flags in PolicyCoreService tests.
- Mark parity rewrite phases and documentation signoff as complete after Milestone 6 validation.
- Add runtime cropination harvest action evaluation with maturity, replant, and durability cadence decisions.
- Add hydration-aware cultivation planning and lumbination trunk/leaf/sapling execution plans.
- Add captivation runtime decision gating for GUI restrictions and radius checks.
- Add focused runtime tests for farming, harvest, and captivation parity slices.
- Add illumination manual-placement decisions, torch depletion reporting, and richer utility runtime coverage.
- Add substitution combat/mining policy decisions with primary-tool switch-back behavior.
- Add typed synced-config payloads and server override enforcement for all modern feature configs, including Cultivation.
- Expand config records to carry legacy parity fields for block lists, policy toggles, torch placement, and override-aware settings.
- Add legacy-parity keybinding metadata and client input toggle handling for feature enablement, excavation modes, illumination actions, and abort flow.
- Preserve SupremeVantage as a hidden code-driven reward path with modern packet and service coverage.

## 2.1.0

- Reset repository into modernization workspace with legacy archive in releases/original.
- Replace legacy build with Stonecutter and Modstitch Kotlin DSL foundation.
- Add four-node version matrix scaffold for Fabric and NeoForge.
- Introduce descriptor-driven component lifecycle and shared service modules.
- Add initial deterministic unit tests for processing, geometry, substitution, illumination, policy, drop handling, and bootstrap registration.
- Add CI and release workflows for all Stonecutter nodes.
