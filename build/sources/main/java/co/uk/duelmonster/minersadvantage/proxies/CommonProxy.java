package co.uk.duelmonster.minersadvantage.proxies;

import co.uk.duelmonster.minersadvantage.MinersAdvantage;
import co.uk.duelmonster.minersadvantage.events.ServerEvents;
import co.uk.duelmonster.minersadvantage.events.SharedEvents;
import co.uk.duelmonster.minersadvantage.packets.PacketBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy {
	
	public boolean isClient() {
		return false;
	}
	
	public void registerHandlers() {
		MinecraftForge.EVENT_BUS.register(new SharedEvents());
		MinecraftForge.EVENT_BUS.register(new ServerEvents());
		
		MinersAdvantage.instance.network.registerMessage(PacketBase.ServerHandler.class, PacketBase.class, 0, Side.SERVER);
	}
}
