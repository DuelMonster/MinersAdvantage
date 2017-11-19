package co.uk.duelmonster.minersadvantage;

import org.apache.logging.log4j.Logger;

import co.uk.duelmonster.minersadvantage.client.config.ConfigHandler;
import co.uk.duelmonster.minersadvantage.common.Constants;
import co.uk.duelmonster.minersadvantage.handlers.LumbinationHandler;
import co.uk.duelmonster.minersadvantage.handlers.VeinationHandler;
import co.uk.duelmonster.minersadvantage.proxies.CommonProxy;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

@Mod(	modid = Constants.MOD_ID,
		name = Constants.MOD_NAME,
		version = Constants.MOD_VERSION,
		acceptedMinecraftVersions = Constants.MC_VERSION,
		dependencies = "after:*",
		guiFactory = Constants.GUI_FACTORY,
		updateJSON = Constants.FVC_URL)
public class MinersAdvantage {
	
	@Instance(Constants.MOD_ID)
	public static MinersAdvantage instance;
	
	@SidedProxy(clientSide = Constants.PROXY + ".ClientProxy",
				serverSide = Constants.PROXY + ".CommonProxy")
	public static CommonProxy	proxy;
	public static Logger		logger;
	
	public SimpleNetworkWrapper network;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger = event.getModLog();
		network = NetworkRegistry.INSTANCE.newSimpleChannel(Constants.CHANNEL);
		
		if (proxy.isClient())
			ConfigHandler.initConfigs(new Configuration(event.getSuggestedConfigurationFile(), true));
		
		proxy.registerHandlers();
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		VeinationHandler.getOreList();
		LumbinationHandler.getLumbinationLists();
	}
	
	@EventHandler
	public void onServerStart(FMLServerStartingEvent event) {
		// MinecraftServer server = event.getServer();
		// ((ServerCommandManager) server.getCommandManager()).registerCommand(new CommandUndo());
	}
}
