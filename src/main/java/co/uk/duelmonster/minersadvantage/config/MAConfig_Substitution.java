package co.uk.duelmonster.minersadvantage.config;

// Substitution Settings
public class MAConfig_Substitution extends MAConfig_SubCategory {
	
	public MAConfig_Substitution(MAConfig _parentConfig) {
		super(_parentConfig);
	}
	
	// ====================================================================================================
	// = Config variables - !! NOT TO BE USED DIRECTLY !! - Use the retrieval functions below at all times!
	// ====================================================================================================
	
	private boolean	_bSwitchBack		= true;
	private boolean	_bFavourSilkTouch	= false;
	private boolean	_bFavourFortune		= true;
	private boolean	_bIgnoreIfValidTool	= true;
	private boolean	_bIgnorePassiveMobs	= true;
	
	// ====================================================================================================
	// = Config retrieval functions
	// ====================================================================================================
	
	/**
	 * @return bEnabled
	 */
	@Override
	public boolean bEnabled() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bOverrideFeatureEnablement && !parentConfig.serverOverrides.substitution.bEnabled())
			return false;
		
		return super.bEnabled();
	}
	
	/**
	 * @return bSwitchBack
	 */
	public boolean bSwitchBack() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceSubstitutionSettings)
			return parentConfig.serverOverrides.substitution.bSwitchBack();
		
		return _bSwitchBack;
	}
	
	/**
	 * @param _bSwitchBack
	 *            Sets bSwitchBack
	 */
	public void setSwitchBack(boolean _bSwitchBack) {
		this._bSwitchBack = _bSwitchBack;
	}
	
	/**
	 * @return bFavourSilkTouch
	 */
	public boolean bFavourSilkTouch() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceSubstitutionSettings)
			return parentConfig.serverOverrides.substitution.bFavourSilkTouch();
		
		return _bFavourSilkTouch;
	}
	
	/**
	 * @param _bFavourSilkTouch
	 *            Sets bFavourSilkTouch
	 */
	public void setFavourSilkTouch(boolean _bFavourSilkTouch) {
		this._bFavourSilkTouch = _bFavourSilkTouch;
	}
	
	/**
	 * @return bFavourFortune
	 */
	public boolean bFavourFortune() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceSubstitutionSettings)
			return parentConfig.serverOverrides.substitution.bFavourFortune();
		
		return _bFavourFortune;
	}
	
	/**
	 * @param _bFavourFortune
	 *            Sets bFavourFortune
	 */
	public void setFavourFortune(boolean _bFavourFortune) {
		this._bFavourFortune = _bFavourFortune;
	}
	
	/**
	 * @return bIgnoreIfValidTool
	 */
	public boolean bIgnoreIfValidTool() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceSubstitutionSettings)
			return parentConfig.serverOverrides.substitution.bIgnoreIfValidTool();
		
		return _bIgnoreIfValidTool;
	}
	
	/**
	 * @param _bIgnoreIfValidTool
	 *            Sets bIgnoreIfValidTool
	 */
	public void setIgnoreIfValidTool(boolean _bIgnoreIfValidTool) {
		this._bIgnoreIfValidTool = _bIgnoreIfValidTool;
	}
	
	/**
	 * @return bIgnorePassiveMobs
	 */
	public boolean bIgnorePassiveMobs() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceSubstitutionSettings)
			return parentConfig.serverOverrides.substitution.bIgnorePassiveMobs();
		
		return _bIgnorePassiveMobs;
	}
	
	/**
	 * @param _bIgnorePassiveMobs
	 *            Sets bIgnorePassiveMobs
	 */
	public void setIgnorePassiveMobs(boolean _bIgnorePassiveMobs) {
		this._bIgnorePassiveMobs = _bIgnorePassiveMobs;
	}
	
}
