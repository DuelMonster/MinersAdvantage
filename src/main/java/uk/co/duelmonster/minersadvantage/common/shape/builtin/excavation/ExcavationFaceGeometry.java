package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.builtin.ShapeGeometryUtils;

import java.util.LinkedHashSet;

public final class ExcavationFaceGeometry {
    public record ComputePlan(
        ComputeState state,
        IntRange widthRange,
        IntRange heightRange
    ) {
    }

    public record ComputeState(
        LinkedHashSet<BlockPos> out,
        BlockPos origin,
        FaceDirection faceDirection
    ) {
    }

    public record IntRange(int min, int max) {
    }

    public enum FaceDirection {
        NORTH,
        SOUTH,
        EAST,
        WEST,
        UP,
        DOWN
    }

    private ExcavationFaceGeometry() {
    }

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

    public static ComputeState begin(MAShapeContext context) {
        return new ComputeState(
            new LinkedHashSet<>(),
            context.origin(),
            fromMinecraftDirection(context.hitFace())
        );
    }

    public static ComputePlan beginWithCenteredWidthAndHeight(MAShapeContext context) {
        return new ComputePlan(
            begin(context),
            centeredRange(context.width()),
            centeredRange(context.height())
        );
    }

    public static IntRange centeredRange(int size) {
        return new IntRange(
            ShapeGeometryUtils.minCenteredOffset(size),
            ShapeGeometryUtils.maxCenteredOffset(size)
        );
    }

    public static boolean hasCapacity(
        LinkedHashSet<BlockPos> out,
        MAShapeContext context
    ) {
        return out.size() < context.maxBlocks();
    }

    public static boolean hasCapacity(
        ComputeState state,
        MAShapeContext context
    ) {
        return hasCapacity(state.out(), context);
    }

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

    public static void addOffset(
        ComputeState state,
        int depth,
        int width,
        int height
    ) {
        addOffset(state.out(), state.origin(), state.faceDirection(), depth, width, height);
    }
}