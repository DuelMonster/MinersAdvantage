package uk.co.duelmonster.minersadvantage.config.categories;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.config.defaults.MAConfig_Defaults;

public class MAConfig_Common extends MAConfig_BaseCategory {

  // ====================================================================================================
  // = Config variables - !! NOT TO BE USED DIRECTLY !!
  // = Use the retrieval and modification functions below at all times!
  // ====================================================================================================
  private final BooleanValue tpsGuard;
  private final BooleanValue gatherDrops;
  private final BooleanValue autoIlluminate;
  private final BooleanValue mineVeins;
  private final IntValue     blocksPerTick;
  private final BooleanValue enableTickDelay;
  private final IntValue     tickDelay;
  private final IntValue     blockRadius;
  private final IntValue     blockLimit;

  // ====================================================================================================
  // = Initialisation
  // ====================================================================================================
  public MAConfig_Common(ForgeConfigSpec.Builder builder) {

    tpsGuard = builder
        .comment("The TPS (Ticks per Second) Guard can be used to help reduce block ghosting [Default = true]")
        .translation("minersadvantage.common.tps_guard")
        .define("tps_guard", MAConfig_Defaults.Common.tpsGuard);

    gatherDrops = builder
        .comment("Gathers all dropped items to the position of the first block hit.")
        .translation("minersadvantage.common.gather_drops")
        .define("gather_drops", MAConfig_Defaults.Common.gatherDrops);

    autoIlluminate = builder
        .comment("Automatically Illuminate dark areas, if the Illumination is enabled, while using Excavation, Shaftanation or Ventilation.")
        .translation("minersadvantage.common.auto_illum")
        .define("auto_illum", MAConfig_Defaults.Common.autoIlluminate);

    mineVeins = builder
        .comment("Automatically mine a vein of ore, if the Veination mod is enabled, while using Excavation, Shaftanation or Ventilation.")
        .translation("minersadvantage.common.mine_veins")
        .define("mine_veins", MAConfig_Defaults.Common.mineVeins);

    blocksPerTick = builder
        .comment("The number of blocks that will be broken per tick. Default is 1, but the faster it is, the more likely that Minecraft will crash due to being unable to keep up.")
        .translation("minersadvantage.common.blocks_per_tick")
        .defineInRange("blocks_per_tick", MAConfig_Defaults.Common.blocksPerTick, 1, 16);

    enableTickDelay = builder
        .comment("Enable/Disable the Blocks per Tick delay.")
        .translation("minersadvantage.common.enable_tick_delay")
        .define("enable_tick_delay", MAConfig_Defaults.Common.enableTickDelay);

    tickDelay = builder
        .comment("The number of Ticks that will be ignored before the next block is harvested.")
        .translation("minersadvantage.common.tick_delay")
        .defineInRange("tick_delay", MAConfig_Defaults.Common.tickDelay, 1, 20);

    blockRadius = builder
        .comment("The Radius of blocks to check for connected blocks.")
        .translation("minersadvantage.common.radius")
        .defineInRange("radius", MAConfig_Defaults.Common.blockRadius, Constants.MIN_BLOCKRADIUS, Constants.MAX_BLOCKRADIUS);

    blockLimit = builder
        .comment("The Maximum number of blocks allowed to be mined.")
        .translation("minersadvantage.common.limit")
        .defineInRange("limit", MAConfig_Defaults.Common.blockLimit, Constants.MIN_BLOCKLIMIT, Constants.MAX_BLOCKLIMIT);

  }

  // ====================================================================================================
  // = Config retrieval functions
  // ====================================================================================================

  /**
   * @return tpsGuard
   */
  public boolean tpsGuard() {
    if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceCommonSettings.get())
      return parentConfig.serverOverrides.common.tpsGuard();

    return tpsGuard.get();
  }

  /**
   * @param tpsGuard Sets tpsGuard
   */
  public void set_tpsGuard(boolean value) {
    tpsGuard.set(value);
  }

  /**
   * @return gatherDrops
   */
  public boolean gatherDrops() {
    if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceCommonSettings.get())
      return parentConfig.serverOverrides.common.gatherDrops();

    return gatherDrops.get();
  }

  /**
   * @param gatherDrops Sets gatherDrops
   */
  public void setGatherDrops(boolean value) {
    gatherDrops.set(value);
  }

  /**
   * @return autoIlluminate
   */
  public boolean autoIlluminate() {
    if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceCommonSettings.get())
      return parentConfig.serverOverrides.common.autoIlluminate();

    return autoIlluminate.get();
  }

  /**
   * @param autoIlluminate Sets autoIlluminate
   */
  public void setAutoIlluminate(boolean value) {
    autoIlluminate.set(value);
  }

  /**
   * @return mineVeins
   */
  public boolean mineVeins() {
    if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceCommonSettings.get())
      return parentConfig.serverOverrides.common.mineVeins();

    return mineVeins.get();
  }

  /**
   * @param mineVeins Sets mineVeins
   */
  public void setMineVeins(boolean value) {
    mineVeins.set(value);
  }

  /**
   * @return blocksPerTick
   */
  public int blocksPerTick() {
    if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceCommonSettings.get())
      return parentConfig.serverOverrides.common.blocksPerTick();

    return blocksPerTick.get();
  }

  /**
   * @param blocksPerTick Sets blocksPerTick
   */
  public void setBlocksPerTick(int value) {
    blocksPerTick.set(value);
  }

  /**
   * @return enableTickDelay
   */
  public boolean enableTickDelay() {
    if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceCommonSettings.get())
      return parentConfig.serverOverrides.common.enableTickDelay();

    return enableTickDelay.get();
  }

  /**
   * @param enableTickDelay Sets enableTickDelay
   */
  public void setEnableTickDelay(boolean value) {
    enableTickDelay.set(value);
  }

  /**
   * @return tickDelay
   */
  public int tickDelay() {
    if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceCommonSettings.get())
      return parentConfig.serverOverrides.common.tickDelay();

    return tickDelay.get();
  }

  /**
   * @param tickDelay Sets tickDelay
   */
  public void setTickDelay(int value) {
    tickDelay.set(value);
  }

  /**
   * @return blockRadius
   */
  public int blockRadius() {
    if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceCommonSettings.get())
      return parentConfig.serverOverrides.common.blockRadius();

    return blockRadius.get();
  }

  /**
   * @param blockRadius Sets blockRadius
   */
  public void setBlockRadius(int value) {
    blockRadius.set(value);
  }

  /**
   * @return blockLimit
   */
  public int blockLimit() {
    if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceCommonSettings.get())
      return parentConfig.serverOverrides.common.blockLimit();

    return blockLimit.get();
  }

  /**
   * @param blockLimit Sets blockLimit
   */
  public void setBlockLimit(int value) {
    blockLimit.set(value);
  }

}
