package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Shapeless excavation processor that follows connected matching blocks instead of a fixed volume.
 */
public final class ShapelessShapeProcessor implements MAShapeProcessor {
  private static final int[][] NEIGHBOR_OFFSETS = createNeighborOffsets();

  /**
   * Flood-fill connected matching blocks with LiteMiner-style neighbor ordering.
   */
  @Override
  /**
   * c om pu te exists so this path stays predictable and easier to debug when things get weird.
   */
  public Set<BlockPos> compute(MAShapeContext context) {
    LinkedHashSet<BlockPos> out = new LinkedHashSet<>();

    BlockState originState = context.originState();
    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
    if (originState.isAir()) {
      return out;
    }

    Set<BlockPos> envelope = facingAwareEnvelope(context);
    // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
    if (envelope.isEmpty()) {
      return out;
    }

    HashSet<BlockPos> visited = new HashSet<>();
    ArrayDeque<BlockPos> stack = new ArrayDeque<>();
    int[] neighborOrder = new int[NEIGHBOR_OFFSETS.length];
    int[] neighborDistances = new int[NEIGHBOR_OFFSETS.length];
    stack.push(context.origin().immutable());

    // Depth-first expansion mirrors LiteMiner traversal and keeps shape growth local-first.
    while (!stack.isEmpty()) {
      BlockPos current = stack.pop();
      // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
      if (!envelope.contains(current) || visited.contains(current)) {
        continue;
      }

      BlockState state = context.level().getBlockState(current);
      boolean matchesOriginFamily = isMatchingOriginFamily(originState, state);
      // If the trigger block is already gone by the time shapeless computes, seed traversal from origin anyway.
      if (!matchesOriginFamily && current.equals(context.origin()) && state.isAir()) {
        matchesOriginFamily = true;
      }
      // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
      if (!matchesOriginFamily) {
        continue;
      }

      visited.add(current);
      out.add(current.immutable());

      sortNeighborOffsetsByDistance(
          current.getX(),
          current.getY(),
          current.getZ(),
          context.origin().getX(),
          context.origin().getY(),
          context.origin().getZ(),
          neighborOrder,
          neighborDistances);

      for (int i = neighborOrder.length - 1; i >= 0; i--) {
        int[] offset = NEIGHBOR_OFFSETS[neighborOrder[i]];
        BlockPos neighbor = current.offset(offset[0], offset[1], offset[2]);
        if (envelope.contains(neighbor) && !visited.contains(neighbor)) {
          stack.push(neighbor);
        }
      }
    }

    return out;
  }

  private static Set<BlockPos> facingAwareEnvelope(MAShapeContext context) {
    LinkedHashSet<BlockPos> envelope = new LinkedHashSet<>();

    int width = Math.max(1, context.width());
    int height = Math.max(1, context.height());
    int depth = Math.max(1, context.depth());

    ExcavationFaceGeometry.FaceDirection faceDirection = ExcavationFaceGeometry
        .fromMinecraftDirection(context.hitFace());
    ExcavationFaceGeometry.FaceDirection playerFacingDirection = ExcavationFaceGeometry
        .fromMinecraftDirection(context.playerFacing());
    ExcavationFaceGeometry.IntRange widthRange = resolveWidthRange(context, faceDirection, playerFacingDirection,
        width);
    ExcavationFaceGeometry.IntRange heightRange = ExcavationFaceGeometry.rightBiasedCenteredRange(height);

    for (int d = 0; d < depth; d++) {
      for (int h = heightRange.min(); h <= heightRange.max(); h++) {
        for (int w = widthRange.min(); w <= widthRange.max(); w++) {
          int[] offset = ExcavationFaceGeometry.offsetFor(faceDirection, playerFacingDirection, d, w, h);
          envelope.add(context.origin().offset(offset[0], offset[1], offset[2]).immutable());
        }
      }
    }

    return envelope;
  }

  private static ExcavationFaceGeometry.IntRange resolveWidthRange(
      MAShapeContext context,
      ExcavationFaceGeometry.FaceDirection faceDirection,
      ExcavationFaceGeometry.FaceDirection playerFacingDirection,
      int width) {
    ExcavationFaceGeometry.IntRange centered = ExcavationFaceGeometry.rightBiasedCenteredRange(width);
    if (context.player() == null) {
      return centered;
    }

    Vec3 look = context.player().getLookAngle();
    double horizontalLengthSquared = (look.x * look.x) + (look.z * look.z);
    if (horizontalLengthSquared < 1.0e-6D) {
      return centered;
    }

    double inverseLength = 1.0D / Math.sqrt(horizontalLengthSquared);
    double awayX = look.x * inverseLength;
    double awayZ = look.z * inverseLength;

    int[] widthAxis = ExcavationFaceGeometry.offsetFor(faceDirection, playerFacingDirection, 0, 1, 0);
    double axisLengthSquared = (widthAxis[0] * widthAxis[0]) + (widthAxis[2] * widthAxis[2]);
    if (axisLengthSquared < 1.0e-6D) {
      return centered;
    }

    double axisInverseLength = 1.0D / Math.sqrt(axisLengthSquared);
    double axisX = widthAxis[0] * axisInverseLength;
    double axisZ = widthAxis[2] * axisInverseLength;
    double awayProjection = (awayX * axisX) + (awayZ * axisZ);

    // Keep cardinal-facing behavior centered and only bias range when heading is truly diagonal.
    if (Math.abs(awayProjection) < 0.25D) {
      return centered;
    }

    if (awayProjection > 0.0D) {
      return new ExcavationFaceGeometry.IntRange(0, width - 1);
    }
    return new ExcavationFaceGeometry.IntRange(-(width - 1), 0);
  }

  /**
   * Match connected candidates by block identity so state-variant configs can still flow through agent checks.
   */
  static boolean isMatchingOriginFamily(BlockState originState, BlockState state) {
    return !state.isAir() && state.getBlock() == originState.getBlock();
  }

  /**
   * Allow traversal to start even when origin is already air from the initiating break.
   */
  static boolean shouldTreatBrokenOriginAsMatch(
      int currentX,
      int currentY,
      int currentZ,
      int originX,
      int originY,
      int originZ,
      boolean currentIsAir) {
    return currentIsAir
        && currentX == originX
        && currentY == originY
        && currentZ == originZ;
  }

  /**
   * Build and sort 18 neighbors (faces + edges, no corners) by Manhattan distance to the absolute origin.
   */
  static BlockPos[] orderedNeighbors(BlockPos center, BlockPos absoluteOrigin) {
    int[] neighborOrder = new int[NEIGHBOR_OFFSETS.length];
    int[] neighborDistances = new int[NEIGHBOR_OFFSETS.length];
    sortNeighborOffsetsByDistance(
        center.getX(),
        center.getY(),
        center.getZ(),
        absoluteOrigin.getX(),
        absoluteOrigin.getY(),
        absoluteOrigin.getZ(),
        neighborOrder,
        neighborDistances);

    BlockPos[] neighbors = new BlockPos[NEIGHBOR_OFFSETS.length];
    for (int i = 0; i < neighborOrder.length; i++) {
      int[] offset = NEIGHBOR_OFFSETS[neighborOrder[i]];
      neighbors[i] = center.offset(offset[0], offset[1], offset[2]);
    }
    return neighbors;
  }

  /**
   * Sort absolute neighbor coordinates by Manhattan distance to the absolute origin.
   */
  static int[][] orderedNeighborCoordinates(
      int centerX,
      int centerY,
      int centerZ,
      int originX,
      int originY,
      int originZ) {
    int[] neighborOrder = new int[NEIGHBOR_OFFSETS.length];
    int[] neighborDistances = new int[NEIGHBOR_OFFSETS.length];
    sortNeighborOffsetsByDistance(centerX, centerY, centerZ, originX, originY, originZ, neighborOrder,
        neighborDistances);

    int[][] neighbors = new int[NEIGHBOR_OFFSETS.length][];
    for (int i = 0; i < neighborOrder.length; i++) {
      int[] offset = NEIGHBOR_OFFSETS[neighborOrder[i]];
      neighbors[i] = new int[] {
          centerX + offset[0],
          centerY + offset[1],
          centerZ + offset[2]
      };
    }
    return neighbors;
  }

  /**
   * Sort neighbor offset indices by Manhattan distance to the absolute origin.
   */
  private static void sortNeighborOffsetsByDistance(
      int centerX,
      int centerY,
      int centerZ,
      int originX,
      int originY,
      int originZ,
      int[] outOrder,
      int[] outDistances) {
    for (int i = 0; i < NEIGHBOR_OFFSETS.length; i++) {
      int[] offset = NEIGHBOR_OFFSETS[i];
      outOrder[i] = i;
      outDistances[i] = manhattanDistance(
          centerX + offset[0],
          centerY + offset[1],
          centerZ + offset[2],
          originX,
          originY,
          originZ);
    }

    // Insertion sort is efficient for this tiny fixed-size (18) array and avoids comparator/object overhead.
    for (int i = 1; i < outOrder.length; i++) {
      int orderValue = outOrder[i];
      int distanceValue = outDistances[i];
      int j = i - 1;

      while (j >= 0 && outDistances[j] > distanceValue) {
        outOrder[j + 1] = outOrder[j];
        outDistances[j + 1] = outDistances[j];
        j--;
      }

      outOrder[j + 1] = orderValue;
      outDistances[j + 1] = distanceValue;
    }
  }

  /**
   * Precompute all 18 neighbor offsets once to avoid per-call allocation churn.
   */
  static int[][] createNeighborOffsets() {
    int[][] offsets = new int[18][];
    int index = 0;
    for (int y = -1; y <= 1; y++) {
      for (int x = -1; x <= 1; x++) {
        for (int z = -1; z <= 1; z++) {
          // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
          if (x == 0 && y == 0 && z == 0) {
            continue;
          }

          // Exclude 3-axis corner diagonals so traversal uses 18-neighbor connectivity.
          if (x != 0 && y != 0 && z != 0) {
            continue;
          }
          offsets[index++] = new int[] { x, y, z };
        }
      }
    }
    return offsets;
  }

  /**
   * Use explicit Manhattan math for cross-version compatibility.
   */
  static int manhattanDistance(BlockPos a, BlockPos b) {
    return manhattanDistance(a.getX(), a.getY(), a.getZ(), b.getX(), b.getY(), b.getZ());
  }

  /**
   * Manhattan distance with primitive coordinates for lightweight test coverage.
   */
  static int manhattanDistance(int ax, int ay, int az, int bx, int by, int bz) {
    return Math.abs(ax - bx)
        + Math.abs(ay - by)
        + Math.abs(az - bz);
  }
}