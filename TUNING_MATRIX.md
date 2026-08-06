# Throughput Tuning Matrix

This matrix is for controlled in-game comparisons of the new common pacing knobs:

- `common.ticks_per_block`
- `common.max_blocks_per_tick`

## Instrumentation Output

When debug logging is enabled, each non-captivation agent now emits per-tick metrics:

- `processed`: blocks processed this tick
- `queueDepth`: queued work remaining (combined queues for multi-phase agents)
- `tickCostMicros`: wall-clock tick cost for the agent update
- `budget`: effective budget after cadence + guardrails

Log line prefix: `AgentTick`

## Test Setup

1. Launch with debug logging enabled (`MINERSADVANTAGE_DEBUG_LOGGING=true`) or enable client debug logging.
2. Use the same world seed/region and similar tool tier for all runs.
3. For each row below, run the same action pattern for ~60 seconds and capture logs.
4. Record min/avg/p95 values for `processed`, `queueDepth`, and `tickCostMicros`.

## Matrix

| Profile | ticks_per_block | max_blocks_per_tick | Intended Feel | Primary Risk | Notes |
| ------- | --------------- | ------------------- | ------------- | ------------ | ----- |
| A (Conservative) | 10 | 1 | Very smooth, low impact | Slow completion | Baseline default |
| B (Smoother-Moderate) | 5 | 1 | Faster than A, still smooth | Mild backlog in large jobs | Good multiplayer candidate |
| C (Balanced) | 3 | 2 | Noticeably responsive | Small bursts | Good single-player candidate |
| D (Responsive) | 2 | 3 | Fast feedback | Occasional spikes | Watch p95 tick cost |
| E (Bursty) | 1 | 4 | Immediate response | Burst lag in dense operations | Stress profile |

## Scenario Set

Run each profile against these scenarios:

1. `ExcavationAgent`: carve a medium tunnel section with mixed stone/ore.
2. `ShaftanationAgent`: depth >= 32 with auto-illumination enabled.
3. `LumbinationAgent`: large canopy tree with leaves phase enabled.
4. `VeinationAgent`: dense ore vein cluster.
5. `CultivationAgent` + `CropinationAgent`: hydrated farm patch loop.

## Comparison Criteria

Use this order when judging outcomes:

1. Stability: no obvious server hitching or long frame stalls.
2. Throughput: queue depth trends downward consistently.
3. Responsiveness: first visible action occurs quickly after trigger.
4. Consistency: low variance in `tickCostMicros` (p95 close to mean).

## Guardrail Notes

Runtime now applies a burst guardrail:

- Effective budget per processing window is capped by `floor(256 / ticks_per_block)`.

Server policy also applies the same guardrail after clamping:

- `ticks_per_block`: `1..40`
- `max_blocks_per_tick`: `1..64`, then reduced by guardrail when needed.

This prevents pathological settings that defer work for long periods and then release oversized bursts.

## Refactor Delta Inventory (Commit 2750db1e333e44af4569e019477544dbf6e952b8)

This section defines what we will and will not replay from the original hot-path refactor while rebuilding the optimization track from the clean `2.21.0` baseline.

### Retain Now (safe, common-first)

- Reflection/member lookup caching that does not alter gameplay semantics.
- Event entry guard short-circuits for disabled features when behavior is unchanged.
- Bounded cache hygiene (for example preview/outline cache caps) where outputs are unchanged.

### Retain Later (needs stage isolation + in-game gate)

- Throughput-path optimizations (`ticks_per_block`, `max_blocks_per_tick`, tick-delay cadence, TPS guard internals).
- Excavation ordering/precompute efficiency improvements.
- Veination/Lumbination bounded work spreading and low-allocation hot-loop cleanups.

These are replayed only in isolated commits with explicit stop-and-test gates.

### Hold Back (do not replay without dedicated approval)

- Changes that alter excavation termination semantics (for example empty-layer abort logic adjustments).
- Changes that alter break-result authority/sync behavior in ways that can impact client/server visual parity.
- Any loader-specific logic fork where shared common code can express the same behavior.

### Regression Trigger Policy

- If ghost blocks or credits-roll behavior returns during replay, revert only the latest stage commit under test.
- Re-scope that stage before continuing; do not batch additional optimizations into the same retry.
