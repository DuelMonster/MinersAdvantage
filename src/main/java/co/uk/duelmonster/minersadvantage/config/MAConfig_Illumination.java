package co.uk.duelmonster.minersadvantage.config;

// Illumination Settings
public class MAConfig_Illumination extends MAConfig_SubCategory {
	
	public MAConfig_Illumination(MAConfig _parentConfig) {
		super(_parentConfig);
	}
	
	// ====================================================================================================
	// = Config variables - !! NOT TO BE USED DIRECTLY !! - Use the retrieval functions below at all times!
	// ====================================================================================================
	
	private boolean	_bUseBlockLight		= true;
	private int		_iLowestLightLevel	= 7;
	
	// ====================================================================================================
	// = Config retrieval functions
	// ====================================================================================================
	
	/**
	 * @return bEnabled
	 */
	@Override
	public boolean bEnabled() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bOverrideFeatureEnablement && !parentConfig.serverOverrides.illumination.bEnabled())
			return false;
		
		return super.bEnabled();
	}
	
	/**
	 * @return bUseBlockLight
	 */
	public boolean bUseBlockLight() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceIlluminationSettings)
			return parentConfig.serverOverrides.illumination.bUseBlockLight();
		
		return _bUseBlockLight;
	}
	
	/**
	 * @param _bUseBlockLight
	 *            Sets bUseBlockLight
	 */
	public void setUseBlockLight(boolean _bUseBlockLight) {
		if (parentConfig == null || parentConfig.serverOverrides == null || !parentConfig.serverOverrides.server.bEnforceIlluminationSettings)
			this._bUseBlockLight = _bUseBlockLight;
	}
	
	/**
	 * @return iLowestLightLevel
	 */
	public int iLowestLightLevel() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceIlluminationSettings)
			return parentConfig.serverOverrides.illumination.iLowestLightLevel();
		
		return _iLowestLightLevel;
	}
	
	/**
	 * @param _iLowestLightLevel
	 *            Sets iLowestLightLevel
	 */
	public void setLowestLightLevel(int _iLowestLightLevel) {
		this._iLowestLightLevel = _iLowestLightLevel;
	}
	
}
