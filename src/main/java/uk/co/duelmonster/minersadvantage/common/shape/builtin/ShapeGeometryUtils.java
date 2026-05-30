package uk.co.duelmonster.minersadvantage.common.shape.builtin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;

import java.util.LinkedHashSet;

/**
 * ShapeGeometryUtils centralizes deterministic shape geometry helpers.
 */
public final class ShapeGeometryUtils {
    private ShapeGeometryUtils() {
    }

    public static Direction forwardFromContext(MAShapeContext context) {
        Direction hitFace = context.hitFace();
        if (hitFace.getAxis().isVertical()) {
            return horizontalOrNorth(context.playerFacing());
        }
        return hitFace.getOpposite();
    }

    public static Direction horizontalOrNorth(Direction direction) {
        if (direction == null || !direction.getAxis().isHorizontal()) {
            return Direction.NORTH;
        }
        return direction;
    }

    public static Direction rightFromForward(Direction forward) {
        Direction horizontalForward = horizontalOrNorth(forward);
        return horizontalForward.getClockWise();
    }

    public static int minCenteredOffset(int size) {
        int half = size / 2;
        return -half;
    }

    public static int maxCenteredOffset(int size) {
        int half = size / 2;
        if ((size & 1) == 0) {
            return half - 1;
        }
        return half;
    }

    public static int clampToLimit(LinkedHashSet<BlockPos> out, int limit) {
        return Math.max(0, limit - out.size());
    }
}
