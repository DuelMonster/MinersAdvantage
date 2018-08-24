package co.uk.duelmonster.minersadvantage.config;

// Excavation Settings
public class MAConfig_Excavation extends MAConfig_SubCategory {
	
	public MAConfig_Excavation(MAConfig _parentConfig) {
		super(_parentConfig);
	}
	
	// ====================================================================================================
	// = Config variables - !! NOT TO BE USED DIRECTLY !! - Use the retrieval functions below at all times!
	// ====================================================================================================
	
	private boolean		_bToggleMode			= false;
	private boolean		_bIgnoreBlockVariants	= false;
	private boolean		_bIsToolWhitelist		= false;
	private String[]	_toolBlacklist			= {};
	private boolean		_bIsBlockWhitelist		= false;
	private String[]	_blockBlacklist			= {};
	
	// ====================================================================================================
	// = Config retrieval functions
	// ====================================================================================================
	
	/**
	 * @return bEnabled
	 */
	@Override
	public boolean bEnabled() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bOverrideFeatureEnablement && !parentConfig.serverOverrides.excavation.bEnabled())
			return false;
		
		return super.bEnabled();
	}
	
	/**
	 * @return bIgnoreBlockVariants
	 */
	public boolean bIgnoreBlockVariants() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceExcavationSettings)
			return parentConfig.serverOverrides.excavation.bIgnoreBlockVariants();
		
		return _bIgnoreBlockVariants;
	}
	
	/**
	 * @param _bIgnoreBlockVariants
	 *            Sets bIgnoreBlockVariants
	 */
	public void setIgnoreBlockVariants(boolean _bIgnoreBlockVariants) {
		this._bIgnoreBlockVariants = _bIgnoreBlockVariants;
	}
	
	/**
	 * @return bToggleMode
	 */
	public boolean bToggleMode() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceExcavationSettings)
			return parentConfig.serverOverrides.excavation.bToggleMode();
		
		return _bToggleMode;
	}
	
	/**
	 * @param _bToggleMode
	 *            Sets bToggleMode
	 */
	public void setToggleMode(boolean _bToggleMode) {
		this._bToggleMode = _bToggleMode;
	}
	
	/**
	 * @return bIsToolWhitelist
	 */
	public boolean bIsToolWhitelist() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceExcavationSettings)
			return parentConfig.serverOverrides.excavation.bIsToolWhitelist();
		
		return _bIsToolWhitelist;
	}
	
	/**
	 * @param _bIsToolWhitelist
	 *            Sets bIsToolWhitelist
	 */
	public void setIsToolWhitelist(boolean _bIsToolWhitelist) {
		this._bIsToolWhitelist = _bIsToolWhitelist;
	}
	
	/**
	 * @return toolBlacklist
	 */
	public String[] toolBlacklist() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceExcavationSettings)
			return parentConfig.serverOverrides.excavation.toolBlacklist();
		
		return _toolBlacklist;
	}
	
	/**
	 * @param _toolBlacklist
	 *            Sets toolBlacklist
	 */
	public void setToolBlacklist(String[] _toolBlacklist) {
		this._toolBlacklist = _toolBlacklist;
	}
	
	/**
	 * @return bIsBlockWhitelist
	 */
	public boolean bIsBlockWhitelist() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceExcavationSettings)
			return parentConfig.serverOverrides.excavation.bIsBlockWhitelist();
		
		return _bIsBlockWhitelist;
	}
	
	/**
	 * @param _bIsBlockWhitelist
	 *            Sets bIsBlockWhitelist
	 */
	public void setIsBlockWhitelist(boolean _bIsBlockWhitelist) {
		this._bIsBlockWhitelist = _bIsBlockWhitelist;
	}
	
	/**
	 * @return blockBlacklist
	 */
	public String[] blockBlacklist() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceExcavationSettings)
			return parentConfig.serverOverrides.excavation.blockBlacklist();
		
		return _blockBlacklist;
	}
	
	/**
	 * @param _blockBlacklist
	 *            Sets blockBlacklist
	 */
	public void setBlockBlacklist(String[] _blockBlacklist) {
		this._blockBlacklist = _blockBlacklist;
	}
	
}
