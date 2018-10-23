package co.uk.duelmonster.minersadvantage.config;

import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.util.IStringSerializable;

// Shaftanation Settings
public class MAConfig_Shaftanation extends MAConfig_SubCategory {
	
	public MAConfig_Shaftanation(MAConfig _parentConfig) {
		super(_parentConfig);
	}
	
	// ====================================================================================================
	// = Config variables - !! NOT TO BE USED DIRECTLY !! - Use the retrieval functions below at all times!
	// ====================================================================================================
	
	private int				_iShaftLength	= 16;
	private int				_iShaftHeight	= 2;
	private int				_iShaftWidth	= 1;
	private TorchPlacement	_TorchPlacement	= TorchPlacement.FLOOR;
	
	public enum TorchPlacement implements IStringSerializable {
		FLOOR(0, "Floor"), LEFT_WALL(1, "Left Wall"), RIGHT_WALL(2, "Right Wall");
		
		private final int		index;
		private final String	name;
		
		public static final TorchPlacement[]				VALUES		= new TorchPlacement[6];
		private static final Map<String, TorchPlacement>	NAME_LOOKUP	= Maps.<String, TorchPlacement>newHashMap();
		
		private TorchPlacement(int indexIn, String nameIn) {
			this.index = indexIn;
			this.name = nameIn;
		}
		
		@Override
		public String getName() {
			return this.name;
		}
		
		/**
		 * Get the Index of this Facing (0-5). The order is D-U-N-S-W-E
		 */
		public int getIndex() {
			return this.index;
		}
		
		/**
		 * Get the facing specified by the given name
		 */
		@Nullable
		public static TorchPlacement byName(String name) {
			return name == null ? null : (TorchPlacement) NAME_LOOKUP.get(name.toLowerCase(Locale.ROOT));
		}
		
		@Override
		public String toString() {
			return this.name;
		}
		
		static {
			for (TorchPlacement enumTP : values()) {
				VALUES[enumTP.index] = enumTP;
				NAME_LOOKUP.put(enumTP.getName().toLowerCase(Locale.ROOT), enumTP);
			}
		}
	}
	
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
	
	/**
	 * @return TorchPlacement
	 */
	public TorchPlacement TorchPlacement() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.server.bEnforceShaftanationSettings)
			return parentConfig.serverOverrides.shaftanation.TorchPlacement();
		
		return _TorchPlacement;
	}
	
	/**
	 * @param _TorchPlacement
	 *            Sets TorchPlacement
	 */
	public void setTorchPlacement(TorchPlacement _TorchPlacement) {
		this._TorchPlacement = _TorchPlacement;
	}
	
	public void setTorchPlacement(String sTorchPlacement) {
		this._TorchPlacement = TorchPlacement.byName(sTorchPlacement);
	}
	
}
