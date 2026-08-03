package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.ShapeGeometryUtils;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Single-layer excavation processor that carves one plane oriented by hit face.
 */
public final class SingleLayerShapeProcessor implements MAShapeProcessor {
  /**
   * Compute a one-layer shape using different traversal logic for vertical vs horizontal face hits.
   */
  @Override
  /**
   * c om pu te exists so this path stays predictable and easier to debug when things get weird.
   */
  public Set<BlockPos> compute(MAShapeContext context) {
    LinkedHashSet<BlockPos> out = new LinkedHashSet<>();
    BlockPos origin = context.origin();
    Direction hitFace = context.hitFace();
    ExcavationFaceGeometry.FaceDirection faceDirection = ExcavationFaceGeometry
        .fromMinecraftDirection(context.hitFace());
    ExcavationFaceGeometry.FaceDirection playerFacingDirection = ExcavationFaceGeometry
        .fromMinecraftDirection(context.playerFacing());
    int depth = Math.max(1, context.depth());

    int minW = ShapeGeometryUtils.minRightBiasedCenteredOffset(context.width());
    int maxW = ShapeGeometryUtils.maxRightBiasedCenteredOffset(context.width());

    // Vertical faces map to a flat XZ plane and extend forward based on player facing.
    if (hitFace.getAxis().isVertical()) {
      for (int forward = 0; forward < depth; forward++) {
        int[] forwardOffset = ExcavationFaceGeometry.offsetFor(faceDirection, playerFacingDirection, 0, 0, forward);
        for (int w = minW; w <= maxW; w++) {
          int[] lateralOffset = ExcavationFaceGeometry.offsetFor(faceDirection, playerFacingDirection, 0, w, 0);
          BlockPos pos = origin
              .offset(
                  forwardOffset[0] + lateralOffset[0],
                  forwardOffset[1] + lateralOffset[1],
                  forwardOffset[2] + lateralOffset[2])
              .immutable();
          out.add(pos);
        }
      }

      return out;
    }

    // Horizontal faces push forward by depth while keeping Y fixed to preserve single-layer behavior.
    for (int d = 0; d < depth; d++) {
      for (int w = minW; w <= maxW; w++) {
        int[] offset = ExcavationFaceGeometry.offsetFor(faceDirection, playerFacingDirection, d, w, 0);
        BlockPos pos = origin.offset(offset[0], offset[1], offset[2]).immutable();
        out.add(pos);
      }
    }

    return out;
  }
}
