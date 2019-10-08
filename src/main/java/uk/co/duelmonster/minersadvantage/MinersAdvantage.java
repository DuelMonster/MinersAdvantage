package uk.co.duelmonster.minersadvantage;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.config.MAConfig;
import uk.co.duelmonster.minersadvantage.events.client.ClientEventHandler;
import uk.co.duelmonster.minersadvantage.events.server.ServerEventHandler;
import uk.co.duelmonster.minersadvantage.network.NetworkHandler;

@Mod(Constants.MOD_ID)
public class MinersAdvantage {
	
	public static MinersAdvantage INSTANCE;
	
	public static ServerEventHandler proxy = DistExecutor.runForDist(() -> () -> new ClientEventHandler(), () -> () -> new ServerEventHandler());
	
	public MinersAdvantage() {
		INSTANCE = this;
		
		// Register the onCommonSetup method for modloading
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
		
		// Register our configuration classes and config event listener
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, MAConfig.clientSpec);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, MAConfig.serverSpec);
		
		MinecraftForge.EVENT_BUS.register(MAConfig.class);
	}
	
	// @SubscribeEvent
	// Common Setup event - Both Server & Client
	public void onCommonSetup(final FMLCommonSetupEvent event) {
		// Ensure the PacketHandler is initialised
		NetworkHandler.init();
	}
	
	// // @SubscribeEvent
	// // Dedicated Server Setup event - Server side only
	// public void onServerSetup(final FMLDedicatedServerSetupEvent event) {
	//
	// }
	//
	// @SubscribeEvent
	// // Fingerprint Violation event
	// public void onFingerprintViolation(FMLFingerprintViolationEvent event) {}
	//
	// @SubscribeEvent
	// // Enqueue messages to other mods
	// private void enqueueIMC(final InterModEnqueueEvent event) {}
	//
	// @SubscribeEvent
	// // Process messages from other mods
	// private void processIMC(final InterModProcessEvent event) {}
	
}
