package co.uk.duelmonster.minersadvantage.proxies;

import java.io.File;

import co.uk.duelmonster.minersadvantage.MinersAdvantage;
import co.uk.duelmonster.minersadvantage.client.KeyBindings;
import co.uk.duelmonster.minersadvantage.events.ClientEvents;
import co.uk.duelmonster.minersadvantage.events.KeyInputEvents;
import co.uk.duelmonster.minersadvantage.packets.NetworkPacket;
import co.uk.duelmonster.minersadvantage.settings.ConfigHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
	
	@Override
	public boolean isClient() {
		return true;
	}
	
	@Override
	public void initConfig(File configFile) {
		ConfigHandler.initConfigs(new Configuration(configFile, true));
	}
	
	@Override
	public void registerHandlers() {
		super.registerHandlers();
		
		MinecraftForge.EVENT_BUS.register(new ClientEvents());
		MinecraftForge.EVENT_BUS.register(new KeyInputEvents());
		
		KeyBindings.registerKeys();
		
		MinersAdvantage.instance.network.registerMessage(NetworkPacket.ClientHandler.class, NetworkPacket.class, 0, Side.CLIENT);
	}
	
}
