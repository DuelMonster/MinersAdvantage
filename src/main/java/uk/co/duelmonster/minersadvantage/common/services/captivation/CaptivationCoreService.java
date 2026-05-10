package uk.co.duelmonster.minersadvantage.common.services.captivation;

import java.util.Set;

public final class CaptivationCoreService {
    private final Set<String> blacklist;
    private final boolean isWhitelist;
    private final boolean unconditionalBlacklist;

    public CaptivationCoreService(Set<String> blacklist, boolean isWhitelist, boolean unconditionalBlacklist) {
        this.blacklist = blacklist;
        this.isWhitelist = isWhitelist;
        this.unconditionalBlacklist = unconditionalBlacklist;
    }

    public boolean canCaptureItem(String itemId, boolean isDirectPickup) {
        if (unconditionalBlacklist && blacklist.contains(itemId)) {
            return false;
        }
        if (isDirectPickup) {
            return true;
        }
        if (isWhitelist) {
            return blacklist.contains(itemId);
        } else {
            return !blacklist.contains(itemId);
        }
    }

    public boolean isWithinRadius(int playerX, int playerY, int playerZ, int itemX, int itemY, int itemZ, int radiusHorizontal, int radiusVertical) {
        int dx = Math.abs(playerX - itemX);
        int dy = Math.abs(playerY - itemY);
        int dz = Math.abs(playerZ - itemZ);
        return dx <= radiusHorizontal && dz <= radiusHorizontal && dy <= radiusVertical;
    }
}
