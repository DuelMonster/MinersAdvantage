package co.uk.duelmonster.minersadvantage.config;

import co.uk.duelmonster.minersadvantage.MinersAdvantage;
import co.uk.duelmonster.minersadvantage.client.ClientFunctions;
import co.uk.duelmonster.minersadvantage.common.Constants;

// Common Settings
public class MAConfig_Common extends MAConfig_SubCategory {
	
	public MAConfig_Common(MAConfig _parentConfig) {
		super(_parentConfig);
	}
	
	// ====================================================================================================
	// = Config variables - !! NOT TO BE USED DIRECTLY !! - Use the retrieval functions below at all times!
	// ====================================================================================================
	
	private boolean	_tpsGuard					= true;
	private boolean	_bGatherDrops				= false;
	private boolean	_bAutoIlluminate			= true;
	private boolean	_bMineVeins					= true;
	private int		_iBlocksPerTick				= 2;
	private boolean	_bEnableTickDelay			= true;
	private int		_iTickDelay					= 3;
	private int		_iBlockRadius				= Constants.DEFAULT_BLOCKRADIUS;
	private int		_iBlockLimit				= (Constants.DEFAULT_BLOCKRADIUS * Constants.DEFAULT_BLOCKRADIUS) * Constants.DEFAULT_BLOCKRADIUS;
	private boolean	_bBreakAtToolSpeeds			= false;
	private boolean	_bDisableParticleEffects	= false;
	
	// ====================================================================================================
	// = Config retrieval functions
	// ====================================================================================================
	
	/**
	 * @return tpsGuard
	 */
	public boolean tpsGuard() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceCommonSettings)
			return parentConfig.serverOverrides.common.tpsGuard();
		
		return _tpsGuard;
	}
	
	/**
	 * @param _tpsGuard
	 *            Sets tpsGuard
	 */
	public void set_tpsGuard(boolean _tpsGuard) {
		this._tpsGuard = _tpsGuard;
	}
	
	/**
	 * @return bGatherDrops
	 */
	public boolean bGatherDrops() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceCommonSettings)
			return parentConfig.serverOverrides.common.bGatherDrops();
		
		return _bGatherDrops;
	}
	
	/**
	 * @param _bGatherDrops
	 *            Sets bGatherDrops
	 */
	public void setGatherDrops(boolean _bGatherDrops) {
		this._bGatherDrops = _bGatherDrops;
	}
	
	/**
	 * @return bAutoIlluminate
	 */
	public boolean bAutoIlluminate() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceCommonSettings)
			return parentConfig.serverOverrides.common.bAutoIlluminate();
		
		return _bAutoIlluminate;
	}
	
	/**
	 * @param _bAutoIlluminate
	 *            Sets bAutoIlluminate
	 */
	public void setAutoIlluminate(boolean _bAutoIlluminate) {
		this._bAutoIlluminate = _bAutoIlluminate;
	}
	
	/**
	 * @return bMineVeins
	 */
	public boolean bMineVeins() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceCommonSettings)
			return parentConfig.serverOverrides.common.bMineVeins();
		
		return _bMineVeins;
	}
	
	/**
	 * @param _bMineVeins
	 *            Sets bMineVeins
	 */
	public void setMineVeins(boolean _bMineVeins) {
		this._bMineVeins = _bMineVeins;
	}
	
	/**
	 * @return iBlocksPerTick
	 */
	public int iBlocksPerTick() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceCommonSettings)
			return parentConfig.serverOverrides.common.iBlocksPerTick();
		
		return _iBlocksPerTick;
	}
	
	/**
	 * @param _iBlocksPerTick
	 *            Sets iBlocksPerTick
	 */
	public void setBlocksPerTick(int _iBlocksPerTick) {
		this._iBlocksPerTick = _iBlocksPerTick;
	}
	
	/**
	 * @return bEnableTickDelay
	 */
	public boolean bEnableTickDelay() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceCommonSettings)
			return parentConfig.serverOverrides.common.bEnableTickDelay();
		
		return _bEnableTickDelay;
	}
	
	/**
	 * @param _bEnableTickDelay
	 *            Sets bEnableTickDelay
	 */
	public void setEnableTickDelay(boolean _bEnableTickDelay) {
		this._bEnableTickDelay = _bEnableTickDelay;
	}
	
	/**
	 * @return iTickDelay
	 */
	public int iTickDelay() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceCommonSettings)
			return parentConfig.serverOverrides.common.iTickDelay();
		
		return _iTickDelay;
	}
	
	/**
	 * @param _iTickDelay
	 *            Sets iTickDelay
	 */
	public void setTickDelay(int _iTickDelay) {
		this._iTickDelay = _iTickDelay;
	}
	
	/**
	 * @return iBlockRadius
	 */
	public int iBlockRadius() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceCommonSettings)
			return parentConfig.serverOverrides.common.iBlockRadius();
		
		return _iBlockRadius;
	}
	
	/**
	 * @param _iBlockRadius
	 *            Sets iBlockRadius
	 */
	public void setBlockRadius(int _iBlockRadius) {
		this._iBlockRadius = _iBlockRadius;
	}
	
	/**
	 * @return iBlockLimit
	 */
	public int iBlockLimit() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceCommonSettings)
			return parentConfig.serverOverrides.common.iBlockLimit();
		
		return _iBlockLimit;
	}
	
	/**
	 * @param _iBlockLimit
	 *            Sets iBlockLimit
	 */
	public void setBlockLimit(int _iBlockLimit) {
		this._iBlockLimit = _iBlockLimit;
	}
	
	/**
	 * @return iBlockLimit
	 */
	public boolean bBreakAtToolSpeeds() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceCommonSettings)
			return parentConfig.serverOverrides.common.bBreakAtToolSpeeds();
		
		return _bBreakAtToolSpeeds;
	}
	
	/**
	 * @param _bBreakAtToolSpeeds
	 *            Sets bBreakAtToolSpeeds
	 */
	public void setBreakAtToolSpeeds(boolean _bBreakAtToolSpeeds) {
		this._bBreakAtToolSpeeds = _bBreakAtToolSpeeds;
	}
	
	/**
	 * @return iBlockLimit
	 */
	public boolean bDisableParticleEffects() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceCommonSettings)
			return parentConfig.serverOverrides.common.bDisableParticleEffects();
		
		return _bDisableParticleEffects;
	}
	
	/**
	 * @param _bDisableParticleEffects
	 *            Sets bDisableParticleEffects
	 */
	public void setDisableParticleEffects(boolean _bDisableParticleEffects) {
		this._bDisableParticleEffects = _bDisableParticleEffects;
		
		if (MinersAdvantage.proxy.origParticleManager != null && MinersAdvantage.proxy.maParticleManager != null)
			if (this.bDisableParticleEffects())
				ClientFunctions.getMC().effectRenderer = MinersAdvantage.proxy.maParticleManager;
			else
				ClientFunctions.getMC().effectRenderer = MinersAdvantage.proxy.origParticleManager;
	}
}
