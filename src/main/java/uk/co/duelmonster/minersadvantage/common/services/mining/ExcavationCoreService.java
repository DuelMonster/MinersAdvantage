package uk.co.duelmonster.minersadvantage.common.services.mining;

public final class ExcavationCoreService {
    public boolean isBlock(String blockId) {
        return blockId != null && !blockId.isEmpty() && !blockId.equals("air");
    }

    public boolean isOre(String blockId) {
        return blockId.contains("ore");
    }

    public int estimatedTurnsToExcavate(int radiusHorizontal, int radiusVertical) {
        int volume = (2 * radiusHorizontal + 1) * (2 * radiusHorizontal + 1) * (2 * radiusVertical + 1);
        return volume;
    }
}
