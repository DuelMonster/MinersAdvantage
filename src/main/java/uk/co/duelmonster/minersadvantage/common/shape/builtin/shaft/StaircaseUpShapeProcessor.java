package uk.co.duelmonster.minersadvantage.common.shape.builtin.shaft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.ShapeGeometryUtils;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Upward staircase processor that climbs one level for each forward depth step.
 */
public final class StaircaseUpShapeProcessor implements MAShapeProcessor {
    /**
     * Compute ascending staircase volume with centered lateral spread and configurable step thickness.
     */
    @Override
    /**
     * c om pu te exists so this path stays predictable and easier to debug when things get weird.
     */
    public Set<BlockPos> compute(MAShapeContext context) {
        LinkedHashSet<BlockPos> out = new LinkedHashSet<>();
        // Vertical hits get remapped to horizontal facing so we don't build staircases into the void.
        Direction forward = context.hitFace().getAxis().isVertical()
            ? ShapeGeometryUtils.horizontalOrNorth(context.playerFacing())
            : ShapeGeometryUtils.forwardFromContext(context);
        Direction right = ShapeGeometryUtils.rightFromForward(forward);
        BlockPos origin = context.origin();

        int minW = ShapeGeometryUtils.minRightBiasedCenteredOffset(context.width());
        int maxW = ShapeGeometryUtils.maxRightBiasedCenteredOffset(context.width());
        int minH = ShapeGeometryUtils.minRightBiasedCenteredOffset(context.height());
        int maxH = ShapeGeometryUtils.maxRightBiasedCenteredOffset(context.height());

        // Each depth step raises one Y level for a proper ascending staircase profile.
        for (int d = 0; d < context.depth(); d++) {
            BlockPos depthBase = origin.relative(forward, d).offset(0, d, 0);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (int h = minH; h <= maxH; h++) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                for (int w = minW; w <= maxW; w++) {
                    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                    if (!ShaftShapePlacement.addIfCapacity(out, context, depthBase, right, w, h)) {
                        return out;
                    }
                }
            }
        }

        return out;
    }
}
