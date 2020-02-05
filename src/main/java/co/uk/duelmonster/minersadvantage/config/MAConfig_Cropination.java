package co.uk.duelmonster.minersadvantage.config;

// Cropination Settings
public class MAConfig_Cropination extends MAConfig_SubCategory {
	
	public MAConfig_Cropination(MAConfig _parentConfig) {
		super(_parentConfig);
	}
	
	// ====================================================================================================
	// = Config variables - !! NOT TO BE USED DIRECTLY !! - Use the retrieval functions below at all times!
	// ====================================================================================================
	
	private boolean _bHarvestSeeds = true;
	
	// ====================================================================================================
	// = Config retrieval functions
	// ====================================================================================================
	
	/**
	 * @return bEnabled
	 */
	@Override
	public boolean bEnabled() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bOverrideFeatureEnablement && !parentConfig.serverOverrides.cropination.bEnabled())
			return false;
		
		return super.bEnabled();
	}
	
	/**
	 * @return bHarvestSeeds
	 */
	public boolean bHarvestSeeds() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceCropinationSettings)
			return parentConfig.serverOverrides.cropination.bHarvestSeeds();
		
		return _bHarvestSeeds;
	}
	
	/**
	 * @param _bHarvestSeeds
	 *            Sets bHarvestSeeds
	 */
	public void setHarvestSeeds(boolean _bHarvestSeeds) {
		this._bHarvestSeeds = _bHarvestSeeds;
	}
	
}
