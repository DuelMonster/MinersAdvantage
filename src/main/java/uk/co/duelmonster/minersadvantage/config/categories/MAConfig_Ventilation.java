package uk.co.duelmonster.minersadvantage.config.categories;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import uk.co.duelmonster.minersadvantage.config.defaults.MAConfig_Defaults;

public class MAConfig_Ventilation extends MAConfig_BaseCategory {

  // ====================================================================================================
  // = Config variables - !! NOT TO BE USED DIRECTLY !!
  // = Use the retrieval and modification functions below at all times!
  // ====================================================================================================
  private final IntValue     ventDiameter;
  private final IntValue     ventDepth;
  private final BooleanValue placeLadders;

  // ====================================================================================================
  // = Initialisation
  // ====================================================================================================
  public MAConfig_Ventilation(ForgeConfigSpec.Builder builder) {

    enabled = builder
        .comment("Enable/Disable Ventilation")
        .translation("minersadvantage.ventilation.enabled")
        .define("enabled", MAConfig_Defaults.Ventilation.enabled);

    ventDiameter = builder
        .comment("The number of blocks square the vent should be.")
        .translation("minersadvantage.ventilation.vent_diameter")
        .defineInRange("vent_diameter", MAConfig_Defaults.Ventilation.ventDiameter, 1, 5);

    ventDepth = builder
        .comment("The number of blocks deep the vent should be.")
        .translation("minersadvantage.ventilation.vent_depth")
        .defineInRange("vent_depth", MAConfig_Defaults.Ventilation.ventDepth, 1, 256);

    placeLadders = builder
        .comment("Place Ladders on one wall of the vent, from top to bottom, if Ladders are in the inventory.")
        .translation("minersadvantage.ventilation.place_ladders")
        .define("place_ladders", MAConfig_Defaults.Ventilation.placeLadders);
  }

  // ====================================================================================================
  // = Config retrieval functions
  // ====================================================================================================

  /**
   * @return ventDiameter
   */
  public int ventDiameter() {
    if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceVentilationSettings.get())
      return parentConfig.serverOverrides.ventilation.ventDiameter();

    return ventDiameter.get();
  }

  /**
   * @param ventDiameter
   *        Sets ventDiameter
   */
  public void setVentDiameter(int value) {
    ventDiameter.set(value);
  }

  /**
   * @return ventDepth
   */
  public int ventDepth() {
    if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceVentilationSettings.get())
      return parentConfig.serverOverrides.ventilation.ventDepth();

    return ventDepth.get();
  }

  /**
   * @param ventDepth
   *        Sets ventDepth
   */
  public void setVentDepth(int value) {
    ventDepth.set(value);
  }

  /**
   * @return placeLadders
   */
  public boolean placeLadders() {
    if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceVentilationSettings.get())
      return parentConfig.serverOverrides.ventilation.placeLadders();

    return placeLadders.get();
  }

  /**
   * @param placeLadders
   *        Sets placeLadders
   */
  public void setPlaceLadders(boolean value) {
    placeLadders.set(value);
  }

}
