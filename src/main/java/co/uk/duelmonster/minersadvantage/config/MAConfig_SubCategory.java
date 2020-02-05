package co.uk.duelmonster.minersadvantage.config;

public class MAConfig_SubCategory {
	
	protected transient MAConfig parentConfig = null;
	
	public MAConfig_SubCategory(MAConfig _parentConfig) {
		parentConfig = _parentConfig;
	}
	
	// ====================================================================================================
	// = Config variables - !! NOT TO BE USED DIRECTLY !! - Use the retrieval functions below at all times!
	// ====================================================================================================
	
	protected boolean _bEnabled = true;
	
	// ====================================================================================================
	// = Config retrieval functions
	// ====================================================================================================
	
	/**
	 * @return bEnabled
	 */
	public boolean bEnabled() {
		return _bEnabled;
	}
	
	/**
	 * @param _bEnabled
	 *                  Sets bEnabled
	 */
	public void setEnabled(boolean p_bEnabled) {
		if (parentConfig == null || parentConfig.serverOverrides == null || !parentConfig.serverOverrides.server.bOverrideFeatureEnablement)
			this._bEnabled = p_bEnabled;
	}
	
}
