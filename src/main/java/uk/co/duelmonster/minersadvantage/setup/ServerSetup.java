package uk.co.duelmonster.minersadvantage.setup;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import uk.co.duelmonster.minersadvantage.events.server.ServerEventHandler;

public class ServerSetup {
	@SubscribeEvent
	public void onServerStarting(FMLServerStartingEvent e) {
		MinecraftForge.EVENT_BUS.register(new ServerEventHandler());
	}
}
