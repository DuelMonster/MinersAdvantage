package co.uk.duelmonster.minersadvantage.config;

import net.minecraft.init.Items;
import net.minecraft.item.Item;

// Captivation Settings
public class MAConfig_Captivation extends MAConfig_SubCategory {
	
	public MAConfig_Captivation(MAConfig _parentConfig) {
		super(_parentConfig);
	}
	
	// ====================================================================================================
	// = Config variables - !! NOT TO BE USED DIRECTLY !! - Use the retrieval functions below at all times!
	// ====================================================================================================
	
	private boolean		_bAllowInGUI		= false;
	private double		_radiusHorizontal	= 16;
	private double		_radiusVertical		= 16;
	private boolean		_bIsWhitelist		= false;
	private String[]	_Blacklist			= { Item.REGISTRY.getNameForObject(Items.ROTTEN_FLESH).toString().trim() };
	
	// ====================================================================================================
	// = Config retrieval functions
	// ====================================================================================================
	
	/**
	 * @return bEnabled
	 */
	@Override
	public boolean bEnabled() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bOverrideFeatureEnablement && !parentConfig.serverOverrides.captivation.bEnabled())
			return false;
		
		return super.bEnabled();
	}
	
	/**
	 * @return bAllowInGUI
	 */
	public boolean bAllowInGUI() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceCaptivationSettings)
			return parentConfig.serverOverrides.captivation.bAllowInGUI();
		
		return _bAllowInGUI;
	}
	
	/**
	 * @param _bAllowInGUI
	 *            Sets bAllowInGUI
	 */
	public void setAllowInGUI(boolean _bAllowInGUI) {
		this._bAllowInGUI = _bAllowInGUI;
	}
	
	/**
	 * @return bIsWhitelist
	 */
	public boolean bIsWhitelist() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceCaptivationSettings)
			return parentConfig.serverOverrides.captivation.bIsWhitelist();
		
		return _bIsWhitelist;
	}
	
	/**
	 * @param _bIsWhitelist
	 *            Sets bIsWhitelist
	 */
	public void setIsWhitelist(boolean _bIsWhitelist) {
		this._bIsWhitelist = _bIsWhitelist;
	}
	
	/**
	 * @return radiusHorizontal
	 */
	public double radiusHorizontal() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceCaptivationSettings)
			return parentConfig.serverOverrides.captivation.radiusHorizontal();
		
		return _radiusHorizontal;
	}
	
	/**
	 * @param _radiusHorizontal
	 *            Sets radiusHorizontal
	 */
	public void setRadiusHorizontal(double _radiusHorizontal) {
		this._radiusHorizontal = _radiusHorizontal;
	}
	
	/**
	 * @return radiusVertical
	 */
	public double radiusVertical() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceCaptivationSettings)
			return parentConfig.serverOverrides.captivation.radiusVertical();
		
		return _radiusVertical;
	}
	
	/**
	 * @param _radiusVertical
	 *            Sets radiusVertical
	 */
	public void setRadiusVertical(double _radiusVertical) {
		this._radiusVertical = _radiusVertical;
	}
	
	/**
	 * @return captivationBlacklist
	 */
	public String[] blacklist() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceCaptivationSettings)
			return parentConfig.serverOverrides.captivation.blacklist();
		
		return _Blacklist;
	}
	
	/**
	 * @param _captivationBlacklist
	 *            Sets captivationBlacklist
	 */
	public void setBlacklist(String[] _Blacklist) {
		this._Blacklist = _Blacklist;
	}
	
}
