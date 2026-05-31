package uk.co.duelmonster.minersadvantage.common.shape.builtin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;

import java.util.LinkedHashSet;

/**
 * ShapeGeometryUtils centralizes deterministic shape geometry helpers.
 */
public final class ShapeGeometryUtils {
    /**
     * Utility class only.
     */
    private ShapeGeometryUtils() {
    }

    /**
     * Resolve forward direction from hit face, defaulting vertical hits to player facing.
     */
    public static Direction forwardFromContext(MAShapeContext context) {
        Direction hitFace = context.hitFace();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (hitFace.getAxis().isVertical()) {
            return horizontalOrNorth(context.playerFacing());
        }
        return hitFace.getOpposite();
    }

    /**
     * Ensure direction is horizontal, fallback north when missing/vertical.
     */
    public static Direction horizontalOrNorth(Direction direction) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (direction == null || !direction.getAxis().isHorizontal()) {
            return Direction.NORTH;
        }
        return direction;
    }

    /**
     * Resolve right-side direction from a forward heading.
     */
    public static Direction rightFromForward(Direction forward) {
        Direction horizontalForward = horizontalOrNorth(forward);
        return horizontalForward.getClockWise();
    }

    /**
     * Compute centered minimum offset for a dimension size.
     */
    public static int minCenteredOffset(int size) {
        int half = size / 2;
        return -half;
    }

    /**
     * Compute centered maximum offset for a dimension size.
     */
    public static int maxCenteredOffset(int size) {
        int half = size / 2;
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if ((size & 1) == 0) {
            return half - 1;
        }
        return half;
    }

    /**
     * Return remaining placement capacity for output set against global limit.
     */
    public static int clampToLimit(LinkedHashSet<BlockPos> out, int limit) {
        return Math.max(0, limit - out.size());
    }
}
