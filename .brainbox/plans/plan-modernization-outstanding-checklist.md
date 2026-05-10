## Plan: Outstanding Work Checklist (Post-Audit)

Purpose
- Track only the work that remains incomplete from plan-modernizastion.md.
- Anchor every outstanding item to legacy source behavior under releases/original/src/main/java.
- Enforce .brainbox guides and .brainbox rules during every milestone commit.

Scope of this checklist
- Phase 3 parity completion only.
- Any follow-up tests/docs required to close parity with evidence.

---

## 0) Governance Enforcement (Mandatory)

Guides reviewed
- [x] .brainbox/guides/COMMIT_STANDARDS.md
- [x] .brainbox/guides/CHANGELOG_RULE.md
- [x] .brainbox/guides/VERSION_BUMP_RULE.md
- [x] .brainbox/guides/COMMENT_STYLE.md

Rules reviewed
- [x] .brainbox/rules/commit.rules.md
- [x] .brainbox/rules/comment.rules.md
- [x] .brainbox/rules/documentation.rules.md
- [x] .brainbox/rules/todo.commit.md
- [x] .brainbox/rules/todo.optimisation.pass.md

Enforcement gates per milestone
- [ ] Apply exactly one logical change group per commit.
- [ ] Use semantic commit subject format with emoji + type.
- [ ] Update CHANGELOG.md before commit when behavior is user-visible.
- [ ] Run optimization pass before each commit (duplication, dead code, structure).
- [ ] Confirm no unrelated staged files before commit.
- [ ] Keep docs accurate for any behavior/config changes.

---

## 1) Legacy vs Modern Coverage Audit

Legend
- COMPLETE: Implemented and wired to runtime flow.
- PARTIAL: Structure exists, behavior stubbed or not fully wired.
- MISSING: No modern equivalent yet.

### A. Runtime and Orchestration

| Parity area                      | Legacy anchors (releases/original)                                 | Modern anchors (src/main/java)                                                     | Status   | Outstanding work |
|----------------------------------|--------------------------------------------------------------------|-------------------------------------------------------------------------------------|----------|------------------|
| Event entrypoints and routing    | events/server/ServerEventHandler.java, events/client/*             | common/event/CommonEventHandlerImpl.java, common/event/FeatureEventHandler.java    | PARTIAL  | Add Fabric + NeoForge runtime registration and real event adapters. |
| Per-player sync loop             | common/Variables.java, network/packets/PacketSynchronization.java  | common/services/sync/SyncCoreService.java, common/services/core/PlayerStateService.java | PARTIAL  | Implement bidirectional sync packets and periodic client/server sync cadence. |
| Worker lifecycle                 | workers/AgentProcessor.java                                        | common/services/processing/ProcessingCoreService.java                               | PARTIAL  | Implement runtime worker orchestration (start, tick budget, stop, completion). |
| Drop/XP interception             | workers/DropsSpawner.java                                          | common/services/drop/DropCoreService.java                                           | PARTIAL  | Add live entity intercept + gatherDrops spawn policy + flush-on-complete. |
| Abort flow                       | events/client/KeyInputEvents.java, packets/PacketAbortAgents.java  | (no runtime packet/handler equivalent)                                              | MISSING  | Add abort packet, active-worker cancel path, and flush cleanup behavior. |
| Hunger/TPS guards                | common/Functions.java, workers/*Agent.java                         | common/services/core/PlayerStateService.java, common/services/policy/PolicyCoreService.java | PARTIAL  | Enforce starvation and TPS guards in worker loop. |

### B. Feature Behavior Parity

| Feature        | Legacy anchors (releases/original)                                 | Modern anchors (src/main/java/common)                      | Status   | Outstanding work |
|----------------|--------------------------------------------------------------------|-------------------------------------------------------------|----------|------------------|
| Captivation    | events/client/ClientEventHandler.java, packets/PacketCaptivate.java| feature/captivation/CaptivationComponent.java               | PARTIAL  | Implement real item/xp capture radius, GUI gate, direct pickup blacklist behavior. |
| Cropination    | packets/PacketCropinate.java, workers/CropinationAgent.java        | feature/farming/CropinationComponent.java                   | PARTIAL  | Implement maturity checks + replant seed consumption + durability cadence. |
| Cultivation    | packets/PacketCultivate.java, workers/CultivationAgent.java        | feature/farming/CultivationComponent.java                   | PARTIAL  | Implement hydration-aware tilling over area geometry. |
| Excavation     | packets/PacketExcavate.java, workers/ExcavationAgent.java          | feature/mining/ExcavationComponent.java                     | PARTIAL  | Replace placeholder mining branches with queue processing and single-layer/toggle behavior. |
| Pathanation    | packets/PacketPathanate.java, workers/PathanationAgent.java        | feature/utility/PathanationComponent.java                   | PARTIAL  | Implement facing-based width/length path conversion with terrain progression. |
| Illumination   | helpers/IlluminationHelper.java, workers/IlluminationAgent.java    | feature/utility/IlluminationComponent.java                  | PARTIAL  | Implement floor/wall strategies, manual modes, inventory depletion notification. |
| Lumbination    | helpers/LumbinationHelper.java, workers/LumbinationAgent.java      | feature/harvest/LumbinationComponent.java                   | PARTIAL  | Implement trunk/leaf traversal, policy toggles, sapling replant flow. |
| Shaftanation   | packets/PacketShaftanate.java, workers/ShaftanationAgent.java      | feature/mining/ShaftanationComponent.java                   | PARTIAL  | Implement directional geometry and torch placement mode behavior. |
| Substitution   | helpers/SubstitutionHelper.java, packets/PacketSubstituteTool.java | feature/utility/SubstitutionComponent.java                  | PARTIAL  | Implement mining + combat substitution policy matrix and switch-back behavior. |
| Veination      | packets/PacketVeinate.java, common/Functions.java                  | feature/utility/VeinationComponent.java                     | PARTIAL  | Implement ore-tag/list recognition and connected-vein execution. |
| Ventilation    | packets/PacketVentilate.java, workers/VentilationAgent.java        | feature/mining/VentilationComponent.java                    | PARTIAL  | Implement diameter/depth dig, ladder workflow, orientation inversion. |

### C. Config and Override Parity

| Parity area                | Legacy anchors (releases/original)                         | Modern anchors (src/main/java/common/config) | Status   | Outstanding work |
|---------------------------|--------------------------------------------------------------|-----------------------------------------------|----------|------------------|
| Full feature config fields| config/categories/MAConfig_*.java                           | config/*.java records                          | PARTIAL  | Add missing fields (lists, toggles, placement modes, substitution policies). |
| Synced client config model| config/SyncedClientConfig.java                              | services/sync/SyncCoreService.java             | PARTIAL  | Add explicit synced payload model and packet serialization. |
| Server override controls  | config/MAConfig_Server.java                                  | services/policy/PolicyCoreService.java         | PARTIAL  | Add per-feature enforce switches and application pipeline. |

### D. Input Surface and Hidden Behavior

| Parity area                   | Legacy anchors (releases/original)                       | Modern anchors (src/main/java) | Status   | Outstanding work |
|------------------------------|------------------------------------------------------------|---------------------------------|----------|------------------|
| Key mappings and toggles     | client/KeyBindings.java, events/client/KeyInputEvents.java | (none)                          | MISSING  | Implement keybind layer and toggle packet flow. |
| Loader bootstrap/adapters    | setup/* + event bus wiring                                 | fabric/ (empty), neoforge/ (empty) | MISSING  | Implement loader bootstrap classes and event registration. |
| SupremeVantage decision      | helpers/SupremeVantage.java                                | (none)                          | MISSING  | Record explicit decision and implement selected behavior path. |

---

## 2) Milestone Implementation Plan (Commit-Split)

Milestone 1: Loader wiring and runtime entrypoints
- [x] Add Fabric bootstrap + event registration adapters.
- [x] Add NeoForge bootstrap + event registration adapters.
- [x] Wire CommonEventHandlerImpl into loader event streams.
- [x] Add/adjust tests for event entrypoint dispatch.
- Commit plan: 👷build for wiring/bootstrap; ✅test for added tests.
- Milestone 1 progress note: Added [CommonEventHandlerImplTest.java](src/test/java/uk/co/duelmonster/minersadvantage/common/event/CommonEventHandlerImplTest.java) to validate routing behavior from tool events into feature dispatch.
- Milestone 1 implementation note: Adopted the Forget-Me-Crops single-entrypoint Stonecutter pattern via [ModEntry.java](src/main/java/uk/co/duelmonster/minersadvantage/ModEntry.java) and [ModMenuEntrypoint.java](src/main/java/uk/co/duelmonster/minersadvantage/client/ModMenuEntrypoint.java), with Fabric metadata updated in [fabric.mod.json](src/main/templates/fabric.mod.json).

Milestone 2: Worker engine, drop interception, abort path
- [x] Implement worker lifecycle orchestration with queue budget + stop semantics.
- [x] Implement drop/XP capture and gatherDrops spawn policy.
- [x] Implement abort packet + cancel-all-active-workers + flush behavior.
- [x] Apply hunger/TPS guard checks in runtime pipeline.
- Commit plan: ✨feature for runtime behavior; ✅test for lifecycle/abort tests.
- Milestone 2 progress note: Added runtime worker lifecycle service in [WorkerRuntimeService.java](src/main/java/uk/co/duelmonster/minersadvantage/common/services/processing/WorkerRuntimeService.java), wired tick execution in [ServerTickOrchestrator.java](src/main/java/uk/co/duelmonster/minersadvantage/common/services/core/ServerTickOrchestrator.java), and exposed runtime access through [MinersAdvantageCore.java](src/main/java/uk/co/duelmonster/minersadvantage/common/MinersAdvantageCore.java).
- Milestone 2 progress note: Added abort transport model [AbortWorkersPacket.java](src/main/java/uk/co/duelmonster/minersadvantage/common/network/AbortWorkersPacket.java), registry ID mapping in [PacketRegistry.java](src/main/java/uk/co/duelmonster/minersadvantage/common/network/PacketRegistry.java), and packet execution path in [MinersAdvantageCore.java](src/main/java/uk/co/duelmonster/minersadvantage/common/MinersAdvantageCore.java) with flow coverage in [AbortWorkersPacketFlowTest.java](src/test/java/uk/co/duelmonster/minersadvantage/common/network/AbortWorkersPacketFlowTest.java).
- Milestone 2 progress note: Added live-drop interception policy and spawn-queue flush behavior in [WorkerRuntimeService.java](src/main/java/uk/co/duelmonster/minersadvantage/common/services/processing/WorkerRuntimeService.java), covered by [WorkerRuntimeServiceTest.java](src/test/java/uk/co/duelmonster/minersadvantage/common/services/processing/WorkerRuntimeServiceTest.java) and updated [AbortWorkersPacketFlowTest.java](src/test/java/uk/co/duelmonster/minersadvantage/common/network/AbortWorkersPacketFlowTest.java).
- Milestone 2 pending note: loader event hook-up for live drop entity interception remains to be wired into platform events.

Milestone 3: Core feature behavior replacement (no placeholders)
- [ ] Replace placeholder logic in Excavation/Shaftanation/Ventilation.
- [ ] Implement Illumination placement strategies and manual modes.
- [ ] Implement Captivation, Cropination, Cultivation, Pathanation, Lumbination, Veination behaviors.
- [ ] Implement Substitution full policy behavior (mining + combat).
- Commit plan: split by feature families:
  - ✨feature mining-runtime parity
  - ✨feature utility-runtime parity
  - ✨feature farming-harvest parity

Milestone 4: Config parity and server override parity
- [ ] Expand config records to match required parity fields.
- [ ] Implement synced config payload path (client -> server and enforced merge).
- [ ] Add per-feature server override enforcement switches.
- [ ] Validate defaults and range policy with tests.
- Commit plan: ✨feature config parity; ✅test config enforcement coverage.

Milestone 5: Input parity + SupremeVantage decision closure
- [ ] Implement key mapping and toggle semantics parity.
- [ ] Decide and record SupremeVantage scope:
  - preserve
  - remove by signoff
  - preserve behind debug flag
- [ ] Implement chosen SupremeVantage behavior path and tests/docs.
- Commit plan: ✨feature input surface parity; 📝docs decision record.

Milestone 6: Final parity audit and signoff
- [ ] Execute full traceability pass: map every appendix anchor to modern implementation or explicit de-scope.
- [ ] Add missing unit tests for all cross-feature policies.
- [ ] Update README.md, TECHNICAL.md, CHANGELOG.md with final parity state.
- [ ] Re-run chiseledBuild and record evidence.
- Commit plan: ✅test parity closure; 📝docs final signoff evidence.

---

## 3) Evidence Tracking Template (Fill Per Milestone)

For each milestone, record:
- [ ] Files changed (modern + docs/tests)
- [ ] Legacy anchors addressed
- [ ] Unit tests added/updated
- [ ] Build/test commands executed and result
- [ ] .brainbox rule compliance notes
- [ ] Commit hash(es) and semantic message(s)

---

## 4) Completion Criteria for This Outstanding Checklist

- [ ] All PARTIAL/MISSING rows above are resolved to COMPLETE or explicitly de-scoped with signoff rationale.
- [ ] Every traceability appendix anchor has a modern implementation reference or approved de-scope note.
- [ ] All milestone commits are split logically and use semantic commit format.
- [ ] CHANGELOG, README, TECHNICAL are accurate for final behavior.
- [ ] chiseledBuild succeeds after final milestone.
- [ ] Final repo state is clean with no unstaged/uncommitted parity work.
