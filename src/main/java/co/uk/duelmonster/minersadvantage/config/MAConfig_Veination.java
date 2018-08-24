package co.uk.duelmonster.minersadvantage.config;

// Veination Settings
public class MAConfig_Veination extends MAConfig_SubCategory {
	
	public MAConfig_Veination(MAConfig _parentConfig) {
		super(_parentConfig);
	}
	
	// ====================================================================================================
	// = Config variables - !! NOT TO BE USED DIRECTLY !! - Use the retrieval functions below at all times!
	// ====================================================================================================
	
	public String[] _Ores = {};
	
	// ====================================================================================================
	// = Config retrieval functions
	// ====================================================================================================
	
	/**
	 * @return bEnabled
	 */
	@Override
	public boolean bEnabled() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bOverrideFeatureEnablement && !parentConfig.serverOverrides.veination.bEnabled())
			return false;
		
		return super.bEnabled();
	}
	
	/**
	 * @return Veination Ores
	 */
	public String[] ores() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceVeinationSettings)
			return parentConfig.serverOverrides.veination.ores();
		
		return _Ores;
	}
	
	/**
	 * @param _Ores
	 *            Sets Veination Ores
	 */
	public void setOres(String[] _Ores) {
		this._Ores = _Ores;
	}
	
}
