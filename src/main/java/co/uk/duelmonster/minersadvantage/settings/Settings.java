package co.uk.duelmonster.minersadvantage.settings;

import java.util.HashMap;
import java.util.UUID;

import com.google.gson.JsonObject;

import co.uk.duelmonster.minersadvantage.MinersAdvantage;
import co.uk.duelmonster.minersadvantage.client.ClientFunctions;
import co.uk.duelmonster.minersadvantage.common.Constants;
import co.uk.duelmonster.minersadvantage.common.JsonHelper;
import net.minecraft.init.Items;

public class Settings {
	
	public static Settings	defaults		= new Settings();
	public SettingsServer	serverOverrides	= null;
	
	private static HashMap<UUID, Settings> playerSettings = new HashMap<UUID, Settings>();
	
	public static Settings get() {
		return get(Constants.instanceUID);
	}
	
	public static Settings get(UUID uid) {
		if (playerSettings.isEmpty() || playerSettings.get(uid) == null)
			set(uid, new Settings());
		
		return playerSettings.get(uid);
	}
	
	public static Settings set(UUID uid, Settings settings) {
		return playerSettings.put(uid, settings);
	}
	
	public boolean hasChanged() {
		String current = JsonHelper.gson.toJson(this);
		if (current.equals(current) || history == null || history.isEmpty()) {
			history = current;
			return true;
		}
		
		return false;
	}
	
	private transient String history = null;
	
	// Common Settings
	private boolean	_tpsGuard					= true;
	private boolean	_bGatherDrops				= false;
	private boolean	_bAutoIlluminate			= true;
	private boolean	_bMineVeins					= true;
	private int		_iBlocksPerTick				= 2;
	private boolean	_bEnableTickDelay			= true;
	private int		_iTickDelay					= 3;
	private int		_iBlockRadius				= Constants.DEFAULT_BLOCKRADIUS;
	private int		_iBlockLimit				= (Constants.MIN_BLOCKRADIUS * Constants.MIN_BLOCKRADIUS) * Constants.MIN_BLOCKRADIUS;
	private boolean	_bBreakAtToolSpeeds			= false;
	private boolean	_bDisableParticleEffects	= false;
	
	// Captivation Settings
	public boolean		_bCaptivationEnabled	= true;
	public boolean		_bAllowInGUI			= false;
	public boolean		_bIsWhitelist			= false;
	public double		_radiusHorizontal		= 16;
	public double		_radiusVertical			= 16;
	public JsonObject	_captivationBlacklist	= JsonHelper.ParseObject("{\"" + Items.ROTTEN_FLESH.getRegistryName().toString().trim() + "\":\"\"}");
	
	// Cropination Settings
	private boolean	_bCropinationEnabled	= true;
	private boolean	_bHarvestSeeds			= true;
	
	// Excavation + Pathanation Settings
	private boolean		_bExcavationEnabled		= true;
	private boolean		_bIgnoreBlockVariants	= false;
	private boolean		_bToggleMode			= false;
	private boolean		_bIsToolWhitelist		= false;
	private boolean		_bIsBlockWhitelist		= false;
	private int			_iPathWidth				= 3;
	private int			_iPathLength			= 6;
	private JsonObject	_toolBlacklist			= new JsonObject();
	private JsonObject	_blockBlacklist			= new JsonObject();
	
	// Illumination Settings
	private boolean	_bIlluminationEnabled	= true;
	private boolean	_bUseBlockLight			= true;
	private int		_iLowestLightLevel		= 7;
	
	// Lumbination Settings
	private boolean		_bLumbinationEnabled		= true;
	private boolean		_bChopTreeBelow				= true;
	private boolean		_bDestroyLeaves				= true;
	private boolean		_bLeavesAffectDurability	= false;
	private int			_iLeafRange					= 6;
	private int			_iTrunkRange				= 32;
	private JsonObject	_lumbinationLogs			= new JsonObject();
	private JsonObject	_lumbinationLeaves			= new JsonObject();
	private JsonObject	_lumbinationAxes			= new JsonObject();
	
	// Shaftanation Settings
	private boolean	_bShaftanationEnabled	= true;
	private int		_iShaftLength			= 16;
	private int		_iShaftHeight			= 2;
	private int		_iShaftWidth			= 1;
	
	// Substitution Settings
	private boolean	_bSubstitutionEnabled	= true;
	private boolean	_bSwitchBack			= true;
	private boolean	_bFavourSilkTouch		= false;
	private boolean	_bFavourFortune			= true;
	private boolean	_bIgnoreIfValidTool		= true;
	private boolean	_bIgnorePassiveMobs		= true;
	
	// Veination Settings
	private boolean		_bVeinationEnabled	= true;
	private JsonObject	_veinationOres		= new JsonObject();
	
	public boolean allEnabled() {
		return (_bCaptivationEnabled &&
				_bCropinationEnabled &&
				_bExcavationEnabled &&
				_bLumbinationEnabled &&
				_bIlluminationEnabled &&
				_bShaftanationEnabled &&
				_bSubstitutionEnabled &&
				_bVeinationEnabled);
	}
	
	/**
	 * @return tpsGuard
	 */
	public boolean tpsGuard() {
		if (serverOverrides != null && serverOverrides.bEnforceCommonSettings)
			return serverOverrides.tpsGuard();
		
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
		if (serverOverrides != null && serverOverrides.bEnforceCommonSettings)
			return serverOverrides.bGatherDrops();
		
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
		if (serverOverrides != null && serverOverrides.bEnforceCommonSettings)
			return serverOverrides.bAutoIlluminate();
		
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
		if (serverOverrides != null && serverOverrides.bEnforceCommonSettings)
			return serverOverrides.bMineVeins();
		
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
		if (serverOverrides != null && serverOverrides.bEnforceCommonSettings)
			return serverOverrides.iBlocksPerTick();
		
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
		if (serverOverrides != null && serverOverrides.bEnforceCommonSettings)
			return serverOverrides.bEnableTickDelay();
		
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
		if (serverOverrides != null && serverOverrides.bEnforceCommonSettings)
			return serverOverrides.iTickDelay();
		
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
		if (serverOverrides != null && serverOverrides.bEnforceCommonSettings)
			return serverOverrides.iBlockRadius();
		
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
		if (serverOverrides != null && serverOverrides.bEnforceCommonSettings)
			return serverOverrides.iBlockLimit();
		
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
		if (serverOverrides != null && serverOverrides.bEnforceCommonSettings)
			return serverOverrides.bBreakAtToolSpeeds();
		
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
		if (serverOverrides != null && serverOverrides.bEnforceCommonSettings)
			return serverOverrides.bDisableParticleEffects();
		
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
	
	/**
	 * @return bCaptivationEnabled
	 */
	public boolean bCaptivationEnabled() {
		if (serverOverrides != null && serverOverrides.bOverrideFeatureEnablement && !serverOverrides.bCaptivationEnabled())
			return false;
		
		return _bCaptivationEnabled;
	}
	
	/**
	 * @param _bCaptivationEnabled
	 *            Sets bCaptivationEnabled
	 */
	public void setCaptivationEnabled(boolean _bCaptivationEnabled) {
		if (serverOverrides == null || !serverOverrides.bOverrideFeatureEnablement)
			this._bCaptivationEnabled = _bCaptivationEnabled;
	}
	
	/**
	 * @return bAllowInGUI
	 */
	public boolean bAllowInGUI() {
		if (serverOverrides != null && serverOverrides.bEnforceCaptivationSettings)
			return serverOverrides.bAllowInGUI();
		
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
		if (serverOverrides != null && serverOverrides.bEnforceCaptivationSettings)
			return serverOverrides.bIsWhitelist();
		
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
		if (serverOverrides != null && serverOverrides.bEnforceCaptivationSettings)
			return serverOverrides.radiusHorizontal();
		
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
		if (serverOverrides != null && serverOverrides.bEnforceCaptivationSettings)
			return serverOverrides.radiusVertical();
		
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
	public JsonObject captivationBlacklist() {
		if (serverOverrides != null && serverOverrides.bEnforceCaptivationSettings)
			return serverOverrides.captivationBlacklist();
		
		return _captivationBlacklist;
	}
	
	/**
	 * @param _captivationBlacklist
	 *            Sets captivationBlacklist
	 */
	public void setCaptivationBlacklist(JsonObject _captivationBlacklist) {
		this._captivationBlacklist = _captivationBlacklist;
	}
	
	/**
	 * @return bCropinationEnabled
	 */
	public boolean bCropinationEnabled() {
		if (serverOverrides != null && serverOverrides.bOverrideFeatureEnablement && !serverOverrides.bCropinationEnabled())
			return false;
		
		return _bCropinationEnabled;
	}
	
	/**
	 * @param _bCropinationEnabled
	 *            Sets bCropinationEnabled
	 */
	public void setCropinationEnabled(boolean _bCropinationEnabled) {
		if (serverOverrides == null || !serverOverrides.bOverrideFeatureEnablement)
			this._bCropinationEnabled = _bCropinationEnabled;
	}
	
	/**
	 * @return bHarvestSeeds
	 */
	public boolean bHarvestSeeds() {
		if (serverOverrides != null && serverOverrides.bEnforceCropinationSettings)
			return serverOverrides.bHarvestSeeds();
		
		return _bHarvestSeeds;
	}
	
	/**
	 * @param _bHarvestSeeds
	 *            Sets bHarvestSeeds
	 */
	public void setHarvestSeeds(boolean _bHarvestSeeds) {
		this._bHarvestSeeds = _bHarvestSeeds;
	}
	
	/**
	 * @return bExcavationEnabled
	 */
	public boolean bExcavationEnabled() {
		if (serverOverrides != null && serverOverrides.bOverrideFeatureEnablement && !serverOverrides.bExcavationEnabled())
			return false;
		
		return _bExcavationEnabled;
	}
	
	/**
	 * @param _bExcavationEnabled
	 *            Sets bExcavationEnabled
	 */
	public void setExcavationEnabled(boolean _bExcavationEnabled) {
		if (serverOverrides == null || !serverOverrides.bOverrideFeatureEnablement)
			this._bExcavationEnabled = _bExcavationEnabled;
	}
	
	/**
	 * @return bIgnoreBlockVariants
	 */
	public boolean bIgnoreBlockVariants() {
		if (serverOverrides != null && serverOverrides.bEnforceExcavationSettings)
			return serverOverrides.bIgnoreBlockVariants();
		
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
		if (serverOverrides != null && serverOverrides.bEnforceExcavationSettings)
			return serverOverrides.bToggleMode();
		
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
		if (serverOverrides != null && serverOverrides.bEnforceExcavationSettings)
			return serverOverrides.bIsToolWhitelist();
		
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
	 * @return bIsBlockWhitelist
	 */
	public boolean bIsBlockWhitelist() {
		if (serverOverrides != null && serverOverrides.bEnforceExcavationSettings)
			return serverOverrides.bIsBlockWhitelist();
		
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
	 * @return iPathWidth
	 */
	public int iPathWidth() {
		if (serverOverrides != null && serverOverrides.bEnforceExcavationSettings)
			return serverOverrides.iPathWidth();
		
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
		if (serverOverrides != null && serverOverrides.bEnforceExcavationSettings)
			return serverOverrides.iPathLength();
		
		return _iPathLength;
	}
	
	/**
	 * @param _iPathLength
	 *            Sets iPathLength
	 */
	public void setPathLength(int _iPathLength) {
		this._iPathLength = _iPathLength;
	}
	
	/**
	 * @return toolBlacklist
	 */
	public JsonObject toolBlacklist() {
		if (serverOverrides != null && serverOverrides.bEnforceExcavationSettings)
			return serverOverrides.toolBlacklist();
		
		return _toolBlacklist;
	}
	
	/**
	 * @param _toolBlacklist
	 *            Sets toolBlacklist
	 */
	public void setToolBlacklist(JsonObject _toolBlacklist) {
		this._toolBlacklist = _toolBlacklist;
	}
	
	/**
	 * @return blockBlacklist
	 */
	public JsonObject blockBlacklist() {
		if (serverOverrides != null && serverOverrides.bEnforceExcavationSettings)
			return serverOverrides.blockBlacklist();
		
		return _blockBlacklist;
	}
	
	/**
	 * @param _blockBlacklist
	 *            Sets blockBlacklist
	 */
	public void setBlockBlacklist(JsonObject _blockBlacklist) {
		this._blockBlacklist = _blockBlacklist;
	}
	
	/**
	 * @return bIlluminationEnabled
	 */
	public boolean bIlluminationEnabled() {
		if (serverOverrides != null && serverOverrides.bOverrideFeatureEnablement && !serverOverrides.bIlluminationEnabled())
			return false;
		
		return _bIlluminationEnabled;
	}
	
	/**
	 * @param _bIlluminationEnabled
	 *            Sets bIlluminationEnabled
	 */
	public void setIlluminationEnabled(boolean _bIlluminationEnabled) {
		if (serverOverrides == null || !serverOverrides.bOverrideFeatureEnablement)
			this._bIlluminationEnabled = _bIlluminationEnabled;
	}
	
	/**
	 * @return bUseBlockLight
	 */
	public boolean bUseBlockLight() {
		if (serverOverrides != null && serverOverrides.bEnforceIlluminationSettings)
			return false;
		
		return _bUseBlockLight;
	}
	
	/**
	 * @param _bUseBlockLight
	 *            Sets bUseBlockLight
	 */
	public void setUseBlockLight(boolean _bUseBlockLight) {
		if (serverOverrides == null || !serverOverrides.bEnforceIlluminationSettings)
			this._bUseBlockLight = _bUseBlockLight;
	}
	
	/**
	 * @return iLowestLightLevel
	 */
	public int iLowestLightLevel() {
		if (serverOverrides != null && serverOverrides.bEnforceIlluminationSettings)
			return serverOverrides.iLowestLightLevel();
		
		return _iLowestLightLevel;
	}
	
	/**
	 * @param _iLowestLightLevel
	 *            Sets iLowestLightLevel
	 */
	public void setLowestLightLevel(int _iLowestLightLevel) {
		this._iLowestLightLevel = _iLowestLightLevel;
	}
	
	/**
	 * @return bLumbinationEnabled
	 */
	public boolean bLumbinationEnabled() {
		if (serverOverrides != null && serverOverrides.bOverrideFeatureEnablement && !serverOverrides.bLumbinationEnabled())
			return false;
		
		return _bLumbinationEnabled;
	}
	
	/**
	 * @param _bLumbinationEnabled
	 *            Sets bLumbinationEnabled
	 */
	public void setLumbinationEnabled(boolean _bLumbinationEnabled) {
		if (serverOverrides == null || !serverOverrides.bOverrideFeatureEnablement)
			this._bLumbinationEnabled = _bLumbinationEnabled;
	}
	
	/**
	 * @return bChopTreeBelow
	 */
	public boolean bChopTreeBelow() {
		if (serverOverrides != null && serverOverrides.bEnforceLumbinationSettings)
			return serverOverrides.bChopTreeBelow();
		
		return _bChopTreeBelow;
	}
	
	/**
	 * @param _bChopTreeBelow
	 *            Sets bChopTreeBelow
	 */
	public void setChopTreeBelow(boolean _bChopTreeBelow) {
		this._bChopTreeBelow = _bChopTreeBelow;
	}
	
	/**
	 * @return bDestroyLeaves
	 */
	public boolean bDestroyLeaves() {
		if (serverOverrides != null && serverOverrides.bEnforceLumbinationSettings)
			return serverOverrides.bDestroyLeaves();
		
		return _bDestroyLeaves;
	}
	
	/**
	 * @param _bDestroyLeaves
	 *            Sets bDestroyLeaves
	 */
	public void setDestroyLeaves(boolean _bDestroyLeaves) {
		this._bDestroyLeaves = _bDestroyLeaves;
	}
	
	/**
	 * @return bLeavesAffectDurability
	 */
	public boolean bLeavesAffectDurability() {
		if (serverOverrides != null && serverOverrides.bEnforceLumbinationSettings)
			return serverOverrides.bLeavesAffectDurability();
		
		return _bLeavesAffectDurability;
	}
	
	/**
	 * @param _bLeavesAffectDurability
	 *            Sets bLeavesAffectDurability
	 */
	public void setLeavesAffectDurability(boolean _bLeavesAffectDurability) {
		this._bLeavesAffectDurability = _bLeavesAffectDurability;
	}
	
	/**
	 * @return iLeafRange
	 */
	public int iLeafRange() {
		if (serverOverrides != null && serverOverrides.bEnforceLumbinationSettings)
			return serverOverrides.iLeafRange();
		
		return _iLeafRange;
	}
	
	/**
	 * @param _iLeafRange
	 *            Sets iLeafRange
	 */
	public void setLeafRange(int _iLeafRange) {
		this._iLeafRange = _iLeafRange;
	}
	
	/**
	 * @return iTrunkRange
	 */
	public int iTrunkRange() {
		if (serverOverrides != null && serverOverrides.bEnforceLumbinationSettings)
			return serverOverrides.iTrunkRange();
		
		return _iTrunkRange;
	}
	
	/**
	 * @param _iTrunkRange
	 *            Sets iTrunkRange
	 */
	public void setTrunkRange(int _iTrunkRange) {
		this._iTrunkRange = _iTrunkRange;
	}
	
	/**
	 * @return lumbinationLogs
	 */
	public JsonObject lumbinationLogs() {
		if (serverOverrides != null && serverOverrides.bEnforceLumbinationSettings)
			return serverOverrides.lumbinationLogs();
		
		return _lumbinationLogs;
	}
	
	/**
	 * @param _lumbinationLogs
	 *            Sets lumbinationLogs
	 */
	public void setLumbinationLogs(JsonObject _lumbinationLogs) {
		this._lumbinationLogs = _lumbinationLogs;
	}
	
	/**
	 * @return lumbinationLeaves
	 */
	public JsonObject lumbinationLeaves() {
		if (serverOverrides != null && serverOverrides.bEnforceLumbinationSettings)
			return serverOverrides.lumbinationLeaves();
		
		return _lumbinationLeaves;
	}
	
	/**
	 * @param _lumbinationLeaves
	 *            Sets lumbinationLeaves
	 */
	public void setLumbinationLeaves(JsonObject _lumbinationLeaves) {
		this._lumbinationLeaves = _lumbinationLeaves;
	}
	
	/**
	 * @return lumbinationAxes
	 */
	public JsonObject lumbinationAxes() {
		if (serverOverrides != null && serverOverrides.bEnforceLumbinationSettings)
			return serverOverrides.lumbinationAxes();
		
		return _lumbinationAxes;
	}
	
	/**
	 * @param _lumbinationAxes
	 *            Sets lumbinationAxes
	 */
	public void setLumbinationAxes(JsonObject _lumbinationAxes) {
		this._lumbinationAxes = _lumbinationAxes;
	}
	
	/**
	 * @return bShaftanationEnabled
	 */
	public boolean bShaftanationEnabled() {
		if (serverOverrides != null && serverOverrides.bOverrideFeatureEnablement && !serverOverrides.bShaftanationEnabled())
			return false;
		
		return _bShaftanationEnabled;
	}
	
	/**
	 * @param _bShaftanationEnabled
	 *            Sets bShaftanationEnabled
	 */
	public void setShaftanationEnabled(boolean _bShaftanationEnabled) {
		if (serverOverrides == null || !serverOverrides.bOverrideFeatureEnablement)
			this._bShaftanationEnabled = _bShaftanationEnabled;
	}
	
	/**
	 * @return iShaftLength
	 */
	public int iShaftLength() {
		if (serverOverrides != null && serverOverrides.bEnforceShaftanationSettings)
			return serverOverrides.iShaftLength();
		
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
		if (serverOverrides != null && serverOverrides.bEnforceShaftanationSettings)
			return serverOverrides.iShaftHeight();
		
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
		if (serverOverrides != null && serverOverrides.bEnforceShaftanationSettings)
			return serverOverrides.iShaftWidth();
		
		return _iShaftWidth;
	}
	
	/**
	 * @param _iShaftWidth
	 *            Sets iShaftWidth
	 */
	public void setShaftWidth(int _iShaftWidth) {
		this._iShaftWidth = _iShaftWidth;
	}
	
	/**
	 * @return bSubstitutionEnabled
	 */
	public boolean bSubstitutionEnabled() {
		if (serverOverrides != null && serverOverrides.bOverrideFeatureEnablement && !serverOverrides.bSubstitutionEnabled())
			return false;
		
		return _bSubstitutionEnabled;
	}
	
	/**
	 * @param _bSubstitutionEnabled
	 *            Sets bSubstitutionEnabled
	 */
	public void setSubstitutionEnabled(boolean _bSubstitutionEnabled) {
		if (serverOverrides == null || !serverOverrides.bOverrideFeatureEnablement)
			this._bSubstitutionEnabled = _bSubstitutionEnabled;
	}
	
	/**
	 * @return bSwitchBack
	 */
	public boolean bSwitchBack() {
		if (serverOverrides != null && serverOverrides.bEnforceSubstitutionSettings)
			return serverOverrides.bSwitchBack();
		
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
		if (serverOverrides != null && serverOverrides.bEnforceSubstitutionSettings)
			return serverOverrides.bFavourSilkTouch();
		
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
		if (serverOverrides != null && serverOverrides.bEnforceSubstitutionSettings)
			return serverOverrides.bFavourFortune();
		
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
		if (serverOverrides != null && serverOverrides.bEnforceSubstitutionSettings)
			return serverOverrides.bIgnoreIfValidTool();
		
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
		if (serverOverrides != null && serverOverrides.bEnforceSubstitutionSettings)
			return serverOverrides.bIgnorePassiveMobs();
		
		return _bIgnorePassiveMobs;
	}
	
	/**
	 * @param _bIgnorePassiveMobs
	 *            Sets bIgnorePassiveMobs
	 */
	public void setIgnorePassiveMobs(boolean _bIgnorePassiveMobs) {
		this._bIgnorePassiveMobs = _bIgnorePassiveMobs;
	}
	
	/**
	 * @return bVeinationEnabled
	 */
	public boolean bVeinationEnabled() {
		if (serverOverrides != null && serverOverrides.bOverrideFeatureEnablement && !serverOverrides.bVeinationEnabled())
			return false;
		
		return _bVeinationEnabled;
	}
	
	/**
	 * @param _bVeinationEnabled
	 *            Sets bVeinationEnabled
	 */
	public void setVeinationEnabled(boolean _bVeinationEnabled) {
		if (serverOverrides == null || !serverOverrides.bOverrideFeatureEnablement)
			this._bVeinationEnabled = _bVeinationEnabled;
	}
	
	/**
	 * @return veinationOres
	 */
	public JsonObject veinationOres() {
		if (serverOverrides != null && serverOverrides.bEnforceVeinationSettings)
			return serverOverrides.veinationOres();
		
		return _veinationOres;
	}
	
	/**
	 * @param _veinationOres
	 *            Sets veinationOres
	 */
	public void setVeinationOres(JsonObject _veinationOres) {
		this._veinationOres = _veinationOres;
	}
}
