# Throughput Tuning Matrix

This matrix is for controlled in-game comparisons of the new common pacing knobs:

- `common.blocks_per_tick`
- `common.enable_tick_delay`
- `common.tick_delay`

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

| Profile               | blocks_per_tick | enable_tick_delay | tick_delay | Intended Feel              | Primary Risk                  | Notes                        |
| --------------------- | --------------- | ----------------- | ---------- | -------------------------- | ----------------------------- | ---------------------------- |
| A (Conservative)      | 1               | true              | 10         | Very smooth, low impact    | Slow completion               | Lowest throughput profile    |
| B (Smoother-Moderate) | 1               | true              | 5          | Smooth with better cadence | Mild backlog in large jobs    | Default baseline profile     |
| C (Balanced)          | 2               | true              | 3          | Noticeably responsive      | Small bursts                  | Good single-player candidate |
| D (Responsive)        | 3               | true              | 1          | Fast feedback              | Occasional spikes             | Watch p95 tick cost          |
| E (Bursty)            | 4               | false             | 0          | Immediate response         | Burst lag in dense operations | Stress profile               |

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

Effective worker budget for a feature is computed as:

- `min(common.blocks_per_tick, <feature>.processes_per_tick)`

Server policy applies authoritative clamps to common pacing fields:

- `common.blocks_per_tick`: input supports `1..1024`, effective runtime clamp is `1..64`
- `common.tick_delay`: input supports `0..200`, effective runtime clamp is `0..40`
- `common.block_radius`: input supports `1..128`, effective runtime clamp is `1..16`

This prevents pathological settings that defer work for long periods and then release oversized bursts.

## Refactor Delta Inventory (Commit 2750db1e333e44af4569e019477544dbf6e952b8)

This section defines what we will and will not replay from the original hot-path refactor while rebuilding the optimization track from the clean `2.21.0` baseline.

### Retain Now (safe, common-first)

- Reflection/member lookup caching that does not alter gameplay semantics.
- Event entry guard short-circuits for disabled features when behavior is unchanged.
- Bounded cache hygiene (for example preview/outline cache caps) where outputs are unchanged.

### Retain Later (needs stage isolation + in-game gate)

- Throughput-path optimizations (`blocks_per_tick`, `enable_tick_delay`, `tick_delay`, TPS guard internals).
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
