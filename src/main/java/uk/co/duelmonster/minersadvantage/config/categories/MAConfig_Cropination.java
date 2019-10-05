package uk.co.duelmonster.minersadvantage.config.categories;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import uk.co.duelmonster.minersadvantage.config.defaults.MAConfig_Defaults;

public class MAConfig_Cropination extends MAConfig_BaseCategory {

	// ====================================================================================================
	// = Config variables - !! NOT TO BE USED DIRECTLY !!
	// = Use the retrieval and modification functions below at all times!
	// ====================================================================================================
	private final BooleanValue harvestSeeds;

	// ====================================================================================================
	// = Initialisation
	// ====================================================================================================
	public MAConfig_Cropination(ForgeConfigSpec.Builder builder) {

		enabled = builder
				.comment("Enable/Disable Cropination")
				.translation("minersadvantage.cropination.enabled")
				.define("enabled", MAConfig_Defaults.Cropination.enabled);

		harvestSeeds = builder
				.comment("Tells Cropination to havest the seeds along with the Crop.")
				.translation("minersadvantage.cropination.harvest_seeds")
				.define("harvest_seeds", MAConfig_Defaults.Cropination.harvestSeeds);
	}

	// ====================================================================================================
	// = Config retrieval functions
	// ====================================================================================================

	/**
	 * @return harvestSeeds
	 */
	public boolean harvestSeeds() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceCropinationSettings.get())
			return parentConfig.serverOverrides.cropination.harvestSeeds();

		return harvestSeeds.get();
	}

	/**
	 * @param harvestSeeds Sets harvestSeeds
	 */
	public void setHarvestSeeds(boolean value) { harvestSeeds.set(value); }

}
