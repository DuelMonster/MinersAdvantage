package uk.co.duelmonster.minersadvantage.common.shape.builtin.shaft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;

import java.util.LinkedHashSet;

/**
 * Shared placement helper for shaft-related processors so they all respect max-block limits consistently.
 */
public final class ShaftShapePlacement {
    /**
     * Utility class only; no state, no instances, no mysterious side effects.
     */
    private ShaftShapePlacement() {
    }

    /**
     * Add one candidate block position if capacity remains; returns false when budget is exhausted.
     */
    public static boolean addIfCapacity(
        LinkedHashSet<BlockPos> out,
        MAShapeContext context,
        BlockPos base,
        Direction right,
        int lateralOffset,
        int verticalOffset
    ) {
        out.add(base.relative(right, lateralOffset).offset(0, verticalOffset, 0).immutable());
        return true;
    }
}