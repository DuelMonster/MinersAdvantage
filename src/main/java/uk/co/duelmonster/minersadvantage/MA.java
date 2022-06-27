package uk.co.duelmonster.minersadvantage;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.config.MAConfig;
import uk.co.duelmonster.minersadvantage.network.NetworkHandler;
import uk.co.duelmonster.minersadvantage.setup.ClientSetup;
import uk.co.duelmonster.minersadvantage.setup.CommonSetup;
import uk.co.duelmonster.minersadvantage.setup.ServerSetup;

@Mod(Constants.MOD_ID)
public class MA {

  public static ClientSetup          clientSetup = null;
  public static CommonSetup          commonSetup = null;
  public static ServerSetup          serverSetup = null;
  public static final NetworkHandler NETWORK     = new NetworkHandler();

  public MA() {
    DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientSetup::new);

    commonSetup = new CommonSetup();
    serverSetup = new ServerSetup();

    // Register our configuration classes and config event listener
    ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, MAConfig.clientSpec);
    ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, MAConfig.serverSpec);
  }
}
