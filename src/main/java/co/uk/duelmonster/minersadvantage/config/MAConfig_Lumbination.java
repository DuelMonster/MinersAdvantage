package co.uk.duelmonster.minersadvantage.config;

// Lumbination Settings
public class MAConfig_Lumbination extends MAConfig_SubCategory {
	
	public MAConfig_Lumbination(MAConfig _parentConfig) {
		super(_parentConfig);
	}
	
	// ====================================================================================================
	// = Config variables - !! NOT TO BE USED DIRECTLY !! - Use the retrieval functions below at all times!
	// ====================================================================================================
	
	private boolean		_bChopTreeBelow				= true;
	private boolean		_bDestroyLeaves				= true;
	private boolean		_bLeavesAffectDurability	= false;
	private boolean		_bReplantSaplings			= true;
	private boolean		_bUseShearsOnLeaves			= true;
	private int			_iLeafRange					= 6;
	private int			_iTrunkRange				= 32;
	private String[]	_Logs						= {};
	private String[]	_Leaves						= {};
	private String[]	_Axes						= {};
	
	// ====================================================================================================
	// = Config retrieval functions
	// ====================================================================================================
	
	/**
	 * @return bEnabled
	 */
	@Override
	public boolean bEnabled() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bOverrideFeatureEnablement && !parentConfig.serverOverrides.lumbination.bEnabled())
			return false;
		
		return super.bEnabled();
	}
	
	/**
	 * @return bChopTreeBelow
	 */
	public boolean bChopTreeBelow() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceLumbinationSettings)
			return parentConfig.serverOverrides.lumbination.bChopTreeBelow();
		
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
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceLumbinationSettings)
			return parentConfig.serverOverrides.lumbination.bDestroyLeaves();
		
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
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceLumbinationSettings)
			return parentConfig.serverOverrides.lumbination.bLeavesAffectDurability();
		
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
	 * @return bReplantSaplings
	 */
	public boolean bReplantSaplings() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceLumbinationSettings)
			return parentConfig.serverOverrides.lumbination.bReplantSaplings();
		
		return _bReplantSaplings;
	}
	
	/**
	 * @param _bReplantSaplings
	 *            Sets bReplantSaplings
	 */
	public void setReplantSaplings(boolean _bReplantSaplings) {
		this._bReplantSaplings = _bReplantSaplings;
	}
	
	/**
	 * @return bUseShearsOnLeaves
	 */
	public boolean bUseShearsOnLeaves() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceLumbinationSettings)
			return parentConfig.serverOverrides.lumbination.bUseShearsOnLeaves();
		
		return _bUseShearsOnLeaves;
	}
	
	/**
	 * @param _bUseShearsOnLeaves
	 *            Sets bUseShearsOnLeaves
	 */
	public void setUseShearsOnLeaves(boolean _bUseShearsOnLeaves) {
		this._bUseShearsOnLeaves = _bUseShearsOnLeaves;
	}
	
	/**
	 * @return iLeafRange
	 */
	public int iLeafRange() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceLumbinationSettings)
			return parentConfig.serverOverrides.lumbination.iLeafRange();
		
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
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceLumbinationSettings)
			return parentConfig.serverOverrides.lumbination.iTrunkRange();
		
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
	 * @return Lumbination Logs
	 */
	public String[] logs() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceLumbinationSettings)
			return parentConfig.serverOverrides.lumbination.logs();
		
		return _Logs;
	}
	
	/**
	 * @param _Logs
	 *            Sets Lumbination Logs
	 */
	public void setLogs(String[] _Logs) {
		this._Logs = _Logs;
	}
	
	/**
	 * @return Lumbination Leaves
	 */
	public String[] leaves() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceLumbinationSettings)
			return parentConfig.serverOverrides.lumbination.leaves();
		
		return _Leaves;
	}
	
	/**
	 * @param _leaves
	 *            Sets Lumbination Leaves
	 */
	public void setLeaves(String[] _Leaves) {
		this._Leaves = _Leaves;
	}
	
	/**
	 * @return Lumbination Axes
	 */
	public String[] axes() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceLumbinationSettings)
			return parentConfig.serverOverrides.lumbination.axes();
		
		return _Axes;
	}
	
	/**
	 * @param _Axes
	 *            Sets Lumbination Axes
	 */
	public void setAxes(String[] _Axes) {
		this._Axes = _Axes;
	}
	
}
