package uk.co.duelmonster.minersadvantage.setup;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import uk.co.duelmonster.minersadvantage.MA;
import uk.co.duelmonster.minersadvantage.events.server.ServerEventHandler;

public class CommonSetup {

  public CommonSetup() {
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);

    MA.commonSetup = this;
  }

  @SubscribeEvent
  public void onCommonSetup(FMLCommonSetupEvent e) {
    MA.NETWORK.registerPackets();
    MinecraftForge.EVENT_BUS.register(new ServerEventHandler());
  }
}
