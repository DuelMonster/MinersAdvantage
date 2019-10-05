package uk.co.duelmonster.minersadvantage.config.categories;

import java.util.List;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import uk.co.duelmonster.minersadvantage.config.defaults.MAConfig_Defaults;

public class MAConfig_Veination extends MAConfig_BaseCategory {

	// ====================================================================================================
	// = Config variables - !! NOT TO BE USED DIRECTLY !!
	// = Use the retrieval and modification functions below at all times!
	// ====================================================================================================
	private final ConfigValue<List<String>> ores;

	// ====================================================================================================
	// = Initialisation
	// ====================================================================================================
	public MAConfig_Veination(ForgeConfigSpec.Builder builder) {

		enabled = builder
				.comment("Enable/Disable Veination")
				.translation("minersadvantage.veination.enabled")
				.define("enabled", MAConfig_Defaults.Veination.enabled);

		ores = builder
				.comment("List of Ore Block IDs.")
				.translation("minersadvantage.veination.ores")
				.define("ores", MAConfig_Defaults.Veination.ores);
	}

	// ====================================================================================================
	// = Config retrieval functions
	// ====================================================================================================

	/**
	 * @return Veination Ores
	 */
	public List<String> ores() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceVeinationSettings.get())
			return parentConfig.serverOverrides.veination.ores();

		return ores.get();
	}

	/**
	 * @param ores Sets Veination ores
	 */
	public void setOres(List<String> value) { ores.set(value); }

}
