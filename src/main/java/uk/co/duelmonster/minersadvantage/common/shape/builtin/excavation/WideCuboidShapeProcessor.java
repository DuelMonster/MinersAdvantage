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
    var sideRange = ExcavationFaceGeometry.rightBiasedCenteredRange(context.depth());
    var verticalForwardRange = ExcavationFaceGeometry.rightBiasedCenteredRange(context.width());

    if (state.hitFace() == ExcavationFaceGeometry.FaceDirection.UP
        || state.hitFace() == ExcavationFaceGeometry.FaceDirection.DOWN) {
      for (int verticalStep = 0; verticalStep < context.height(); verticalStep++) {
        for (int forward = verticalForwardRange.min(); forward <= verticalForwardRange.max(); forward++) {
          for (int side = sideRange.min(); side <= sideRange.max(); side++) {
            if (!ExcavationFaceGeometry.hasCapacity(state, context)) {
              return state.out();
            }
            int[] offset = ExcavationFaceGeometry.wideCuboidOffset(
                state.hitFace(),
                state.playerFacing(),
                forward,
                side,
                verticalStep);
            state.out().add(state.origin().offset(offset[0], offset[1], offset[2]).immutable());
          }
        }
      }
      return state.out();
    }

    var heightRange = ExcavationFaceGeometry.rightBiasedCenteredRange(context.height());

    // Horizontal hits start at the target face and project width forward from origin.
    for (int forward = 0; forward < context.width(); forward++) {
      for (int y = heightRange.min(); y <= heightRange.max(); y++) {
        for (int side = sideRange.min(); side <= sideRange.max(); side++) {
          if (!ExcavationFaceGeometry.hasCapacity(state, context)) {
            return state.out();
          }
          int[] offset = ExcavationFaceGeometry.wideCuboidOffset(
              state.hitFace(),
              state.playerFacing(),
              forward,
              side,
              y);
          state.out().add(state.origin().offset(offset[0], offset[1], offset[2]).immutable());
        }
      }
    }

    return state.out();
  }
}
