package uk.co.duelmonster.minersadvantage.config.categories;

import java.util.List;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import uk.co.duelmonster.minersadvantage.config.defaults.MAConfig_Defaults;

public class MAConfig_Veination extends MAConfig_BaseCategory {

  // ====================================================================================================
  // = Config variables - !! NOT TO BE USED DIRECTLY !!
  // = Use the retrieval and modification functions below at all times!
  // ====================================================================================================
  private final ConfigValue<List<? extends String>> ores;

  // ====================================================================================================
  // = Initialisation
  // ====================================================================================================
  public MAConfig_Veination(ForgeConfigSpec.Builder builder) {

    enabled = builder
        .comment("Enable/Disable Veination")
        .translation("minersadvantage.veination.enabled")
        .define("enabled", MAConfig_Defaults.Veination.enabled);

    ores = builder
        .comment("List of Ore Block IDs.")
        .translation("minersadvantage.veination.ores")
        .defineList("ores", MAConfig_Defaults.Veination.ores, obj -> obj instanceof String);
  }

  // ====================================================================================================
  // = Config retrieval functions
  // ====================================================================================================

  /**
   * @return Veination Ores
   */
  public List<? extends String> ores() {
    if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceVeinationSettings.get())
      return parentConfig.serverOverrides.veination.ores();

    return ores.get();
  }

  /**
   * @param ores Sets Veination ores
   */
  public void setOres(List<? extends String> value) {
    ores.set(value);
  }

}
