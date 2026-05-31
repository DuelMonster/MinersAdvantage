package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.Direction;

public final class ExcavationFaceGeometry {
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
}