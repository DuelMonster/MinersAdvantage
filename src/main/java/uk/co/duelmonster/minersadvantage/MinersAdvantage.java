package uk.co.duelmonster.minersadvantage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import uk.co.duelmonster.minersadvantage.client.ClientFunctions;
import uk.co.duelmonster.minersadvantage.client.KeyBindings;
import uk.co.duelmonster.minersadvantage.client.MAParticleManager;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.config.MAConfig;
import uk.co.duelmonster.minersadvantage.network.NetworkHandler;

@Mod(Constants.MOD_ID)
public class MinersAdvantage {

	public static MinersAdvantage INSTANCE;
	// Directly reference a log4j logger.
	public static final Logger LOGGER = LogManager.getLogger();
	final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

	public MinersAdvantage() {
		INSTANCE = this;

		// Register ourselves for game events we are interested in
		modEventBus.addListener(this::onCommonSetup);
		modEventBus.addListener(this::onClientSetup);
        MinecraftForge.EVENT_BUS.addListener(this::onServerSetup);
		//modEventBus.register(this);
		//MinecraftForge.EVENT_BUS.register(this);

		// Register our configuration classes and config event listener
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, MAConfig.clientSpec);
		ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, MAConfig.serverSpec);
		//modEventBus.register(MAConfig.class);
		MinecraftForge.EVENT_BUS.register(MAConfig.class);
	}

	//@SubscribeEvent
	// Common Setup event - Both Server & Client
	public void onCommonSetup(final FMLCommonSetupEvent event) {
		// Ensure the PacketHandler is initialised
		DeferredWorkQueue.runLater(NetworkHandler::init);
	}

	//@SubscribeEvent
	// Client Setup event - Client side only
	public void onClientSetup(final FMLClientSetupEvent event) {
		Minecraft mc = event.getMinecraftSupplier().get();
		ClientFunctions.mc = mc;

		if (MAParticleManager.getOriginal() == null)
			MAParticleManager.setOriginal(mc.particles);

		if (MAConfig.CLIENT.disableParticleEffects.get()) {
			mc.particles = MAParticleManager.set(new MAParticleManager(mc.world, mc.getTextureManager()));
			((IReloadableResourceManager)mc.getResourceManager()).addReloadListener(mc.particles);
		}

		// KeyBindings
		KeyBindings.registerKeys();
		//DeferredWorkQueue.runLater(KeyBindings::registerKeys);
	}

	//@SubscribeEvent
	// Dedicated Server Setup event - Server side only
	public void onServerSetup(final FMLDedicatedServerSetupEvent event) {

	}

//	@SubscribeEvent
//	// Fingerprint Violation event
//	public void onFingerprintViolation(FMLFingerprintViolationEvent event) {}
//	
//	@SubscribeEvent
//	// Enqueue messages to other mods
//    private void enqueueIMC(final InterModEnqueueEvent event) {}
//
//	@SubscribeEvent
//	// Process messages from other mods
//	private void processIMC(final InterModProcessEvent event) {}

}
