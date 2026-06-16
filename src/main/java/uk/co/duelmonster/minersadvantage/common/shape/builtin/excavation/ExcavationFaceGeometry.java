package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.ShapeGeometryUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Shared excavation geometry helper that keeps direction math and offset wiring in one place,
 * so shape processors can focus on shape logic instead of coordinate acrobatics.
 */
public final class ExcavationFaceGeometry {
    private static final AtomicInteger CROSS_SECTION_DEBUG_LOG_BUDGET = new AtomicInteger(4);

    /**
     * Debug helper that captures one cross-section summary per depth layer.
     */
    public static final class DepthCrossSectionTracker {
        private final boolean enabled;
        private final String shapeId;
        private final MAShapeContext context;
        private final int totalDepth;
        private final int centerStretch;
        private final int[] counts;
        private final int[] minWidth;
        private final int[] maxWidth;
        private final int[] minHeight;
        private final int[] maxHeight;
        private boolean truncated;
        private int truncatedDepth;
        private int truncatedCount;

        private DepthCrossSectionTracker(String shapeId, MAShapeContext context, int totalDepth, int centerStretch) {
            this.enabled = LogUtils.isDebugLoggingEnabled();
            this.shapeId = shapeId;
            this.context = context;
            this.totalDepth = Math.max(0, totalDepth);
            this.centerStretch = Math.max(0, centerStretch);

            if (!enabled || this.totalDepth == 0) {
                this.counts = null;
                this.minWidth = null;
                this.maxWidth = null;
                this.minHeight = null;
                this.maxHeight = null;
                return;
            }

            this.counts = new int[this.totalDepth];
            this.minWidth = new int[this.totalDepth];
            this.maxWidth = new int[this.totalDepth];
            this.minHeight = new int[this.totalDepth];
            this.maxHeight = new int[this.totalDepth];
            Arrays.fill(this.minWidth, Integer.MAX_VALUE);
            Arrays.fill(this.maxWidth, Integer.MIN_VALUE);
            Arrays.fill(this.minHeight, Integer.MAX_VALUE);
            Arrays.fill(this.maxHeight, Integer.MIN_VALUE);
            this.truncated = false;
            this.truncatedDepth = -1;
            this.truncatedCount = 0;
        }

        public void record(int depthIndex, int widthOffset, int heightOffset) {
            if (!enabled || depthIndex < 0 || depthIndex >= totalDepth) {
                return;
            }

            counts[depthIndex]++;
            minWidth[depthIndex] = Math.min(minWidth[depthIndex], widthOffset);
            maxWidth[depthIndex] = Math.max(maxWidth[depthIndex], widthOffset);
            minHeight[depthIndex] = Math.min(minHeight[depthIndex], heightOffset);
            maxHeight[depthIndex] = Math.max(maxHeight[depthIndex], heightOffset);
        }

        public void markTruncated(int depthIndex, int emittedCount) {
            if (!enabled || truncated) {
                return;
            }
            truncated = true;
            truncatedDepth = depthIndex;
            truncatedCount = Math.max(0, emittedCount);
        }

        public void log() {
            boolean debugEnabledNow = LogUtils.isDebugLoggingEnabled();
            if (!enabled || !debugEnabledNow || totalDepth == 0) {
                return;
            }

            // Guardrail: limit noisy startup diagnostics so debug logging does not starve the render thread.
            if (CROSS_SECTION_DEBUG_LOG_BUDGET.getAndDecrement() <= 0) {
                return;
            }

            int emittedBlocks = 0;
            for (int depthIndex = 0; depthIndex < totalDepth; depthIndex++) {
                emittedBlocks += counts[depthIndex];
            }

            LogUtils.logDebug(
                "Ellipsoid cross-sections shape={} origin={} hitFace={} playerFacing={} width={} height={} depth={} centerStretch={} truncated={} truncatedDepth={} emittedBlocks={} debugLogging={}",
                shapeId,
                context.origin(),
                context.hitFace(),
                context.playerFacing(),
                context.width(),
                context.height(),
                context.depth(),
                centerStretch,
                truncated,
                truncatedDepth,
                emittedBlocks,
                debugEnabledNow
            );
        }
    }

    /**
     * Bundles state plus centered ranges so processors can start work without repeating setup boilerplate.
     */
    public record ComputePlan(
        ComputeState state,
        IntRange widthRange,
        IntRange heightRange
    ) {
    }

    /**
     * Mutable-ish compute context passed through inner loops without hauling three separate locals around.
     */
    public record ComputeState(
        LinkedHashSet<BlockPos> out,
        BlockPos origin,
        FaceDirection hitFace,
        FaceDirection playerFacing
    ) {
    }

    /**
     * Tiny inclusive range holder used for centered width/height traversal.
     */
    public record IntRange(int min, int max) {
    }

    /**
     * Internal face enum so processors avoid Minecraft-direction-specific branching in every loop.
     */
    public enum FaceDirection {
        NORTH,
        SOUTH,
        EAST,
        WEST,
        UP,
        DOWN
    }

    /**
     * Utility class only; if someone instantiates this, they owe the team snacks.
     */
    private ExcavationFaceGeometry() {
    }

    /**
     * Convert the hit face once up front so loop code can stay branch-light.
     */
    public static FaceDirection fromMinecraftDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> FaceDirection.NORTH;
            case SOUTH -> FaceDirection.SOUTH;
            case EAST -> FaceDirection.EAST;
            case WEST -> FaceDirection.WEST;
            case UP -> FaceDirection.UP;
            case DOWN -> FaceDirection.DOWN;
        };
    }

    /**
     * Translate logical depth/width/height movement into world-axis offsets based on chosen face.
     */
    public static int[] offsetFor(FaceDirection faceDirection, int depth, int width, int height) {
        return switch (faceDirection) {
            case NORTH -> new int[] {width, height, depth};
            case SOUTH -> new int[] {width, height, -depth};
            case EAST -> new int[] {-depth, height, width};
            case WEST -> new int[] {depth, height, width};
            case UP -> new int[] {width, -depth, height};
            case DOWN -> new int[] {width, depth, height};
        };
    }

    /**
     * Resolve excavation offsets while preserving player-facing depth for floor and ceiling hits.
     */
    public static int[] offsetFor(FaceDirection hitFace, FaceDirection playerFacing, int depth, int width, int height) {
        if (hitFace == FaceDirection.UP || hitFace == FaceDirection.DOWN) {
            int verticalDepth = hitFace == FaceDirection.UP ? -depth : depth;
            return switch (horizontalOrNorth(playerFacing)) {
                case NORTH -> new int[] {width, verticalDepth, -height};
                case SOUTH -> new int[] {-width, verticalDepth, height};
                case EAST -> new int[] {height, verticalDepth, width};
                case WEST -> new int[] {-height, verticalDepth, -width};
                case UP, DOWN -> throw new IllegalStateException("horizontalOrNorth returned vertical direction");
            };
        }
        return offsetFor(hitFace, depth, width, height);
    }

    /**
     * Resolve Deep Cuboid offsets with depth always traveling away from the hit face.
     */
    public static int[] deepCuboidOffset(FaceDirection hitFace, int depth, int width, int height) {
        return switch (hitFace) {
            case NORTH -> new int[] {width, height, depth};
            case SOUTH -> new int[] {width, height, -depth};
            case EAST -> new int[] {-depth, height, width};
            case WEST -> new int[] {depth, height, width};
            case UP -> new int[] {width, -depth, height};
            case DOWN -> new int[] {width, depth, height};
        };
    }

    /**
     * Resolve Wide Cuboid offsets using forward width, side depth, and player-facing vertical orientation.
     */
    public static int[] wideCuboidOffset(FaceDirection hitFace, FaceDirection playerFacing, int forwardWidth, int sideDepth, int height) {
        return switch (hitFace) {
            case NORTH -> new int[] {sideDepth, height, forwardWidth};
            case SOUTH -> new int[] {-sideDepth, height, -forwardWidth};
            case EAST -> new int[] {-forwardWidth, height, sideDepth};
            case WEST -> new int[] {forwardWidth, height, -sideDepth};
            case UP -> switch (horizontalOrNorth(playerFacing)) {
                case NORTH -> new int[] {sideDepth, -height, forwardWidth};
                case SOUTH -> new int[] {-sideDepth, -height, forwardWidth};
                case EAST -> new int[] {forwardWidth, -height, sideDepth};
                case WEST -> new int[] {forwardWidth, -height, -sideDepth};
                case UP, DOWN -> throw new IllegalStateException("horizontalOrNorth returned vertical direction");
            };
            case DOWN -> switch (horizontalOrNorth(playerFacing)) {
                case NORTH -> new int[] {sideDepth, height, forwardWidth};
                case SOUTH -> new int[] {-sideDepth, height, forwardWidth};
                case EAST -> new int[] {forwardWidth, height, sideDepth};
                case WEST -> new int[] {forwardWidth, height, -sideDepth};
                case UP, DOWN -> throw new IllegalStateException("horizontalOrNorth returned vertical direction");
            };
        };
    }

    /**
     * Create the base compute state with origin and resolved face direction.
     */
    public static ComputeState begin(MAShapeContext context) {
        return new ComputeState(
            new LinkedHashSet<>(),
            context.origin(),
            fromMinecraftDirection(context.hitFace()),
            fromMinecraftDirection(context.playerFacing())
        );
    }

    /**
     * Normalize missing or vertical facings to north for floor and ceiling excavation anchoring.
     */
    public static FaceDirection horizontalOrNorth(FaceDirection faceDirection) {
        if (faceDirection == null || faceDirection == FaceDirection.UP || faceDirection == FaceDirection.DOWN) {
            return FaceDirection.NORTH;
        }
        return faceDirection;
    }

    /**
     * Precompute the two centered ranges most excavation shapes need, because repetition is boring.
     */
    public static ComputePlan beginWithCenteredWidthAndHeight(MAShapeContext context) {
        return new ComputePlan(
            begin(context),
            centeredRange(context.width()),
            centeredRange(context.height())
        );
    }

    /**
     * Create a debug cross-section tracker that logs once per depth layer when debug logging is enabled.
     */
    public static DepthCrossSectionTracker depthCrossSectionTracker(
        String shapeId,
        MAShapeContext context,
        int totalDepth,
        int centerStretch
    ) {
        return new DepthCrossSectionTracker(shapeId, context, totalDepth, centerStretch);
    }

    /**
     * Build an inclusive centered range for the supplied size.
     */
    public static IntRange centeredRange(int size) {
        return new IntRange(
            ShapeGeometryUtils.minRightBiasedCenteredOffset(size),
            ShapeGeometryUtils.maxRightBiasedCenteredOffset(size)
        );
    }

    /**
     * Build an inclusive centered range that biases the extra block toward positive coordinates for even sizes.
     */
    public static IntRange rightBiasedCenteredRange(int size) {
        int normalized = Math.max(1, size);
        int min = -Math.floorDiv(normalized, 2) + ((normalized & 1) == 0 ? 1 : 0);
        return new IntRange(min, min + normalized - 1);
    }

    /**
     * Compatibility helper kept so older processors can call capacity checks while limits are unbounded.
     */
    public static boolean hasCapacity(
        LinkedHashSet<BlockPos> out,
        MAShapeContext context
    ) {
        return true;
    }

    /**
     * Compatibility overload for callers carrying ComputeState.
     */
    public static boolean hasCapacity(
        ComputeState state,
        MAShapeContext context
    ) {
        return true;
    }

    /**
     * Resolve and add one immutable world position from local shape coordinates.
     */
    public static void addOffset(
        LinkedHashSet<BlockPos> out,
        BlockPos origin,
        FaceDirection hitFace,
        FaceDirection playerFacing,
        int depth,
        int width,
        int height
    ) {
        int[] offset = offsetFor(hitFace, playerFacing, depth, width, height);
        out.add(origin.offset(offset[0], offset[1], offset[2]).immutable());
    }

    /**
     * Convenience overload that uses the origin/face already stored in ComputeState.
     */
    public static void addOffset(
        ComputeState state,
        int depth,
        int width,
        int height
    ) {
        addOffset(state.out(), state.origin(), state.hitFace(), state.playerFacing(), depth, width, height);
    }
}