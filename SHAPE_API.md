# SHAPE API

## Status

This document defines the upcoming Miners Advantage Shape API contract for addon developers.
It describes the runtime registration model and geometry processing rules that addon shapes must follow.

## Scope

The Shape API currently targets two features:

- Excavation
- Shaftanation

Ventilation behavior remains unchanged and is not currently extension-driven.

## Quickstart

1. Add Miners Advantage as a compile dependency in your addon project.
2. Register your shapes during common mod initialization.
3. Provide one definition and one processor for each shape.
4. Use globally unique shape ids in `namespace:path` format.

## Core Types

### MAShapeDefinition

`MAShapeDefinition` describes one registerable shape:

- `id`: stable unique id (`examplemod:my_shape`)
- `displayName`: UI-facing label
- `feature`: owning feature (`EXCAVATION` or `SHAFTANATION`)
- `processor`: functional processor implementation

### MAShapeContext

`MAShapeContext` contains the runtime geometry inputs:

- `level`
- `player`
- `origin`
- `hitFace`
- `playerFacing`
- `width`
- `height`
- `depth`
- `maxBlocks`

### MAShapeProcessor

`MAShapeProcessor` computes candidate block positions for the given context.

Contract:

- Return deterministic output for identical input.
- Respect `maxBlocks` budget.
- Include origin if your shape semantics require it.
- Keep ordering stable when converting to ordered collections downstream.

### MAShapeRegistry

`MAShapeRegistry` is the runtime registry for shape definitions.

Behavior:

- Preserves registration order.
- Rejects duplicate ids.
- Supports index-based lookup per feature.

## Registering a Shape

Example registration flow:

```java
import net.minecraft.core.BlockPos;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeDefinition;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeRegistry;

import java.util.LinkedHashSet;

public final class ExampleShapes {
    private ExampleShapes() {
    }

    public static void register() {
        MAShapeRegistry.register(new MAShapeDefinition(
            "examplemod:column_plus",
            "Column Plus",
            FeatureId.EXCAVATION,
            context -> {
                LinkedHashSet<BlockPos> out = new LinkedHashSet<>();
                BlockPos origin = context.origin();
                out.add(origin);
                out.add(origin.above());
                out.add(origin.below());
                return out;
            }
        ));
    }
}
```

## Shape Authoring Rules

### Id Rules

- Use your addon mod id as namespace.
- Never reuse built-in Miners Advantage ids.
- Keep ids stable across releases once published.

### Geometry Rules

- Treat `width`, `height`, and `depth` as positive dimensions.
- Use deterministic odd/even centering for symmetric shapes.
- Apply hit-face and facing rules consistently for directional shapes.
- Cap output to `maxBlocks` to avoid expensive previews and oversized mining plans.

### Determinism Rules

- Do not rely on random order collections when output order matters.
- Prefer `LinkedHashSet` when producing de-duplicated ordered candidates.
- Keep any tie-break ordering explicit and stable.

## Shaftanation Specific Guidance

For shaft-like directional shapes:

- Horizontal-face hits should follow depth direction conventions.
- Vertical-face handling must follow feature constraints.
- Staircase variants should derive depth direction from player facing when required.

## Input and Preview Expectations

Shape selection changes are expected to be:

- client-driven while active mode is engaged
- synchronized to server state
- reflected in outline preview and runtime execution

Preview and execution must use the same shape processor logic to avoid mismatch.

## Troubleshooting

### Shape does not appear in cycle

Check:

- registration ran during init
- feature affinity matches expected feature
- id is unique

### Duplicate id exception

Check:

- your id namespace
- accidental double registration in client and common init

### Preview differs from executed blocks

Check:

- both paths call the same processor implementation
- any runtime filtering applied after shape generation
- `maxBlocks` clipping behavior

## Compatibility Notes

This API is under active rollout while shape migration lands in Miners Advantage.
If method signatures evolve during rollout, this document will be updated with finalized interfaces and migration notes.
