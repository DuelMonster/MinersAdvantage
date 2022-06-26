package uk.co.duelmonster.minersadvantage.config;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;

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

}
