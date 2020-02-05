package uk.co.duelmonster.minersadvantage.config.categories;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import uk.co.duelmonster.minersadvantage.config.defaults.MAConfig_Defaults;

public class MAConfig_Illumination extends MAConfig_BaseCategory {

	// ====================================================================================================
	// = Config variables - !! NOT TO BE USED DIRECTLY !!
	// = Use the retrieval and modification functions below at all times!
	// ====================================================================================================
	private final BooleanValue useBlockLight;
	private final IntValue lowestLightLevel;

	// ====================================================================================================
	// = Initialisation
	// ====================================================================================================
	public MAConfig_Illumination(ForgeConfigSpec.Builder builder) {

		enabled = builder
				.comment("Enable/Disable Illumination")
				.translation("minersadvantage.illumination.enabled")
				.define("enabled", MAConfig_Defaults.Illumination.enabled);

		useBlockLight = builder
				.comment("Use the Block light level to detect the Lowest Level of light.", 
						"If disabled the Sky light level will be used, which takes the time of day into account.")
				.translation("minersadvantage.illumination.use_block_light")
				.define("use_block_light", MAConfig_Defaults.Illumination.useBlockLight);

		lowestLightLevel = builder
				.comment("The Lowest Level of light allowed before a torch is placed.")
				.translation("minersadvantage.illumination.light_level")
				.defineInRange("light_level", MAConfig_Defaults.Illumination.lowestLightLevel, 0, 16);
	}

	// ====================================================================================================
	// = Config retrieval functions
	// ====================================================================================================

	/**
	 * @return useBlockLight
	 */
	public boolean useBlockLight() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceIlluminationSettings.get())
			return parentConfig.serverOverrides.illumination.useBlockLight();

		return useBlockLight.get();
	}

	/**
	 * @param useBlockLight Sets useBlockLight
	 */
	public void setUseBlockLight(boolean value) { useBlockLight.set(value); }

	/**
	 * @return lowestLightLevel
	 */
	public int lowestLightLevel() {
		if (parentConfig != null && parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceIlluminationSettings.get())
			return parentConfig.serverOverrides.illumination.lowestLightLevel();

		return lowestLightLevel.get();
	}

	/**
	 * @param lowestLightLevel Sets lowestLightLevel
	 */
	public void setLowestLightLevel(int value) { lowestLightLevel.set(value); }

}
