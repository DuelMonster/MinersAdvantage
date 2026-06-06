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

        int playerFeetY = context.player() == null ? context.origin().getY() : context.player().blockPosition().getY();
        int floorOffset = ShaftFloorGeometry.resolveFloorOffset(context.origin().getY(), playerFeetY, context.height());
        return computeAtOrigin(
            context.hitFace(),
            context.playerFacing(),
            context.width(),
            context.height(),
            context.depth(),
            floorOffset
        );
    }

    /**
     * Compute shaft geometry relative to origin so it can be precomputed and reused.
     */
    public static Set<BlockPos> computeAtOrigin(
        Direction hitFace,
        Direction playerFacing,
        int width,
        int height,
        int depth,
        int floorOffset
    ) {
        if (hitFace.getAxis().isVertical()) {
            return Set.of();
        }

        LinkedHashSet<BlockPos> out = new LinkedHashSet<>();
        Direction forward = hitFace.getOpposite();
        Direction right = ShapeGeometryUtils.rightFromForward(forward);
        BlockPos origin = BlockPos.ZERO;

        int floorY = floorOffset;
        int minW = ShapeGeometryUtils.minCenteredOffset(width);
        int maxW = ShapeGeometryUtils.maxCenteredOffset(width);

        // Depth-first traversal gives predictable forward growth for both visuals and behavior parity.
        for (int d = 0; d < depth; d++) {
            BlockPos base = new BlockPos(origin.getX(), floorY, origin.getZ()).relative(forward, d);
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (int h = 0; h < height; h++) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                for (int w = minW; w <= maxW; w++) {
                    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                    if (!ShaftShapePlacement.addIfCapacity(out, null, base, right, w, h)) {
                        return out;
                    }
                }
            }
        }
        return out;
    }
}
