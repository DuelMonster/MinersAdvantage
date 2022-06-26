package uk.co.duelmonster.minersadvantage.config.categories;

import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import uk.co.duelmonster.minersadvantage.config.MAConfig_Base;

public class MAConfig_BaseCategory {

  // ====================================================================================================
  // = Config variables - !! NOT TO BE USED DIRECTLY !!
  // = Use the retrieval and modification functions below at all times!
  // ====================================================================================================
  protected BooleanValue enabled;

  // ====================================================================================================
  // = Non-config variables
  // ====================================================================================================
  protected transient MAConfig_Base parentConfig = null;

  public void SetParentConfig(MAConfig_Base _parentConfig) {
    parentConfig = _parentConfig;
  }

  // ====================================================================================================
  // = Config retrieval functions
  // ====================================================================================================

  /**
   * @return bEnabled
   */
  public boolean enabled() {
    return enabled.get();
  }

  /**
   * @param _bEnabled Sets bEnabled
   */
  public void setEnabled(boolean p_bEnabled) {
    if (parentConfig == null || parentConfig.serverOverrides == null || !parentConfig.serverOverrides.overrideFeatureEnablement.get())
      enabled.set(p_bEnabled);
  }

}
