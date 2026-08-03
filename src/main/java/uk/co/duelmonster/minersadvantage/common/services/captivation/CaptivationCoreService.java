package uk.co.duelmonster.minersadvantage.common.services.captivation;

import java.util.Set;

/**
 * CaptivationCoreService keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class CaptivationCoreService {
  /**
   * CaptureDecision keeps this part of MinersAdvantage running without turning server ticks into confetti.
   * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
   */
  public record CaptureDecision(boolean canCapture, boolean blockedByGui, boolean withinRadius) {
  }

  private final Set<String> blacklist;
  private final boolean isWhitelist;
  private final boolean unconditionalBlacklist;

  /**
   * c ap ti va ti on co re se rv ic e exists so this path stays predictable and easier to debug when things get weird.
   */
  public CaptivationCoreService(Set<String> blacklist, boolean isWhitelist, boolean unconditionalBlacklist) {
    this.blacklist = blacklist;
    this.isWhitelist = isWhitelist;
    this.unconditionalBlacklist = unconditionalBlacklist;
  }

  /**
   * canCaptureItem exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
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

  /**
   * isWithinRadius exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public boolean isWithinRadius(int playerX, int playerY, int playerZ, int itemX, int itemY, int itemZ,
      int radiusHorizontal, int radiusVertical) {
    int dx = Math.abs(playerX - itemX);
    int dy = Math.abs(playerY - itemY);
    int dz = Math.abs(playerZ - itemZ);
    return dx <= radiusHorizontal && dz <= radiusHorizontal && dy <= radiusVertical;
  }

  public CaptureDecision evaluateCapture(
      String itemId,
      boolean isDirectPickup,
      boolean inventoryOpen,
      boolean allowInGUI,
      boolean withinRadius) {
    if (inventoryOpen && !allowInGUI) {
      return new CaptureDecision(false, true, withinRadius);
    }

    boolean canCapture = withinRadius && canCaptureItem(itemId, isDirectPickup);
    return new CaptureDecision(canCapture, false, withinRadius);
  }
}
