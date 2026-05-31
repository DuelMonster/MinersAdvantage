package uk.co.duelmonster.minersadvantage.common.shape.builtin.shaft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.ShapeGeometryUtils;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Classic straight shaft processor: dig forward, then fill each cross-section by height and width.
 */
public final class ShaftShapeProcessor implements MAShapeProcessor {
    /**
     * Compute a horizontal shaft volume based on player-facing context and configured dimensions.
     */
    @Override
    /**
     * c om pu te exists so this path stays predictable and easier to debug when things get weird.
     */
    public Set<BlockPos> compute(MAShapeContext context) {
        // Vertical face hits don't define a meaningful horizontal shaft direction, so we decline politely.
        if (context.hitFace().getAxis().isVertical()) {
            return Set.of();
        }

        LinkedHashSet<BlockPos> out = new LinkedHashSet<>();
        Direction forward = ShapeGeometryUtils.forwardFromContext(context);
        Direction right = ShapeGeometryUtils.rightFromForward(forward);
        BlockPos origin = context.origin();

        int floorY = ShaftFloorGeometry.resolveFloorY(origin.getY(), context.player().blockPosition().getY(), context.height());
        int minW = ShapeGeometryUtils.minCenteredOffset(context.width());
        int maxW = ShapeGeometryUtils.maxCenteredOffset(context.width());

        // Depth-first traversal gives predictable forward growth for both visuals and behavior parity.
        for (int d = 0; d < context.depth(); d++) {
            BlockPos base = new BlockPos(origin.getX(), floorY, origin.getZ()).relative(forward, d);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (int h = 0; h < context.height(); h++) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                for (int w = minW; w <= maxW; w++) {
                    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                    if (!ShaftShapePlacement.addIfCapacity(out, context, base, right, w, h)) {
                        return out;
                    }
                }
            }
        }
        return out;
    }
}
