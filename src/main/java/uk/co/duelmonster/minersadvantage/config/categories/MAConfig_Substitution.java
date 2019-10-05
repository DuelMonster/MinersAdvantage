package uk.co.duelmonster.minersadvantage.config.categories;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import uk.co.duelmonster.minersadvantage.config.defaults.MAConfig_Defaults;

public class MAConfig_Substitution extends MAConfig_BaseCategory {

	// ====================================================================================================
	// = Config variables - !! NOT TO BE USED DIRECTLY !!
	// = Use the retrieval and modification functions below at all times!
	// ====================================================================================================
	private final BooleanValue switchBack;
	private final BooleanValue favourSilkTouch;
	private final BooleanValue favourFortune;
	private final BooleanValue ignoreIfValidTool;
	private final BooleanValue ignorePassiveMobs;

	// ====================================================================================================
	// = Initialisation
	// ====================================================================================================
	public MAConfig_Substitution(ForgeConfigSpec.Builder builder) {

		enabled = builder
				.comment("Enable/Disable Substitution")
				.translation("minersadvantage.substitution.enabled")
				.define("enabled", MAConfig_Defaults.Substitution.enabled);

		switchBack = builder
				.comment("If 'true' will switch back to the previously selected item.")
				.translation("minersadvantage.substitution.switchback")
				.define("switchback", MAConfig_Defaults.Substitution.switchBack);

		favourSilkTouch = builder
				.comment("Favour a SilkTouch tool over any other tool.")
				.translation("minersadvantage.substitution.favour_silk")
				.define("favour_silk", MAConfig_Defaults.Substitution.favourSilkTouch);

		favourFortune = builder
				.comment("Favour a Fortune tool over any other tool.")
				.translation("minersadvantage.substitution.favour_fortune")
				.define("favour_fortune", MAConfig_Defaults.Substitution.favourFortune);

		ignoreIfValidTool = builder
				.comment("Ignore other tools when deciding to substitute if the current Tool is valid for the block.")
				.translation("minersadvantage.substitution.ignore_valid")
				.define("ignore_valid", MAConfig_Defaults.Substitution.ignoreIfValidTool);

		ignorePassiveMobs = builder
				.comment("Ignore Passive Mobs when deciding to substitute to a weapon.")
				.translation("minersadvantage.substitution.ignore_passive")
				.define("ignore_passive", MAConfig_Defaults.Substitution.ignorePassiveMobs);
	}

	// ====================================================================================================
	// = Config retrieval functions
	// ====================================================================================================

	/**
	 * @return switchBack
	 */
	public boolean switchBack() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceSubstitutionSettings.get())
			return parentConfig.serverOverrides.substitution.switchBack();

		return switchBack.get();
	}

	/**
	 * @param switchBack Sets switchBack
	 */
	public void setSwitchBack(boolean value) { switchBack.set(value); }

	/**
	 * @return favourSilkTouch
	 */
	public boolean favourSilkTouch() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceSubstitutionSettings.get())
			return parentConfig.serverOverrides.substitution.favourSilkTouch();

		return favourSilkTouch.get();
	}

	/**
	 * @param favourSilkTouch Sets favourSilkTouch
	 */
	public void setFavourSilkTouch(boolean value) { favourSilkTouch.set(value); }

	/**
	 * @return favourFortune
	 */
	public boolean favourFortune() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceSubstitutionSettings.get())
			return parentConfig.serverOverrides.substitution.favourFortune();

		return favourFortune.get();
	}

	/**
	 * @param favourFortune Sets favourFortune
	 */
	public void setFavourFortune(boolean value) { favourFortune.set(value); }

	/**
	 * @return ignoreIfValidTool
	 */
	public boolean ignoreIfValidTool() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceSubstitutionSettings.get())
			return parentConfig.serverOverrides.substitution.ignoreIfValidTool();

		return ignoreIfValidTool.get();
	}

	/**
	 * @param ignoreIfValidTool Sets ignoreIfValidTool
	 */
	public void setIgnoreIfValidTool(boolean value) { ignoreIfValidTool.set(value); }

	/**
	 * @return ignorePassiveMobs
	 */
	public boolean ignorePassiveMobs() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceSubstitutionSettings.get())
			return parentConfig.serverOverrides.substitution.ignorePassiveMobs();

		return ignorePassiveMobs.get();
	}

	/**
	 * @param ignorePassiveMobs Sets ignorePassiveMobs
	 */
	public void setIgnorePassiveMobs(boolean value) { ignorePassiveMobs.set(value); }

}
