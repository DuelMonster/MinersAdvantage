package uk.co.duelmonster.minersadvantage.config.categories;

import java.util.List;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.BooleanValue;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import uk.co.duelmonster.minersadvantage.config.defaults.MAConfig_Defaults;

public class MAConfig_Captivation extends MAConfig_BaseCategory {

	// ====================================================================================================
	// = Config variables - !! NOT TO BE USED DIRECTLY !!
	// = Use the retrieval and modification functions below at all times!
	// ====================================================================================================
	private final BooleanValue allowInGUI;
	private final DoubleValue radiusHorizontal;
	private final DoubleValue radiusVertical;
	private final BooleanValue isWhitelist;
	private final BooleanValue unconditionalBlacklist;
	private final ConfigValue<List<? extends String>>	blacklist;

	// ====================================================================================================
	// = Initialisation
	// ====================================================================================================
	public MAConfig_Captivation(ForgeConfigSpec.Builder builder) {

		enabled = builder
				.comment("Enable/Disable Captivation")
				.translation("minersadvantage.captivation.enabled")
				.define("enabled", MAConfig_Defaults.Captivation.enabled);

		allowInGUI = builder
				.comment("Allows/Disallows Captivation to pick up items while in an in-game GUI.")
				.translation("minersadvantage.captivation.allow_in_gui")
				.define("allow_in_gui", MAConfig_Defaults.Captivation.allowInGUI);

		radiusHorizontal = builder
				.comment("The Radius of Horizontal blocks to check for items.")
				.translation("minersadvantage.captivation.h_radius")
				.defineInRange("h_radius", MAConfig_Defaults.Captivation.radiusHorizontal, 0, 128);

		radiusVertical = builder
				.comment("The Radius of Vertical blocks to check for items.")
				.translation("minersadvantage.captivation.v_radius")
				.defineInRange("v_radius", MAConfig_Defaults.Captivation.radiusVertical, 0, 128);

		isWhitelist = builder
				.comment("If 'true' Captivation will only pickup items in the blacklist. If False, Captivation will pick up all items except those in the blacklist.")
				.translation("minersadvantage.captivation.is_whitelist")
				.define("is_whitelist", MAConfig_Defaults.Captivation.isWhitelist);

		unconditionalBlacklist = builder
				.comment("If 'true' Captivation will NEVER allow items in the blacklist to be picked up, even when walking over them.")
				.translation("minersadvantage.captivation.unconditional_blacklist")
				.define("unconditional_blacklist", MAConfig_Defaults.Captivation.unconditionalBlacklist);
		
		blacklist = builder
				.comment("List of blacklisted Item IDs.")
				.translation("minersadvantage.captivation.blacklist")
				.defineList("blacklist", MAConfig_Defaults.Captivation.blacklist, obj -> obj instanceof String);

	}

	// ====================================================================================================
	// = Config retrieval functions
	// ====================================================================================================

	/**
	 * @return allowInGUI
	 */
	public boolean allowInGUI() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceCaptivationSettings.get())
			return parentConfig.serverOverrides.captivation.allowInGUI();

		return allowInGUI.get();
	}

	/**
	 * @param allowInGUI Sets allowInGUI
	 */
	public void setAllowInGUI(boolean value) {
		allowInGUI.set(value);
	}

	/**
	 * @return radiusHorizontal
	 */
	public double radiusHorizontal() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceCaptivationSettings.get())
			return parentConfig.serverOverrides.captivation.radiusHorizontal();

		return radiusHorizontal.get();
	}

	/**
	 * @param radiusHorizontal Sets radiusHorizontal
	 */
	public void setRadiusHorizontal(double value) {
		radiusHorizontal.set(value);
	}

	/**
	 * @return radiusVertical
	 */
	public double radiusVertical() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceCaptivationSettings.get())
			return parentConfig.serverOverrides.captivation.radiusVertical();

		return radiusVertical.get();
	}

	/**
	 * @param radiusVertical Sets radiusVertical
	 */
	public void setRadiusVertical(double value) {
		radiusVertical.set(value);
	}

	/**
	 * @return isWhitelist
	 */
	public boolean isWhitelist() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceCaptivationSettings.get())
			return parentConfig.serverOverrides.captivation.isWhitelist();

		return isWhitelist.get();
	}

	/**
	 * @param isWhitelist Sets isWhitelist
	 */
	public void setIsWhitelist(boolean value) {
		isWhitelist.set(value);
	}

	/**
	 * @return unconditionalBlacklist
	 */
	public boolean unconditionalBlacklist() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceCaptivationSettings.get())
			return parentConfig.serverOverrides.captivation.unconditionalBlacklist();

		return unconditionalBlacklist.get();
	}

	/**
	 * @param unconditionalBlacklist Sets unconditionalBlacklist
	 */
	public void setUnconditionalBlacklist(boolean value) {
		unconditionalBlacklist.set(value);
	}

	/**
	 * @return blacklist
	 */
	public List<? extends String> blacklist() {
		if (parentConfig.serverOverrides != null && parentConfig.serverOverrides.enforceCaptivationSettings.get())
			return parentConfig.serverOverrides.captivation.blacklist();

		return blacklist.get();
	}

	/**
	 * @param blacklist Sets blacklist
	 */
	public void setBlacklist(List<? extends String> value) {
		blacklist.set(value);
	}

}
