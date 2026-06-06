package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapePrecomputeCache;
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

        Set<BlockPos> envelope = MAShapePrecomputeCache.excavationEnvelopeAt(
            context.origin(),
            context.width(),
            context.height(),
            context.depth(),
            context.hitFace(),
            context.playerFacing()
        );
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (envelope.isEmpty()) {
            return out;
        }

        HashSet<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> stack = new ArrayDeque<>();
        int[] neighborOrder = new int[NEIGHBOR_OFFSETS.length];
        int[] neighborDistances = new int[NEIGHBOR_OFFSETS.length];
        boolean originAirSeeded = false;
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
                originAirSeeded = true;
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
                neighborDistances
            );

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
        boolean currentIsAir
    ) {
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
            neighborDistances
        );

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
        int originZ
    ) {
        int[] neighborOrder = new int[NEIGHBOR_OFFSETS.length];
        int[] neighborDistances = new int[NEIGHBOR_OFFSETS.length];
        sortNeighborOffsetsByDistance(centerX, centerY, centerZ, originX, originY, originZ, neighborOrder, neighborDistances);

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
        int[] outDistances
    ) {
        for (int i = 0; i < NEIGHBOR_OFFSETS.length; i++) {
            int[] offset = NEIGHBOR_OFFSETS[i];
            outOrder[i] = i;
            outDistances[i] = manhattanDistance(
                centerX + offset[0],
                centerY + offset[1],
                centerZ + offset[2],
                originX,
                originY,
                originZ
            );
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
                    offsets[index++] = new int[] {x, y, z};
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