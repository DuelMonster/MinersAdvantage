//? if neoforge {
package uk.co.duelmonster.minersadvantage.platform.neoforge.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.level.BlockEvent;
//? if >=26.1 {
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
//?}

public final class NeoForgeBreakEvents {
  private NeoForgeBreakEvents() {
  }

  @FunctionalInterface
  public interface BreakHandler {
    void handle(Player player, Level level, BlockPos pos, BlockState state);
  }

  public static void register(IEventBus bus, BreakHandler handler) {
    //? if mc1 {
    bus.addListener((BlockEvent.BreakEvent event) -> handle(event, event.getPlayer(), handler));
    //?} else {
    bus.addListener((BreakBlockEvent event) -> handle(event, event.getPlayer(), handler));
    //?}
  }

  private static void handle(BlockEvent event, Player player, BreakHandler handler) {
    if (player != null && event.getLevel() instanceof Level level) {
      handler.handle(player, level, event.getPos(), event.getState());
    }
  }
}
//?} else {
/*
// This class is NeoForge-only.
*/ //?}