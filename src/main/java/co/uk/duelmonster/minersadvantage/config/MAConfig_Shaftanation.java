package co.uk.duelmonster.minersadvantage.config;

// Shaftanation Settings
public class MAConfig_Shaftanation extends MAConfig_SubCategory {
	
	public MAConfig_Shaftanation(MAConfig _parentConfig) {
		super(_parentConfig);
	}
	
	// ====================================================================================================
	// = Config variables - !! NOT TO BE USED DIRECTLY !! - Use the retrieval functions below at all times!
	// ====================================================================================================
	
	private int	_iShaftLength	= 16;
	private int	_iShaftHeight	= 2;
	private int	_iShaftWidth	= 1;
	
	// ====================================================================================================
	// = Config retrieval functions
	// ====================================================================================================
	
	/**
	 * @return bEnabled
	 */
	@Override
	public boolean bEnabled() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bOverrideFeatureEnablement && !parentConfig.serverOverrides.shaftanation.bEnabled())
			return false;
		
		return super.bEnabled();
	}
	
	/**
	 * @return iShaftLength
	 */
	public int iShaftLength() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceShaftanationSettings)
			return parentConfig.serverOverrides.shaftanation.iShaftLength();
		
		return _iShaftLength;
	}
	
	/**
	 * @param _iShaftLength
	 *            Sets iShaftLength
	 */
	public void setShaftLength(int _iShaftLength) {
		this._iShaftLength = _iShaftLength;
	}
	
	/**
	 * @return iShaftHeight
	 */
	public int iShaftHeight() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceShaftanationSettings)
			return parentConfig.serverOverrides.shaftanation.iShaftHeight();
		
		return _iShaftHeight;
	}
	
	/**
	 * @param _iShaftHeight
	 *            Sets iShaftHeight
	 */
	public void setShaftHeight(int _iShaftHeight) {
		this._iShaftHeight = _iShaftHeight;
	}
	
	/**
	 * @return iShaftWidth
	 */
	public int iShaftWidth() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceShaftanationSettings)
			return parentConfig.serverOverrides.shaftanation.iShaftWidth();
		
		return _iShaftWidth;
	}
	
	/**
	 * @param _iShaftWidth
	 *            Sets iShaftWidth
	 */
	public void setShaftWidth(int _iShaftWidth) {
		this._iShaftWidth = _iShaftWidth;
	}
	
}
