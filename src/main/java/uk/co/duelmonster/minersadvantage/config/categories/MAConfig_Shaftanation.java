package uk.co.duelmonster.minersadvantage.config.categories;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import uk.co.duelmonster.minersadvantage.common.TorchPlacement;
import uk.co.duelmonster.minersadvantage.config.defaults.MAConfig_Defaults;

public class MAConfig_Shaftanation extends MAConfig_BaseCategory {
	
	// ====================================================================================================
	// = Config variables - !! NOT TO BE USED DIRECTLY !!
	// = Use the retrieval and modification functions below at all times!
	// ====================================================================================================
	private final IntValue					shaftLength;
	private final IntValue					shaftHeight;
	private final IntValue					shaftWidth;
	private final EnumValue<TorchPlacement>	torchPlacement;
	
	// ====================================================================================================
	// = Initialisation
	// ====================================================================================================
	public MAConfig_Shaftanation(ForgeConfigSpec.Builder builder) {
		
		enabled = builder
				.comment("Enable/Disable Shaftanation")
				.translation("minersadvantage.shaftanation.enabled")
				.define("enabled", MAConfig_Defaults.Shaftanation.enabled);
		
		shaftLength = builder
				.comment("The number of blocks long the shaft should be.")
				.translation("minersadvantage.shaftanation.shaft_l")
				.defineInRange("shaft_l", MAConfig_Defaults.Shaftanation.shaftLength, 4, 128);
		
		shaftHeight = builder
				.comment("The number of blocks high the shaft should be.")
				.translation("minersadvantage.shaftanation.shaft_h")
				.defineInRange("shaft_h", MAConfig_Defaults.Shaftanation.shaftHeight, 2, 16);
		
		shaftWidth = builder
				.comment("The number of blocks wide the shaft should be.")
				.translation("minersadvantage.shaftanation.shaft_w")
				.defineInRange("shaft_w", MAConfig_Defaults.Shaftanation.shaftWidth, 1, 16);
		
		torchPlacement = builder
				.comment("Where torches should be placed when Auto Illumination is enabled:  FLOOR (default), LEFT_WALL or RIGHT_WALL (2 blocks up from shaft floor).")
				.translation("minersadvantage.shaftanation.torch_placement")
				.defineEnum("torch_placement", MAConfig_Defaults.Shaftanation.torchPlacement);
	}
	
	// ====================================================================================================
	// = Config retrieval functions
	// ====================================================================================================
	
	/**
	 * @return shaftLength
	 */
	public int shaftLength() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceShaftanationSettings.get())
			return parentConfig.serverOverrides.shaftanation.shaftLength();
		
		return shaftLength.get();
	}
	
	/**
	 * @param shaftLength
	 *            Sets shaftLength
	 */
	public void setShaftLength(int value) {
		shaftLength.set(value);
	}
	
	/**
	 * @return shaftHeight
	 */
	public int shaftHeight() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceShaftanationSettings.get())
			return parentConfig.serverOverrides.shaftanation.shaftHeight();
		
		return shaftHeight.get();
	}
	
	/**
	 * @param shaftHeight
	 *            Sets shaftHeight
	 */
	public void setShaftHeight(int value) {
		shaftHeight.set(value);
	}
	
	/**
	 * @return shaftWidth
	 */
	public int shaftWidth() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceShaftanationSettings.get())
			return parentConfig.serverOverrides.shaftanation.shaftWidth();
		
		return shaftWidth.get();
	}
	
	/**
	 * @param shaftWidth
	 *            Sets shaftWidth
	 */
	public void setShaftWidth(int value) {
		shaftWidth.set(value);
	}
	
	/**
	 * @return torchPlacement
	 */
	public TorchPlacement torchPlacement() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceShaftanationSettings.get())
			return parentConfig.serverOverrides.shaftanation.torchPlacement();
		
		return torchPlacement.get();
	}
	
	/**
	 * @param torchPlacement
	 *            Sets torchPlacement
	 */
	public void setTorchPlacement(TorchPlacement value) {
		torchPlacement.set(value);
	}
	
	public void setTorchPlacement(String value) {
		torchPlacement.set(TorchPlacement.byName(value));
	}
	
}
