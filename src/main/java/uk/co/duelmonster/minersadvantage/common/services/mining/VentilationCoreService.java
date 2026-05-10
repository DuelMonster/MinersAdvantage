package uk.co.duelmonster.minersadvantage.common.services.mining;

public final class VentilationCoreService {
    public boolean isCave(int surfaceLevel, int currentLevel) {
        return currentLevel < surfaceLevel - 10;
    }

    public int estimatedTurnsToVentilate(int radiusHorizontal, int radiusVertical) {
        int volume = (2 * radiusHorizontal + 1) * (2 * radiusHorizontal + 1) * (2 * radiusVertical + 1);
        return volume / 2;
    }
}
