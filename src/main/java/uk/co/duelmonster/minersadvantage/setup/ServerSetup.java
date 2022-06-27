package uk.co.duelmonster.minersadvantage.setup;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import uk.co.duelmonster.minersadvantage.MA;

public class ServerSetup {

  public ServerSetup() {
    FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onServerStarting);

    MA.serverSetup = this;
  }

  @SubscribeEvent
  public void onServerStarting(FMLDedicatedServerSetupEvent e) {
    // MinecraftForge.EVENT_BUS.register(new ServerEventHandler());
  }
}