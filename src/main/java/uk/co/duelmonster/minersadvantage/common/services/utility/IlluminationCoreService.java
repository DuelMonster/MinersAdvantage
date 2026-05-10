package uk.co.duelmonster.minersadvantage.common.services.utility;

public final class IlluminationCoreService {
    public boolean shouldPlaceTorch(int lightLevel) {
        return lightLevel < 8;
    }

    public TorchPlacement selectPlacement(boolean leftWallAvailable, boolean rightWallAvailable) {
        if (leftWallAvailable && rightWallAvailable) {
            return TorchPlacement.BOTH_WALLS;
        } else if (leftWallAvailable) {
            return TorchPlacement.LEFT_WALL;
        } else if (rightWallAvailable) {
            return TorchPlacement.RIGHT_WALL;
        }
        return TorchPlacement.FLOOR;
    }

    public int expectedPlacementsInRadius(int radiusHorizontal, int radiusVertical) {
        return (2 * radiusHorizontal + 1) * (2 * radiusVertical + 1);
    }
}
