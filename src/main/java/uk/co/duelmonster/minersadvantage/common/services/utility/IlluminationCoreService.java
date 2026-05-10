package uk.co.duelmonster.minersadvantage.common.services.utility;

public final class IlluminationCoreService {
    public record IlluminationDecision(TorchPlacement placement, int plannedTorches, boolean placeNow) {}

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

    public IlluminationDecision decidePlacement(
        int lightLevel,
        boolean leftWallAvailable,
        boolean rightWallAvailable,
        int radiusHorizontal,
        int radiusVertical
    ) {
        if (!shouldPlaceTorch(lightLevel)) {
            return new IlluminationDecision(TorchPlacement.FLOOR, 0, false);
        }

        TorchPlacement placement = selectPlacement(leftWallAvailable, rightWallAvailable);
        int planned = Math.max(1, expectedPlacementsInRadius(radiusHorizontal, radiusVertical) / 3);
        return new IlluminationDecision(placement, planned, true);
    }
}
