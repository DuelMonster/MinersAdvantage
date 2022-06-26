package uk.co.duelmonster.minersadvantage.config;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.config.ModConfig.Reloading;
import uk.co.duelmonster.minersadvantage.common.Constants;

public class MAConfig {

  public static final ForgeConfigSpec clientSpec;
  public static final MAConfig_Client CLIENT;
  static {
    final Pair<MAConfig_Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(MAConfig_Client::new);
    clientSpec = specPair.getRight();
    CLIENT     = specPair.getLeft();
  }

  public static final ForgeConfigSpec serverSpec;
  public static final MAConfig_Server SERVER;
  static {
    final Pair<MAConfig_Server, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(MAConfig_Server::new);
    serverSpec = specPair.getRight();
    SERVER     = specPair.getLeft();
  }

  @SubscribeEvent
  public static void onLoad(final ModConfig.Loading configEvent) {
    Constants.LOGGER.debug(Constants.MOD_ID, "Loaded config file {}", configEvent.getConfig().getFileName());
  }

  @SubscribeEvent
  public static void onFileChange(final Reloading configEvent) {
    Constants.LOGGER.fatal(Constants.MOD_ID, "Config just got changed on the file system!");
  }

}
