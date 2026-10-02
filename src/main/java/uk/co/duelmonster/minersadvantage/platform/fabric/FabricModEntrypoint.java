//? if fabric {
package uk.co.duelmonster.minersadvantage.platform.fabric;

import net.fabricmc.api.ModInitializer;
import uk.co.duelmonster.minersadvantage.ModEntry;

public final class FabricModEntrypoint implements ModInitializer {
  private final ModEntry runtime = new ModEntry();

  @Override
  public void onInitialize() {
    runtime.initialize();
  }
}
//?} else {
/*
// This class is Fabric-only.
*/ //?}
