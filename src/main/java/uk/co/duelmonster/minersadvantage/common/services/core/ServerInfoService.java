package uk.co.duelmonster.minersadvantage.common.services.core;

/**
 * ServerInfoService keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class ServerInfoService {
  private static boolean isOnServer = false;
  private static String worldUid = null;
  private static final String DEFAULT_WORLD_UID = "world" + "default".hashCode();

  private ServerInfoService() {
  }

  /**
   * isOnServer exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public static boolean isOnServer() {
    return isOnServer;
  }

  /**
   * onConnectedToServer exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public static void onConnectedToServer(boolean isNowOnServer) {
    isOnServer = isNowOnServer;
    worldUid = null;
  }

  /**
   * getWorldUid exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public static String getWorldUid() {
    if (worldUid == null) {
      worldUid = DEFAULT_WORLD_UID;
    }
    return worldUid;
  }

  /**
   * getWorldUid exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public static String getWorldUid(boolean localChannel, String localWorldName, String serverAddress,
      String serverName) {
    if (worldUid == null) {
      String source;
      if (localChannel) {
        source = localWorldName;
      } else {
        String address = serverAddress == null ? "" : serverAddress;
        String name = serverName == null ? "" : serverName;
        source = (address + " " + name).trim();
      }

      if (source == null || source.isBlank()) {
        source = "default";
      }

      worldUid = "world" + source.hashCode();
    }

    return worldUid;
  }
}
