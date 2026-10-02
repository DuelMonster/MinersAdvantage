//? if neoforge {
package uk.co.duelmonster.minersadvantage.platform.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import uk.co.duelmonster.minersadvantage.ModEntry;

@Mod("minersadvantage")
public final class NeoForgeModEntrypoint {
  public NeoForgeModEntrypoint(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
    new ModEntry(modEventBus, modContainer, dist);
  }
}
//?} else {
/*
// This class is NeoForge-only.
*/ //?}
