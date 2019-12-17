package uk.co.duelmonster.minersadvantage.setup;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import uk.co.duelmonster.minersadvantage.client.ClientFunctions;
import uk.co.duelmonster.minersadvantage.client.KeyBindings;
import uk.co.duelmonster.minersadvantage.client.MAParticleManager;
import uk.co.duelmonster.minersadvantage.config.MAConfig;
import uk.co.duelmonster.minersadvantage.events.client.ClientEventHandler;
import uk.co.duelmonster.minersadvantage.events.client.KeyInputEvents;

public class ClientSetup {
	
	public ClientSetup() {
		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
	}
	
	@SubscribeEvent
	public void onClientSetup(FMLClientSetupEvent e) {
		Minecraft mc = Minecraft.getInstance();
		ClientFunctions.mc = mc;
		
		if (MAParticleManager.getOriginal() == null)
			MAParticleManager.setOriginal(mc.particles);
		
		if (MAConfig.CLIENT.disableParticleEffects.get()) {
			mc.particles = MAParticleManager.set(new MAParticleManager(mc.world, mc.getTextureManager()));
			((IReloadableResourceManager) mc.getResourceManager()).addReloadListener(mc.particles);
		}
		
		// KeyBindings
		KeyBindings.registerKeys();
		MinecraftForge.EVENT_BUS.register(new KeyInputEvents());
		
		MinecraftForge.EVENT_BUS.register(new ClientEventHandler());
		
		// ScreenManager.registerFactory(RSContainers.CONTROLLER, ControllerScreen::new);
	}
}
