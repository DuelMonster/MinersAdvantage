## 0.6.0

- Complete runtime config-option parity wiring across Fabric and NeoForge event routes, including right/left click dispatch and server tick orchestration gates.
- Make substitution switch tools on the initiating click instead of the following tick.
- Stop manual torch placement from auto-triggering illumination placement bursts.
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

## 0.5.0

- Rework Veination discovery/runtime wiring to support AFTER-break origins, optional origin-state hints, and connected-vein queueing through a shared runtime service.
- Add optional reflective Fabric Collective dig-speed callback registration so missing Collective dependencies do not break startup.
- Align Veination defaults to harvest-without-sneak across synced defaults, legacy constructors, and policy/config plumbing.
- Persist legacy global config to `config/minersadvantage/client-config.json` and load it for UI bootstrap, login sync seeding, and player state sync updates.
- Reduce Fabric player-state sync spam by sending activation-state updates only when tracked toggle state changes.
- Add Veination config parity tests for legacy constructor defaults and modifier clamping.
- Refresh VS Code launch/task definitions for the full Fabric/NeoForge debug matrix and simplified Java environment handling.
- Align workspace validation, hook setup, and documentation rules with stricter parity checks.
- Trigger substitution on block-attack start instead of post-break so tool selection applies before mining completes.
- Restrict substitution candidate selection to hotbar slots and switch held tool by selected slot change rather than stack swapping.
- Add start-trigger de-duplication and activity tracking to avoid repeated queueing while mining or interacting continuously.
- Improve switch-back timing so restore waits for break-target activity to end instead of reverting mid-action.

## 0.4.0

- Rework substitution selection to autoswitch-style candidate ordering with multi-level ratings and deterministic tie-breaks.
- Add action-aware substitution rule tables for BREAK/INTERACT with per-rule target and tool priorities.
- Add boolean expression support (`AND`/`OR`/`NOT`) for target/tool rule matching and enforce per-rule enchant constraints (silk, fortune, mending).
- Wire substitution runtime with active-hand context and synchronized rule propagation across policy/default/category config paths.
- Expand README and TECHNICAL with substitution rule expression syntax and practical rule authoring examples.

## 0.3.0

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

## 0.2.0

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

## 0.1.0

- Reset repository into modernization workspace with legacy archive in releases/original.
- Replace legacy build with Stonecutter and Modstitch Kotlin DSL foundation.
- Add four-node version matrix scaffold for Fabric and NeoForge.
- Introduce descriptor-driven component lifecycle and shared service modules.
- Add initial deterministic unit tests for processing, geometry, substitution, illumination, policy, drop handling, and bootstrap registration.
- Add CI and release workflows for all Stonecutter nodes.
