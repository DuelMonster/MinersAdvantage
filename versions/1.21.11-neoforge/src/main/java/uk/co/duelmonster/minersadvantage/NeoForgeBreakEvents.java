package uk.co.duelmonster.minersadvantage;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.level.BlockEvent;

public final class NeoForgeBreakEvents {

    private NeoForgeBreakEvents() {}

    @FunctionalInterface
    public interface BreakHandler {
        void handle(Player player, Level level, BlockPos pos, BlockState state);
    }

    public static void register(IEventBus bus, BreakHandler handler) {
        bus.addListener((BlockEvent.BreakEvent event) -> {
            if (event.getLevel() instanceof Level level) {
                handler.handle(event.getPlayer(), level, event.getPos(), event.getState());
            }
        });
    }
}
