package uk.co.duelmonster.minersadvantage.common.services.illumination;

public final class IlluminationCoreService {
    public enum TorchPlacement {
        FLOOR,
        LEFT_WALL,
        RIGHT_WALL,
        BOTH_WALLS
    }

    public boolean shouldPlaceTorch(int sampledLightLevel, int lowestLightLevel, boolean useBlockLight) {
        int effectiveLevel = useBlockLight ? sampledLightLevel : sampledLightLevel;
        return effectiveLevel <= lowestLightLevel;
    }

    public int expectedPlacements(TorchPlacement placement) {
        return switch (placement) {
            case FLOOR, LEFT_WALL, RIGHT_WALL -> 1;
            case BOTH_WALLS -> 2;
        };
    }
}
