package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.BlockPos;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;

import java.util.Set;

/**
 * Straightforward cuboid excavation: march depth-forward and fill each centered width/height slice.
 */
public final class DeepCuboidShapeProcessor implements MAShapeProcessor {
    /**
     * Generate all cuboid offsets relative to the hit face until we run out of configured capacity.
     */
    @Override
    /**
     * c om pu te exists so this path stays predictable and easier to debug when things get weird.
     */
    public Set<BlockPos> compute(MAShapeContext context) {
        var plan = ExcavationFaceGeometry.beginWithCenteredWidthAndHeight(context);

        // Triple loop on purpose: depth first, then vertical, then lateral fill for stable shape ordering.
        for (int d = 0; d <= context.depth(); d++) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (int y = plan.heightRange().min(); y <= plan.heightRange().max(); y++) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                for (int w = plan.widthRange().min(); w <= plan.widthRange().max(); w++) {
                    // Hard stop once we hit block budget, because server ticks are not infinite.
                    if (!ExcavationFaceGeometry.hasCapacity(plan.state(), context)) {
                        return plan.state().out();
                    }
                    ExcavationFaceGeometry.addOffset(plan.state(), d, w, y);
                }
            }
        }
        return plan.state().out();
    }
}
