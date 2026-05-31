package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.ShapeGeometryUtils;

import java.util.LinkedHashSet;

/**
 * Shared excavation geometry helper that keeps direction math and offset wiring in one place,
 * so shape processors can focus on shape logic instead of coordinate acrobatics.
 */
public final class ExcavationFaceGeometry {
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
        FaceDirection faceDirection
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
     * Create the base compute state with origin and resolved face direction.
     */
    public static ComputeState begin(MAShapeContext context) {
        return new ComputeState(
            new LinkedHashSet<>(),
            context.origin(),
            fromMinecraftDirection(context.hitFace())
        );
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
     * Build an inclusive centered range for the supplied size.
     */
    public static IntRange centeredRange(int size) {
        return new IntRange(
            ShapeGeometryUtils.minCenteredOffset(size),
            ShapeGeometryUtils.maxCenteredOffset(size)
        );
    }

    /**
     * Guard against overshooting max block count while filling output sets.
     */
    public static boolean hasCapacity(
        LinkedHashSet<BlockPos> out,
        MAShapeContext context
    ) {
        return out.size() < context.maxBlocks();
    }

    /**
     * Convenience overload for callers already carrying a ComputeState.
     */
    public static boolean hasCapacity(
        ComputeState state,
        MAShapeContext context
    ) {
        return hasCapacity(state.out(), context);
    }

    /**
     * Resolve and add one immutable world position from local shape coordinates.
     */
    public static void addOffset(
        LinkedHashSet<BlockPos> out,
        BlockPos origin,
        FaceDirection faceDirection,
        int depth,
        int width,
        int height
    ) {
        int[] offset = offsetFor(faceDirection, depth, width, height);
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
        addOffset(state.out(), state.origin(), state.faceDirection(), depth, width, height);
    }
}