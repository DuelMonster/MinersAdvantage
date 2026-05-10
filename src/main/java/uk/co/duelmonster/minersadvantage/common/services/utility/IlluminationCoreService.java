package uk.co.duelmonster.minersadvantage.common.services.utility;

public final class IlluminationCoreService {
    public record IlluminationDecision(
        TorchPlacement placement,
        int plannedTorches,
        boolean placeNow,
        boolean manualMode,
        boolean inventoryDepleted
    ) {}

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

    public boolean isManualMode(String toolHint) {
        return toolHint.contains("manual");
    }

    public TorchPlacement selectPlacement(boolean leftWallAvailable, boolean rightWallAvailable, String toolHint) {
        if (toolHint.contains("manual_left")) {
            return leftWallAvailable ? TorchPlacement.LEFT_WALL : TorchPlacement.FLOOR;
        }
        if (toolHint.contains("manual_right")) {
            return rightWallAvailable ? TorchPlacement.RIGHT_WALL : TorchPlacement.FLOOR;
        }
        if (toolHint.contains("manual_both") || toolHint.contains("manual_wall")) {
            if (leftWallAvailable && rightWallAvailable) {
                return TorchPlacement.BOTH_WALLS;
            }
            if (leftWallAvailable) {
                return TorchPlacement.LEFT_WALL;
            }
            if (rightWallAvailable) {
                return TorchPlacement.RIGHT_WALL;
            }
        }
        if (toolHint.contains("manual_floor")) {
            return TorchPlacement.FLOOR;
        }
        return selectPlacement(leftWallAvailable, rightWallAvailable);
    }

    public int availableTorches(String toolHint, int requestedTorches) {
        if (toolHint.contains("empty")) {
            return 0;
        }
        if (toolHint.contains("single")) {
            return 1;
        }
        return requestedTorches;
    }

    public IlluminationDecision decidePlacement(
        int lightLevel,
        boolean leftWallAvailable,
        boolean rightWallAvailable,
        int radiusHorizontal,
        int radiusVertical,
        String toolHint
    ) {
        boolean manualMode = isManualMode(toolHint);
        if (!shouldPlaceTorch(lightLevel)) {
            return new IlluminationDecision(TorchPlacement.FLOOR, 0, false, manualMode, false);
        }

        TorchPlacement placement = selectPlacement(leftWallAvailable, rightWallAvailable, toolHint);
        int placementMultiplier = placement == TorchPlacement.BOTH_WALLS ? 2 : 1;
        int requested = Math.max(1, (expectedPlacementsInRadius(radiusHorizontal, radiusVertical) / 3) * placementMultiplier);
        int available = availableTorches(toolHint, requested);
        boolean inventoryDepleted = available < requested;
        int planned = Math.min(requested, available);
        boolean placeNow = planned > 0;
        return new IlluminationDecision(placement, planned, placeNow, manualMode, inventoryDepleted);
    }
}
