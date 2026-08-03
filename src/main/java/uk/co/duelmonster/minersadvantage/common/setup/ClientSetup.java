package uk.co.duelmonster.minersadvantage.common.setup;

import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;

/**
 * Legacy compatibility entry point for client-side setup wiring.
 */
public final class ClientSetup {
  private final MinersAdvantageCore core;

  /**
   * ClientSetup exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public ClientSetup(MinersAdvantageCore core) {
    this.core = core;
  }

  /**
   * initializeClient exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public void initializeClient() {
    // Client registration points are handled by platform entrypoints. (future-you will thank present-you).
    core.defaultConfig();
  }
}
