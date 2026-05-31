package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.BlockPos;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;

import java.util.Set;

/**
 * Wide cuboid excavation that treats configured width as forward distance and depth as side spread.
 */
public final class WideCuboidShapeProcessor implements MAShapeProcessor {
    /**
     * Fill a wide tunnel-like cuboid while respecting max block budget.
     */
    @Override
    /**
     * c om pu te exists so this path stays predictable and easier to debug when things get weird.
     */
    public Set<BlockPos> compute(MAShapeContext context) {
        ExcavationFaceGeometry.ComputeState state = ExcavationFaceGeometry.begin(context);
        var sideRange = ExcavationFaceGeometry.centeredRange(context.depth());
        var heightRange = ExcavationFaceGeometry.centeredRange(context.height());

        // Forward-first traversal keeps this shape intuitive for players aiming into space ahead.
        for (int forwardStep = 0; forwardStep <= context.width(); forwardStep++) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (int y = heightRange.min(); y <= heightRange.max(); y++) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                for (int side = sideRange.min(); side <= sideRange.max(); side++) {
                    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                    if (!ExcavationFaceGeometry.hasCapacity(state, context)) {
                        return state.out();
                    }
                    ExcavationFaceGeometry.addOffset(state, forwardStep, side, y);
                }
            }
        }

        return state.out();
    }
}
