package uk.co.duelmonster.minersadvantage.config.categories;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import uk.co.duelmonster.minersadvantage.config.defaults.MAConfig_Defaults;

public class MAConfig_Pathanation extends MAConfig_BaseCategory {

	// ====================================================================================================
	// = Config variables - !! NOT TO BE USED DIRECTLY !!
	// = Use the retrieval and modification functions below at all times!
	// ====================================================================================================
	private final IntValue pathWidth;
	private final IntValue pathLength;

	// ====================================================================================================
	// = Initialisation
	// ====================================================================================================
	public MAConfig_Pathanation(ForgeConfigSpec.Builder builder) {

		enabled = builder
				.comment("Enable/Disable Pathanation")
				.translation("minersadvantage.pathanation.enabled")
				.define("enabled", MAConfig_Defaults.Pathanation.enabled);

		pathWidth = builder
				.comment("How wide to make the Path.")
				.translation("minersadvantage.pathanation.path_width")
				.defineInRange("path_width", MAConfig_Defaults.Pathanation.pathWidth, 1, 16);

		pathLength = builder
				.comment("How long to make the path.")
				.translation("minersadvantage.pathanation.path_length")
				.defineInRange("path_length", MAConfig_Defaults.Pathanation.pathLength, 1, 64);
	}

	// ====================================================================================================
	// = Config retrieval functions
	// ====================================================================================================

	/**
	 * @return pathWidth
	 */
	public int pathWidth() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceExcavationSettings.get())
			return parentConfig.serverOverrides.pathanation.pathWidth();

		return pathWidth.get();
	}

	/**
	 * @param pathWidth Sets pathWidth
	 */
	public void setPathWidth(int value) { pathWidth.set(value); }

	/**
	 * @return pathLength
	 */
	public int pathLength() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceExcavationSettings.get())
			return parentConfig.serverOverrides.pathanation.pathLength();

		return pathLength.get();
	}

	/**
	 * @param pathLength Sets pathLength
	 */
	public void setPathLength(int value) { pathLength.set(value); }

}
