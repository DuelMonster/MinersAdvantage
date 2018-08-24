package co.uk.duelmonster.minersadvantage.config;

// Pathanation Settings
public class MAConfig_Pathanation extends MAConfig_SubCategory {
	
	public MAConfig_Pathanation(MAConfig _parentConfig) {
		super(_parentConfig);
	}
	
	// ====================================================================================================
	// = Config variables - !! NOT TO BE USED DIRECTLY !! - Use the retrieval functions below at all times!
	// ====================================================================================================
	
	public int	_iPathWidth		= 3;
	public int	_iPathLength	= 6;
	
	// ====================================================================================================
	// = Config retrieval functions
	// ====================================================================================================
	
	/**
	 * @return bEnabled
	 */
	@Override
	public boolean bEnabled() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bOverrideFeatureEnablement && !parentConfig.serverOverrides.pathanation.bEnabled())
			return false;
		
		return super.bEnabled();
	}
	
	/**
	 * @return iPathWidth
	 */
	public int iPathWidth() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceExcavationSettings)
			return parentConfig.serverOverrides.pathanation.iPathWidth();
		
		return _iPathWidth;
	}
	
	/**
	 * @param _iPathWidth
	 *            Sets iPathWidth
	 */
	public void setPathWidth(int _iPathWidth) {
		this._iPathWidth = _iPathWidth;
	}
	
	/**
	 * @return iPathLength
	 */
	public int iPathLength() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceExcavationSettings)
			return parentConfig.serverOverrides.pathanation.iPathLength();
		
		return _iPathLength;
	}
	
	/**
	 * @param _iPathLength
	 *            Sets iPathLength
	 */
	public void setPathLength(int _iPathLength) {
		this._iPathLength = _iPathLength;
	}
	
}
