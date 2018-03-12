package co.uk.duelmonster.minersadvantage.proxies;

import java.io.File;

import co.uk.duelmonster.minersadvantage.MinersAdvantage;
import co.uk.duelmonster.minersadvantage.events.ServerEvents;
import co.uk.duelmonster.minersadvantage.events.SharedEvents;
import co.uk.duelmonster.minersadvantage.packets.NetworkPacket;
import co.uk.duelmonster.minersadvantage.settings.ConfigHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy {
	
	public boolean isClient() {
		return false;
	}
	
	public void initConfig(File configFile) {
		ConfigHandler.initConfigs(new Configuration(configFile, true));
	}
	
	public void registerHandlers() {
		MinecraftForge.EVENT_BUS.register(new SharedEvents());
		MinecraftForge.EVENT_BUS.register(new ServerEvents());
		
		MinersAdvantage.instance.network.registerMessage(NetworkPacket.ServerHandler.class, NetworkPacket.class, 0, Side.SERVER);
	}
}
