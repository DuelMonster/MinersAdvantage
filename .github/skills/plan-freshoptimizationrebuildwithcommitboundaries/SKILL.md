---
name: plan-freshoptimizationrebuildwithcommitboundaries
description: plan-freshOptimizationRebuildWithCommitBoundaries
disable-model-invocation: true
---
## Plan: Fresh Optimization Rebuild With Commit Boundaries

Restart from clean commit 299d0cd9bf2a22a15a6abe49e422bf190bb37f83 and re-apply optimizations as narrowly scoped commits. Each optimization commit is followed by a mandatory in-game test pause before proceeding. The known-regression commit 2750db1e333e44af4569e019477544dbf6e952b8 is treated as a source of candidate deltas, not as a unit to replay.

**Steps**

1. Commit 00 - Branch Bootstrap (blocking): create new branch from 299d0cd9bf2a22a15a6abe49e422bf190bb37f83.
2. Include: branch creation only.
3. Exclude: source edits.
4. Gate: confirm HEAD equals 299d0cd9... before any edits.

5. Commit 01 - Baseline Diagnostics Envelope
6. Include: minimal, safe diagnostics needed to compare NeoForge vs Fabric harvesting behavior and detect ghost/credits recurrence in this restart effort.
7. Files: ModEntry, ClientInputHandler, any existing debug helper paths only.
8. Exclude: runtime behavior changes, scheduling changes, harvesting logic changes.
9. Gate: user runs in-game repro and confirms baseline characteristics captured.

10. Commit 02 - Refactor Delta Inventory (Documentation-Only)
11. Include: written mapping of commit 2750db1e... into retain-now / retain-later / hold-back buckets.
12. Files: TECHNICAL.md or TUNING_MATRIX.md section and working notes.
13. Exclude: Java logic changes.
14. Gate: user agrees decomposition before replaying optimizations.

15. Commit 03 - Safe Cache Optimizations (Common Code)
16. Include: reflection/result caching and equivalent no-semantic-change cache wins from 2750db1e... that cannot alter harvesting behavior.
17. Files: common/client shared helpers such as ClientRuntimeCompat, CaptivationAgent-type reflection call sites.
18. Exclude: block break eligibility rules, excavation termination rules, sync packet behavior.
19. Gate: compile matrix + user in-game test.

20. Commit 04 - Event Dispatch Guard Optimizations (Common-First)
21. Include: early-return feature-enabled guards and dispatch de-dup improvements that do not modify world-state logic.
22. Files: CommonEventHandlerImpl, ModEntry forwarding/gating only.
23. Exclude: break-face consumption semantics that affect orientation correctness.
24. Gate: compile matrix + user in-game test specifically rapid adjacent 3x3 triggers.

25. Commit 05 - Break-Face Cache Safety Replay
26. Include: per-player/per-position break-face cache hardening only.
27. Files: ModEntry.
28. Exclude: excavation queue logic, agent tick budget logic.
29. Gate: in-game rapid adjacent 3x3 test; must show no second-trigger miss.

30. Commit 06 - Throughput Track A: Config/Policy Parity (ticks-per-block track)
31. Include: align and lock semantics for common.blocks_per_tick, feature processes_per_tick, enable_tick_delay, tick_delay, tps_guard in config/policy/docs/UI text only where needed for parity.
32. Files: CommonConfig, PolicyCoreService, MAConfig_Defaults, MATomlConfigStore, MinersAdvantageConfigScreen, README, TUNING_MATRIX.
33. Exclude: runtime processing loops.
34. Gate: config round-trip tests + user sanity test with default settings.

35. Commit 07 - Throughput Track B: Budget Loop Micro-Optimization
36. Include: ProcessingCoreService/Agent budget-loop micro-optimizations preserving exact output semantics.
37. Files: ProcessingCoreService, Agent shared budget helpers.
38. Exclude: tick-delay cadence semantics and TPS guard behavior semantics.
39. Gate: user in-game test under sustained harvesting; no ghost/credits regressions.

40. Commit 08 - Throughput Track C: Tick-Delay Cadence Optimization
41. Include: ServerTickOrchestrator tick-delay path optimization preserving current effective cadence.
42. Files: ServerTickOrchestrator and directly related tests.
43. Exclude: TPS guard behavior changes.
44. Gate: deterministic cadence test + user in-game test.

45. Commit 09 - Throughput Track D: TPS Guard Optimization
46. Include: internal TPS guard efficiency improvements only; retain current pause semantics unless separately approved.
47. Files: WorkerRuntimeService, ServerTickOrchestrator, policy/config references as needed.
48. Exclude: adaptive throttle or semantic policy shifts unless explicitly requested later.
49. Gate: low-TPS scenario test; user confirms stability.

50. Commit 10 - Excavation Ordering Optimization (High Risk Isolated)
51. Include: only ordering/precompute optimizations from prior refactor that reduce constructor/tick cost while preserving target set and order semantics.
52. Files: ExcavationAgent, MAShapeRegistry/shape helpers only if required.
53. Exclude: empty-layer abort behavior changes and completion sync behavior changes.
54. Gate: user tests 3x3 chaining and larger excavation scenarios.

55. Commit 11 - Veination Discovery Spread Optimization
56. Include: move expensive discovery work into bounded per-tick slices while preserving mined block set.
57. Files: VeinationAgent, VeinationCoreService, VeinationRuntimeService.
58. Exclude: loader-specific forks.
59. Gate: user tests large ore veins on NeoForge and Fabric.

60. Commit 12 - Lumbination/Canopy Hot Path Optimization
61. Include: low-allocation matching and bounded canopy seeding/work queue improvements.
62. Files: LumbinationAgent, optional utility helpers.
63. Exclude: behavior changes to what constitutes valid logs/leaves.
64. Gate: user tests dense-forest tree runs with profiling.

65. Commit 13 - Optional NeoForge Adapter-Only Adjustments
66. Include: unavoidable loader-API glue differences only.
67. Files: versions/\*-neoforge adapter event files.
68. Exclude: logic forks duplicating shared decisions.
69. Gate: must show shared path remains source of truth.

70. Commit 14 - Final Docs/Test Matrix Sync
71. Include: docs parity updates, tuning matrix finalization, changelog entries, missing tests for throughput and event parity.
72. Files: README, TUNING_MATRIX, CHANGELOG, relevant tests.
73. Exclude: performance logic edits.
74. Gate: full compile matrix and test suite green.

75. Commit 15 - Shape/Predicate Cache Hygiene Optimization
76. Include: bounded cache and lookup micro-optimizations in shared shape/predicate paths that do not alter target sets or runtime decisions.
77. Files: MAShapeRegistry, RegistryPredicates.
78. Exclude: shape generation semantics, ore/log/leaf classification semantics.
79. Gate: compile matrix + user in-game preview/runtime parity check.

80. Commit 16 - Substitution Core Candidate Filtering Optimization
81. Include: low-allocation candidate filtering/ranking optimizations in substitution core while preserving selection outcomes.
82. Files: SubstitutionCoreService.
83. Exclude: rule precedence, enchantment policy, and switch-back semantics.
84. Gate: substitution regression tests + user in-game tool-swap parity check.

85. Commit 17 - Substitution Agent Runtime Fast-Path Optimization
86. Include: agent-level fast-path and queue churn reductions with unchanged behavior.
87. Files: SubstitutionAgent.
88. Exclude: dispatch authority and multiplayer sync semantics.
89. Gate: compile matrix + user in-game sustained mining/combat substitution parity check.

90. Commit 18 - Packet Process Support Micro-Optimization
91. Include: packet-process hot-path allocation and dispatch micro-optimizations with unchanged packet authority behavior.
92. Files: PacketProcessSupport.
93. Exclude: packet payload schema, trust/authority checks, and sync ordering rules.
94. Gate: packet regression checks + user multiplayer in-game parity test.

95. Commit 19 - Supreme Vantage Runtime Optimization
96. Include: Supreme Vantage service micro-optimizations that preserve deterministic reward progression.
97. Files: SupremeVantageService.
98. Exclude: reward sequencing semantics, cap enforcement, and packet contract changes.
99. Gate: targeted reward-flow tests + user in-game confirmation.

100. Commit 20 - Extension Final Docs/Test Matrix Sync
101. Include: changelog/docs updates and any missing regression tests introduced by commits 15-19.
102. Files: README, TECHNICAL, TUNING_MATRIX, CHANGELOG, relevant tests.
103. Exclude: performance logic edits.
104. Gate: full compile matrix and test suite green.

105. Stop-and-Wait Rule (hard requirement)
106. After every optimization commit (03 onward), stop and wait for user in-game verification before preparing the next commit.
107. Exception: docs-only commits do not require in-game verification; proceed once validation gates pass.
108. If ghosting or credits-roll recurs: immediately revert only the latest commit under test and re-scope before continuing.

**Relevant files**

- d:/Mod_Source/MinersAdvantage/src/main/java/uk/co/duelmonster/minersadvantage/agent/Agent.java — shared break path and budget helper risk center.
- d:/Mod_Source/MinersAdvantage/src/main/java/uk/co/duelmonster/minersadvantage/agent/ExcavationAgent.java — high-risk second-3x3 behavior area.
- d:/Mod_Source/MinersAdvantage/src/main/java/uk/co/duelmonster/minersadvantage/ModEntry.java — break-face and dispatch ingress behavior.
- d:/Mod_Source/MinersAdvantage/src/main/java/uk/co/duelmonster/minersadvantage/common/services/core/ProcessingCoreService.java — per-tick budget loop.
- d:/Mod_Source/MinersAdvantage/src/main/java/uk/co/duelmonster/minersadvantage/common/services/core/ServerTickOrchestrator.java — tick-delay cadence.
- d:/Mod_Source/MinersAdvantage/src/main/java/uk/co/duelmonster/minersadvantage/common/services/core/WorkerRuntimeService.java — TPS/hunger pausing flow.
- d:/Mod_Source/MinersAdvantage/src/main/java/uk/co/duelmonster/minersadvantage/common/config/CommonConfig.java — throughput knobs.
- d:/Mod_Source/MinersAdvantage/src/main/java/uk/co/duelmonster/minersadvantage/common/services/policy/PolicyCoreService.java — clamp/validation semantics.
- d:/Mod_Source/MinersAdvantage/src/main/java/uk/co/duelmonster/minersadvantage/common/config/defaults/MAConfig_Defaults.java — defaults.
- d:/Mod_Source/MinersAdvantage/src/main/java/uk/co/duelmonster/minersadvantage/common/config/storage/MATomlConfigStore.java — persistence.
- d:/Mod_Source/MinersAdvantage/src/main/java/uk/co/duelmonster/minersadvantage/client/MinersAdvantageConfigScreen.java — UI semantics for ticks-per-block terminology mapping.
- d:/Mod_Source/MinersAdvantage/src/main/java/uk/co/duelmonster/minersadvantage/common/shape/api/MAShapeRegistry.java — preview/runtime shape cache + lookup surfaces.
- d:/Mod_Source/MinersAdvantage/src/main/java/uk/co/duelmonster/minersadvantage/common/registry/RegistryPredicates.java — shared block/item predicate hot paths.
- d:/Mod_Source/MinersAdvantage/src/main/java/uk/co/duelmonster/minersadvantage/common/services/substitution/SubstitutionCoreService.java — substitution candidate filtering and ranking.
- d:/Mod_Source/MinersAdvantage/src/main/java/uk/co/duelmonster/minersadvantage/agent/SubstitutionAgent.java — substitution runtime dispatch/queue behavior.
- d:/Mod_Source/MinersAdvantage/src/main/java/uk/co/duelmonster/minersadvantage/common/network/packets/PacketProcessSupport.java — packet processing hot path.
- d:/Mod_Source/MinersAdvantage/src/main/java/uk/co/duelmonster/minersadvantage/common/services/utility/SupremeVantageService.java — deterministic reward progression runtime path.
- d:/Mod_Source/MinersAdvantage/README.md — public config/perf semantics.
- d:/Mod_Source/MinersAdvantage/TECHNICAL.md — scenario matrices and loader parity guidance.
- d:/Mod_Source/MinersAdvantage/versions/1.21.11-neoforge/src/main/java/uk/co/duelmonster/minersadvantage/client/NeoForgeClientEvents.java — adapter-only NeoForge hooks.
- d:/Mod_Source/MinersAdvantage/versions/26.1.2-neoforge/src/main/java/uk/co/duelmonster/minersadvantage/client/NeoForgeClientEvents.java — adapter-only NeoForge hooks.

**Verification**

1. Before Commit 03, establish clean baseline behavior from Commit 00 branch.
2. For each commit 03-20: compile matrix must pass and diagnostics must be clean.
3. For each commit 03-19: pause for user in-game test before next commit (docs-only commits are exempt from this gate).
4. For throughput commits 06-09: validate config-policy-doc parity and runtime behavior equivalence for unchanged semantics.
5. For high-risk commits 10-12: require targeted scenario replay (rapid adjacent 3x3, large vein, dense canopy) on NeoForge first, then Fabric parity pass.
6. For commits 15-19: require targeted scenario replay by subsystem (preview parity, substitution parity, packet/multiplayer parity, Supreme Vantage reward parity).

**Decisions**

- Commit granularity is optimization-atom sized; no mixed-risk commits.
- Throughput/ticks-per-block changes are isolated into four dedicated commits (06-09).
- Loader-specific code remains adapter-only and cannot own shared gameplay decisions.
- Regression trigger policy is immediate one-commit rollback of the current stage under test.

**Further Considerations**

1. Commit message convention: Option A strict semantic prefixes per stage. Option B semantic prefixes plus stage tag in body for easier bisect.
2. Throughput terminology: Option A keep blocks_per_tick naming and map user-facing “ticks-per-block” in docs text. Option B add explicit UI helper text explaining inverse relationship.
3. Safety net: Option A lightweight temporary checkpoint tags after each accepted stage. Option B rely only on linear commits plus revert if needed.
