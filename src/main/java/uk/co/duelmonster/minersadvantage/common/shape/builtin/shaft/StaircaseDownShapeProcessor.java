package uk.co.duelmonster.minersadvantage.common.shape.builtin.shaft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.ShapeGeometryUtils;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Downward staircase processor that advances forward while dropping one level per step.
 */
public final class StaircaseDownShapeProcessor implements MAShapeProcessor {
  /**
   * Compute descending staircase volume with centered width and configurable stair height thickness.
   */
  @Override
  /**
   * c om pu te exists so this path stays predictable and easier to debug when things get weird.
   */
  public Set<BlockPos> compute(MAShapeContext context) {
    LinkedHashSet<BlockPos> out = new LinkedHashSet<>();
    // If user clicked vertical faces, we fall back to player facing so stairs still have a direction.
    Direction forward = context.hitFace().getAxis().isVertical()
        ? ShapeGeometryUtils.horizontalOrNorth(context.playerFacing())
        : ShapeGeometryUtils.forwardFromContext(context);
    Direction right = ShapeGeometryUtils.rightFromForward(forward);
    BlockPos origin = context.origin();

    int minW = ShapeGeometryUtils.minRightBiasedCenteredOffset(context.width());
    int maxW = ShapeGeometryUtils.maxRightBiasedCenteredOffset(context.width());
    int minH = ShapeGeometryUtils.minRightBiasedCenteredOffset(context.height());
    int maxH = ShapeGeometryUtils.maxRightBiasedCenteredOffset(context.height());

    // Each depth step drops one Y level, which is the whole point of a down staircase.
    for (int d = 0; d < context.depth(); d++) {
      BlockPos depthBase = origin.relative(forward, d).offset(0, -d, 0);
      for (int h = minH; h <= maxH; h++) {
        for (int w = minW; w <= maxW; w++) {
          if (!ShaftShapePlacement.addIfCapacity(out, context, depthBase, right, w, -h)) {
            return out;
          }
        }
      }
    }

    return out;
  }
}
