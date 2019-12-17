package uk.co.duelmonster.minersadvantage.setup;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import uk.co.duelmonster.minersadvantage.MA;

public class CommonSetup {
	@SubscribeEvent
	public void onCommonSetup(FMLCommonSetupEvent e) {
		MA.NETWORK.registerPackets();
	}
}
