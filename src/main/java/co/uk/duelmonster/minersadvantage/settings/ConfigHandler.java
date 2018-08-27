package co.uk.duelmonster.minersadvantage.settings;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;

import co.uk.duelmonster.minersadvantage.MinersAdvantage;
import co.uk.duelmonster.minersadvantage.common.Constants;
import co.uk.duelmonster.minersadvantage.common.Functions;
import co.uk.duelmonster.minersadvantage.common.JsonHelper;
import co.uk.duelmonster.minersadvantage.config.MAConfig;
import co.uk.duelmonster.minersadvantage.config.MAServerConfig;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

public class ConfigHandler {
	private static Configuration	_config		= null;
	private static MAConfig			settings	= null;
	
	public static void initConfigs(Configuration config) {
		if (config == null) {
			MinersAdvantage.logger.log(Level.ERROR, "Config attempted to be loaded before it was initialised!");
			return;
		}
		
		if (MinersAdvantage.proxy.isClient())
			settings = MAConfig.get();
		else
			settings = MAServerConfig.get();
		
		_config = config;
		
		readConfig();
	}
	
	public static void readConfig() {
		_config.load();
		
		if (!MinersAdvantage.proxy.isClient())
			getServerSettings();
		
		getCommonSettings();
		getCaptivationSettings();
		getCropinationSettings();
		getExcavationSettings();
		getIlluminationSettings();
		getLumbinationSettings();
		getPathanationSettings();
		getShaftanationSettings();
		getSubstitutionSettings();
		getVeinationSettings();
		
		save();
	}
	
	public static Configuration getConfig() {
		return _config;
	}
	
	public static void save() {
		if (_config != null && _config.hasChanged()) {
			// Save the config
			_config.save();
			// Refresh the MAConfig data
			readConfig();
		}
	}
	
	public static ConfigCategory getCategory(String category) {
		if (_config != null)
			return _config.getCategory(category);
		return null;
	}
	
	public static void setValue(String category, String key, Object value) {
		if (_config != null)
			if (value instanceof Boolean)
				getCategory(category).get(Functions.localize(category + "." + key)).setValue((Boolean) value);
			else if (value instanceof String)
				getCategory(category).get(Functions.localize(category + "." + key)).setValue((String) value);
			else if (value instanceof String[])
				getCategory(category).get(Functions.localize(category + "." + key)).setValues((String[]) value);
	}
	
	public static boolean isOldConfig() {
		return (_config.getLoadedConfigVersion() == null || Float.parseFloat(_config.getLoadedConfigVersion()) < MAConfig.CONVERSION_VERSION);
	}
	
	/**
	 * This function exists in order to convert The old JSON formated list elements into the more user friendly String[]
	 * format. It compares the version number of the config file being loaded and converts if it is less than the
	 * MAConfig.CONVERSION_VERSION or if doesn't have a version.
	 * 
	 * @param sCategory
	 * @param sLangKey
	 * @param _default
	 * @return String[]
	 */
	private static String[] getStringArray(String sCategory, String sLangKey, String[] _default) {
		if (isOldConfig()) {
			String json = _config.getString(Functions.localize(sCategory + "." + sLangKey),
					sCategory, "",
					Functions.localize(sCategory + "." + sLangKey + ".desc"));
			
			String[] empty = {};
			_default = (json.isEmpty() || !json.startsWith("{") ? empty : JsonHelper.toStringList(JsonHelper.ParseObject(json)));
			
			_config.getCategory(sCategory).remove(Functions.localize(sCategory + "." + sLangKey));
		}
		
		return _config.getStringList(Functions.localize(sCategory + "." + sLangKey),
				sCategory, _default,
				Functions.localize(sCategory + "." + sLangKey + ".desc"));
	}
	
	private static void getCommonSettings() {
		settings.common.setGatherDrops(_config.getBoolean(Functions.localize(Constants.COMMON_ID + ".gather_drops"), Constants.COMMON_ID, MAConfig.defaults.common.bGatherDrops(), Functions.localize(Constants.COMMON_ID + ".gather_drops.desc")));
		settings.common.setAutoIlluminate(_config.getBoolean(Functions.localize(Constants.COMMON_ID + ".auto_illum"), Constants.COMMON_ID, MAConfig.defaults.common.bAutoIlluminate(), Functions.localize(Constants.COMMON_ID + ".auto_illum.desc")));
		settings.common.setMineVeins(_config.getBoolean(Functions.localize(Constants.COMMON_ID + ".mine_veins"), Constants.COMMON_ID, MAConfig.defaults.common.bMineVeins(), Functions.localize(Constants.COMMON_ID + ".mine_veins.desc")));
		settings.common.setBlocksPerTick(_config.getInt(Functions.localize(Constants.COMMON_ID + ".blocks_per_tick"), Constants.COMMON_ID, MAConfig.defaults.common.iBlocksPerTick(), 1, 16, Functions.localize(Constants.COMMON_ID + ".blocks_per_tick.desc")));
		settings.common.setEnableTickDelay(_config.getBoolean(Functions.localize(Constants.COMMON_ID + ".enable_tick_delay"), Constants.COMMON_ID, MAConfig.defaults.common.bEnableTickDelay(), Functions.localize(Constants.COMMON_ID + ".enable_tick_delay.desc")));
		settings.common.setTickDelay(_config.getInt(Functions.localize(Constants.COMMON_ID + ".tick_delay"), Constants.COMMON_ID, MAConfig.defaults.common.iTickDelay(), 1, 20, Functions.localize(Constants.COMMON_ID + ".tick_delay.desc")));
		settings.common.setBlockRadius(_config.getInt(Functions.localize(Constants.COMMON_ID + ".radius"), Constants.COMMON_ID, MAConfig.defaults.common.iBlockRadius(), Constants.MIN_BLOCKRADIUS, Constants.MAX_BLOCKRADIUS, Functions.localize(Constants.COMMON_ID + ".radius.desc")));
		settings.common.setBlockLimit(_config.getInt(Functions.localize(Constants.COMMON_ID + ".limit"), Constants.COMMON_ID, MAConfig.defaults.common.iBlockLimit(), ((Constants.MIN_BLOCKRADIUS * Constants.MIN_BLOCKRADIUS) * Constants.MIN_BLOCKRADIUS), Constants.MAX_BLOCKLIMIT, Functions.localize(Constants.COMMON_ID + ".limit.desc")));
		// settings.setBreakAtToolSpeeds(_config.getBoolean(Functions.localize(Constants.COMMON_ID +
		// ".break_at_tool_speeds"), Constants.COMMON_ID, MAConfig.defaults.common.bBreakAtToolSpeeds(),
		// Functions.localize(Constants.COMMON_ID + ".break_at_tool_speeds.desc")));
		settings.common.setDisableParticleEffects(_config.getBoolean(Functions.localize(Constants.COMMON_ID + ".disable_particle_effects"), Constants.COMMON_ID, MAConfig.defaults.common.bDisableParticleEffects(), Functions.localize(Constants.COMMON_ID + ".disable_particle_effects.desc")));
		
		List<String> order = new ArrayList<String>(6);
		order.add(Functions.localize(Constants.COMMON_ID + ".gather_drops"));
		order.add(Functions.localize(Constants.COMMON_ID + ".auto_illum"));
		order.add(Functions.localize(Constants.COMMON_ID + ".mine_veins"));
		order.add(Functions.localize(Constants.COMMON_ID + ".enable_tick_delay"));
		order.add(Functions.localize(Constants.COMMON_ID + ".tick_delay"));
		order.add(Functions.localize(Constants.COMMON_ID + ".blocks_per_tick"));
		order.add(Functions.localize(Constants.COMMON_ID + ".radius"));
		order.add(Functions.localize(Constants.COMMON_ID + ".limit"));
		// order.add(Functions.localize(Constants.COMMON_ID + ".break_at_tool_speeds"));
		order.add(Functions.localize(Constants.COMMON_ID + ".disable_particle_effects"));
		
		_config.setCategoryPropertyOrder(Constants.COMMON_ID, order);
	}
	
	private static void getServerSettings() {
		MAServerConfig serverSettings = (MAServerConfig) settings;
		
		serverSettings.server.bOverrideFeatureEnablement = _config.getBoolean(Functions.localize(Constants.SERVER_ID + ".override_client"), Constants.SERVER_ID, MAServerConfig.defaults.server.bOverrideFeatureEnablement, Functions.localize(Constants.SERVER_ID + ".override_client.desc"));
		serverSettings.server.bEnforceCommonSettings = _config.getBoolean(Functions.localize(Constants.SERVER_ID + ".enforce_common_settings"), Constants.SERVER_ID, MAServerConfig.defaults.server.bEnforceCommonSettings, Functions.localize(Constants.SERVER_ID + ".enforce_common_settings.desc"));
		serverSettings.server.bEnforceCaptivationSettings = _config.getBoolean(Functions.localize(Constants.SERVER_ID + ".enforce_captivation_settings"), Constants.SERVER_ID, MAServerConfig.defaults.server.bEnforceCaptivationSettings, Functions.localize(Constants.SERVER_ID + ".enforce_captivation_settings.desc"));
		serverSettings.server.bEnforceCropinationSettings = _config.getBoolean(Functions.localize(Constants.SERVER_ID + ".enforce_cropination_settings"), Constants.SERVER_ID, MAServerConfig.defaults.server.bEnforceCropinationSettings, Functions.localize(Constants.SERVER_ID + ".enforce_cropination_settings.desc"));
		serverSettings.server.bEnforceExcavationSettings = _config.getBoolean(Functions.localize(Constants.SERVER_ID + ".enforce_excavation_settings"), Constants.SERVER_ID, MAServerConfig.defaults.server.bEnforceExcavationSettings, Functions.localize(Constants.SERVER_ID + ".enforce_excavation_settings.desc"));
		serverSettings.server.bEnforceIlluminationSettings = _config.getBoolean(Functions.localize(Constants.SERVER_ID + ".enforce_illumination_settings"), Constants.SERVER_ID, MAServerConfig.defaults.server.bEnforceIlluminationSettings, Functions.localize(Constants.SERVER_ID + ".enforce_illumination_settings.desc"));
		serverSettings.server.bEnforceLumbinationSettings = _config.getBoolean(Functions.localize(Constants.SERVER_ID + ".enforce_lumbination_settings"), Constants.SERVER_ID, MAServerConfig.defaults.server.bEnforceLumbinationSettings, Functions.localize(Constants.SERVER_ID + ".enforce_lumbination_settings.desc"));
		serverSettings.server.bEnforceShaftanationSettings = _config.getBoolean(Functions.localize(Constants.SERVER_ID + ".enforce_shaftanation_settings"), Constants.SERVER_ID, MAServerConfig.defaults.server.bEnforceShaftanationSettings, Functions.localize(Constants.SERVER_ID + ".enforce_shaftanation_settings.desc"));
		serverSettings.server.bEnforceSubstitutionSettings = _config.getBoolean(Functions.localize(Constants.SERVER_ID + ".enforce_substitution_settings"), Constants.SERVER_ID, MAServerConfig.defaults.server.bEnforceSubstitutionSettings, Functions.localize(Constants.SERVER_ID + ".enforce_substitution_settings.desc"));
		serverSettings.server.bEnforceVeinationSettings = _config.getBoolean(Functions.localize(Constants.SERVER_ID + ".enforce_veination_settings"), Constants.SERVER_ID, MAServerConfig.defaults.server.bEnforceVeinationSettings, Functions.localize(Constants.SERVER_ID + ".enforce_veination_settings.desc"));
		
		List<String> order = new ArrayList<String>(2);
		order.add(Functions.localize(Constants.SERVER_ID + ".override_client"));
		order.add(Functions.localize(Constants.SERVER_ID + ".enforce_common_settings"));
		order.add(Functions.localize(Constants.SERVER_ID + ".enforce_captivation_settings"));
		order.add(Functions.localize(Constants.SERVER_ID + ".enforce_cropination_settings"));
		order.add(Functions.localize(Constants.SERVER_ID + ".enforce_excavation_settings"));
		order.add(Functions.localize(Constants.SERVER_ID + ".enforce_illumination_settings"));
		order.add(Functions.localize(Constants.SERVER_ID + ".enforce_lumbination_settings"));
		order.add(Functions.localize(Constants.SERVER_ID + ".enforce_shaftanation_settings"));
		order.add(Functions.localize(Constants.SERVER_ID + ".enforce_substitution_settings"));
		order.add(Functions.localize(Constants.SERVER_ID + ".enforce_veination_settings"));
		
		_config.setCategoryPropertyOrder(Constants.SERVER_ID, order);
	}
	
	private static void getCaptivationSettings() {
		settings.captivation.setEnabled(_config.getBoolean(Functions.localize(Constants.CAPTIVATION_ID + ".enabled"), Constants.CAPTIVATION_ID, MAConfig.defaults.captivation.bEnabled(), Functions.localize(Constants.CAPTIVATION_ID + ".enabled.desc")));
		settings.captivation.setAllowInGUI(_config.getBoolean(Functions.localize(Constants.CAPTIVATION_ID + ".allow_in_gui"), Constants.CAPTIVATION_ID, MAConfig.defaults.captivation.bAllowInGUI(), Functions.localize(Constants.CAPTIVATION_ID + ".allow_in_gui.desc")));
		settings.captivation.setRadiusHorizontal(_config.getFloat(Functions.localize(Constants.CAPTIVATION_ID + ".h_radius"), Constants.CAPTIVATION_ID, (float) MAConfig.defaults.captivation.radiusHorizontal(), 0.0F, 128.0F, Functions.localize(Constants.CAPTIVATION_ID + ".h_radius.desc")));
		settings.captivation.setRadiusVertical(_config.getFloat(Functions.localize(Constants.CAPTIVATION_ID + ".v_radius"), Constants.CAPTIVATION_ID, (float) MAConfig.defaults.captivation.radiusVertical(), 0.0F, 128.0F, Functions.localize(Constants.CAPTIVATION_ID + ".v_radius.desc")));
		settings.captivation.setIsWhitelist(_config.getBoolean(Functions.localize(Constants.CAPTIVATION_ID + ".is_whitelist"), Constants.CAPTIVATION_ID, MAConfig.defaults.captivation.bIsWhitelist(), Functions.localize(Constants.CAPTIVATION_ID + ".is_whitelist.desc")));
		
		settings.captivation.setBlacklist(getStringArray(Constants.CAPTIVATION_ID, "blacklist", MAConfig.defaults.captivation.blacklist()));
		
		List<String> order = new ArrayList<String>(6);
		order.add(Functions.localize(Constants.CAPTIVATION_ID + ".enabled"));
		order.add(Functions.localize(Constants.CAPTIVATION_ID + ".allow_in_gui"));
		order.add(Functions.localize(Constants.CAPTIVATION_ID + ".h_radius"));
		order.add(Functions.localize(Constants.CAPTIVATION_ID + ".v_radius"));
		order.add(Functions.localize(Constants.CAPTIVATION_ID + ".is_whitelist"));
		order.add(Functions.localize(Constants.CAPTIVATION_ID + ".blacklist"));
		
		_config.setCategoryPropertyOrder(Constants.CAPTIVATION_ID, order);
	}
	
	private static void getCropinationSettings() {
		settings.cropination.setEnabled(_config.getBoolean(Functions.localize(Constants.CROPINATION_ID + ".enabled"), Constants.CROPINATION_ID, MAConfig.defaults.cropination.bEnabled(), Functions.localize(Constants.CROPINATION_ID + ".enabled.desc")));
		settings.cropination.setHarvestSeeds(_config.getBoolean(Functions.localize(Constants.CROPINATION_ID + ".harvest_seeds"), Constants.CROPINATION_ID, MAConfig.defaults.cropination.bHarvestSeeds(), Functions.localize(Constants.CROPINATION_ID + ".harvest_seeds.desc")));
		
		List<String> order = new ArrayList<String>(2);
		order.add(Functions.localize(Constants.CROPINATION_ID + ".enabled"));
		order.add(Functions.localize(Constants.CROPINATION_ID + ".harvest_seeds"));
		
		_config.setCategoryPropertyOrder(Constants.CROPINATION_ID, order);
	}
	
	private static void getExcavationSettings() {
		settings.excavation.setEnabled(_config.getBoolean(Functions.localize(Constants.EXCAVATION_ID + ".enabled"), Constants.EXCAVATION_ID, MAConfig.defaults.excavation.bEnabled(), Functions.localize(Constants.EXCAVATION_ID + ".enabled.desc")));
		settings.excavation.setToggleMode(_config.getBoolean(Functions.localize(Constants.EXCAVATION_ID + ".toggle_mode"), Constants.EXCAVATION_ID, MAConfig.defaults.excavation.bToggleMode(), Functions.localize(Constants.EXCAVATION_ID + ".toggle_mode.desc")));
		settings.excavation.setIgnoreBlockVariants(_config.getBoolean(Functions.localize(Constants.EXCAVATION_ID + ".ignore_variants"), Constants.EXCAVATION_ID, MAConfig.defaults.excavation.bIgnoreBlockVariants(), Functions.localize(Constants.EXCAVATION_ID + ".ignore_variants.desc")));
		settings.excavation.setIsToolWhitelist(_config.getBoolean(Functions.localize(Constants.EXCAVATION_ID + ".is_tool_whitelist"), Constants.EXCAVATION_ID, MAConfig.defaults.excavation.bIsToolWhitelist(), Functions.localize(Constants.EXCAVATION_ID + ".is_tool_whitelist.desc")));
		settings.excavation.setIsBlockWhitelist(_config.getBoolean(Functions.localize(Constants.EXCAVATION_ID + ".is_block_whitelist"), Constants.EXCAVATION_ID, MAConfig.defaults.excavation.bIsBlockWhitelist(), Functions.localize(Constants.EXCAVATION_ID + ".is_block_whitelist.desc")));
		
		settings.excavation.setToolBlacklist(getStringArray(Constants.EXCAVATION_ID, "tool_blacklist", MAConfig.defaults.excavation.toolBlacklist()));
		settings.excavation.setBlockBlacklist(getStringArray(Constants.EXCAVATION_ID, "block_blacklist", MAConfig.defaults.excavation.blockBlacklist()));
		
		// Move the Pathanation settings to their own category if required
		if (isOldConfig()) {
			_config.moveProperty(Constants.EXCAVATION_ID, Functions.localize(Constants.PATHANATION_ID + ".path_width"), Constants.PATHANATION_ID);
			_config.moveProperty(Constants.EXCAVATION_ID, Functions.localize(Constants.PATHANATION_ID + ".path_length"), Constants.PATHANATION_ID);
		}
		
		List<String> order = new ArrayList<String>(11);
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".enabled"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".toggle_mode"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".ignore_variants"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".is_tool_whitelist"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".tool_blacklist"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".is_block_whitelist"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".block_blacklist"));
		
		_config.setCategoryPropertyOrder(Constants.EXCAVATION_ID, order);
	}
	
	private static void getIlluminationSettings() {
		settings.illumination.setEnabled(_config.getBoolean(Functions.localize(Constants.ILLUMINATION_ID + ".enabled"), Constants.ILLUMINATION_ID, MAConfig.defaults.illumination.bEnabled(), Functions.localize(Constants.ILLUMINATION_ID + ".enabled.desc")));
		settings.illumination.setUseBlockLight(_config.getBoolean(Functions.localize(Constants.ILLUMINATION_ID + ".use_block_light"), Constants.ILLUMINATION_ID, MAConfig.defaults.illumination.bUseBlockLight(), Functions.localize(Constants.ILLUMINATION_ID + ".use_block_light.desc")));
		settings.illumination.setLowestLightLevel(_config.getInt(Functions.localize(Constants.ILLUMINATION_ID + ".light_level"), Constants.ILLUMINATION_ID, MAConfig.defaults.illumination.iLowestLightLevel(), 0, 16, Functions.localize(Constants.ILLUMINATION_ID + ".light_level.desc")));
		
		List<String> order = new ArrayList<String>(2);
		order.add(Functions.localize(Constants.ILLUMINATION_ID + ".enabled"));
		order.add(Functions.localize(Constants.ILLUMINATION_ID + ".use_block_light"));
		order.add(Functions.localize(Constants.ILLUMINATION_ID + ".light_level"));
		
		_config.setCategoryPropertyOrder(Constants.ILLUMINATION_ID, order);
	}
	
	private static void getLumbinationSettings() {
		settings.lumbination.setEnabled(_config.getBoolean(Functions.localize(Constants.LUMBINATION_ID + ".enabled"), Constants.LUMBINATION_ID, MAConfig.defaults.lumbination.bEnabled(), Functions.localize(Constants.LUMBINATION_ID + ".enabled.desc")));
		settings.lumbination.setChopTreeBelow(_config.getBoolean(Functions.localize(Constants.LUMBINATION_ID + ".chop_below"), Constants.LUMBINATION_ID, MAConfig.defaults.lumbination.bChopTreeBelow(), Functions.localize(Constants.LUMBINATION_ID + ".chop_below.desc")));
		settings.lumbination.setDestroyLeaves(_config.getBoolean(Functions.localize(Constants.LUMBINATION_ID + ".destroy_leaves"), Constants.LUMBINATION_ID, MAConfig.defaults.lumbination.bDestroyLeaves(), Functions.localize(Constants.LUMBINATION_ID + ".destroy_leaves.desc")));
		settings.lumbination.setLeavesAffectDurability(_config.getBoolean(Functions.localize(Constants.LUMBINATION_ID + ".leaves_affect_durability"), Constants.LUMBINATION_ID, MAConfig.defaults.lumbination.bLeavesAffectDurability(), Functions.localize(Constants.LUMBINATION_ID + ".leaves_affect_durability.desc")));
		settings.lumbination.setLeafRange(_config.getInt(Functions.localize(Constants.LUMBINATION_ID + ".leaf_range"), Constants.LUMBINATION_ID, MAConfig.defaults.lumbination.iLeafRange(), 0, 16, Functions.localize(Constants.LUMBINATION_ID + ".leaf_range.desc")));
		settings.lumbination.setTrunkRange(_config.getInt(Functions.localize(Constants.LUMBINATION_ID + ".trunk_range"), Constants.LUMBINATION_ID, MAConfig.defaults.lumbination.iTrunkRange(), 8, 128, Functions.localize(Constants.LUMBINATION_ID + ".trunk_range.desc")));
		
		settings.lumbination.setLogs(getStringArray(Constants.LUMBINATION_ID, "logs", MAConfig.defaults.lumbination.logs()));
		settings.lumbination.setLeaves(getStringArray(Constants.LUMBINATION_ID, "leaves", MAConfig.defaults.lumbination.leaves()));
		settings.lumbination.setAxes(getStringArray(Constants.LUMBINATION_ID, "axes", MAConfig.defaults.lumbination.axes()));
		
		List<String> order = new ArrayList<String>(8);
		order.add(Functions.localize(Constants.LUMBINATION_ID + ".enabled"));
		order.add(Functions.localize(Constants.LUMBINATION_ID + ".chop_below"));
		order.add(Functions.localize(Constants.LUMBINATION_ID + ".destroy_leaves"));
		order.add(Functions.localize(Constants.LUMBINATION_ID + ".leaves_affect_durability"));
		order.add(Functions.localize(Constants.LUMBINATION_ID + ".leaf_range"));
		order.add(Functions.localize(Constants.LUMBINATION_ID + ".trunk_range"));
		order.add(Functions.localize(Constants.LUMBINATION_ID + ".logs"));
		order.add(Functions.localize(Constants.LUMBINATION_ID + ".leaves"));
		order.add(Functions.localize(Constants.LUMBINATION_ID + ".axes"));
		
		_config.setCategoryPropertyOrder(Constants.LUMBINATION_ID, order);
	}
	
	private static void getPathanationSettings() {
		settings.pathanation.setEnabled(_config.getBoolean(Functions.localize(Constants.PATHANATION_ID + ".enabled"), Constants.PATHANATION_ID, MAConfig.defaults.pathanation.bEnabled(), Functions.localize(Constants.PATHANATION_ID + ".enabled.desc")));
		settings.pathanation.setPathWidth(_config.getInt(Functions.localize(Constants.PATHANATION_ID + ".path_width"), Constants.PATHANATION_ID, MAConfig.defaults.pathanation.iPathWidth(), 1, 16, Functions.localize(Constants.PATHANATION_ID + ".path_width.desc")));
		settings.pathanation.setPathLength(_config.getInt(Functions.localize(Constants.PATHANATION_ID + ".path_length"), Constants.PATHANATION_ID, MAConfig.defaults.pathanation.iPathLength(), 1, 64, Functions.localize(Constants.PATHANATION_ID + ".path_length.desc")));
		
		List<String> order = new ArrayList<String>(11);
		order.add(Functions.localize(Constants.PATHANATION_ID + ".enabled"));
		order.add(Functions.localize(Constants.PATHANATION_ID + ".path_width"));
		order.add(Functions.localize(Constants.PATHANATION_ID + ".path_length"));
		
		_config.setCategoryPropertyOrder(Constants.PATHANATION_ID, order);
	}
	
	private static void getShaftanationSettings() {
		settings.shaftanation.setEnabled(_config.getBoolean(Functions.localize(Constants.SHAFTANATION_ID + ".enabled"), Constants.SHAFTANATION_ID, MAConfig.defaults.shaftanation.bEnabled(), Functions.localize(Constants.SHAFTANATION_ID + ".enabled.desc")));
		settings.shaftanation.setShaftLength(_config.getInt(Functions.localize(Constants.SHAFTANATION_ID + ".shaft_l"), Constants.SHAFTANATION_ID, MAConfig.defaults.shaftanation.iShaftLength(), 4, 128, Functions.localize(Constants.SHAFTANATION_ID + ".shaft_l.desc")));
		settings.shaftanation.setShaftHeight(_config.getInt(Functions.localize(Constants.SHAFTANATION_ID + ".shaft_h"), Constants.SHAFTANATION_ID, MAConfig.defaults.shaftanation.iShaftHeight(), 2, 16, Functions.localize(Constants.SHAFTANATION_ID + ".shaft_l.desc")));
		settings.shaftanation.setShaftWidth(_config.getInt(Functions.localize(Constants.SHAFTANATION_ID + ".shaft_w"), Constants.SHAFTANATION_ID, MAConfig.defaults.shaftanation.iShaftWidth(), 1, 16, Functions.localize(Constants.SHAFTANATION_ID + ".shaft_w.desc")));
		
		List<String> order = new ArrayList<String>(4);
		order.add(Functions.localize(Constants.SHAFTANATION_ID + ".enabled"));
		order.add(Functions.localize(Constants.SHAFTANATION_ID + ".shaft_l"));
		order.add(Functions.localize(Constants.SHAFTANATION_ID + ".shaft_h"));
		order.add(Functions.localize(Constants.SHAFTANATION_ID + ".shaft_w"));
		
		_config.setCategoryPropertyOrder(Constants.SHAFTANATION_ID, order);
	}
	
	private static void getSubstitutionSettings() {
		settings.substitution.setEnabled(_config.getBoolean(Functions.localize(Constants.SUBSTITUTION_ID + ".enabled"), Constants.SUBSTITUTION_ID, MAConfig.defaults.substitution.bEnabled(), Functions.localize(Constants.SUBSTITUTION_ID + ".enabled.desc")));
		settings.substitution.setSwitchBack(_config.getBoolean(Functions.localize(Constants.SUBSTITUTION_ID + ".switchback"), Constants.SUBSTITUTION_ID, MAConfig.defaults.substitution.bSwitchBack(), Functions.localize(Constants.SUBSTITUTION_ID + ".switchback.desc")));
		settings.substitution.setFavourSilkTouch(_config.getBoolean(Functions.localize(Constants.SUBSTITUTION_ID + ".favour_silk"), Constants.SUBSTITUTION_ID, MAConfig.defaults.substitution.bFavourSilkTouch(), Functions.localize(Constants.SUBSTITUTION_ID + ".favour_silk.desc")));
		settings.substitution.setFavourFortune(_config.getBoolean(Functions.localize(Constants.SUBSTITUTION_ID + ".favour_fortune"), Constants.SUBSTITUTION_ID, MAConfig.defaults.substitution.bFavourFortune(), Functions.localize(Constants.SUBSTITUTION_ID + ".favour_fortune.desc")));
		settings.substitution.setIgnoreIfValidTool(_config.getBoolean(Functions.localize(Constants.SUBSTITUTION_ID + ".ignore_valid"), Constants.SUBSTITUTION_ID, MAConfig.defaults.substitution.bIgnoreIfValidTool(), Functions.localize(Constants.SUBSTITUTION_ID + ".ignore_valid.desc")));
		settings.substitution.setIgnorePassiveMobs(_config.getBoolean(Functions.localize(Constants.SUBSTITUTION_ID + ".ignore_passive"), Constants.SUBSTITUTION_ID, MAConfig.defaults.substitution.bIgnorePassiveMobs(), Functions.localize(Constants.SUBSTITUTION_ID + ".ignore_passive.desc")));
		
		List<String> order = new ArrayList<String>(6);
		order.add(Functions.localize(Constants.SUBSTITUTION_ID + ".enabled"));
		order.add(Functions.localize(Constants.SUBSTITUTION_ID + ".switchback"));
		order.add(Functions.localize(Constants.SUBSTITUTION_ID + ".favour_silk"));
		order.add(Functions.localize(Constants.SUBSTITUTION_ID + ".favour_fortune"));
		order.add(Functions.localize(Constants.SUBSTITUTION_ID + ".ignore_valid"));
		order.add(Functions.localize(Constants.SUBSTITUTION_ID + ".ignore_passive"));
		
		_config.setCategoryPropertyOrder(Constants.SUBSTITUTION_ID, order);
	}
	
	private static void getVeinationSettings() {
		settings.veination.setEnabled(_config.getBoolean(Functions.localize(Constants.VEINATION_ID + ".enabled"), Constants.VEINATION_ID, MAConfig.defaults.veination.bEnabled(), Functions.localize(Constants.VEINATION_ID + ".enabled.desc")));
		
		settings.veination.setOres(getStringArray(Constants.VEINATION_ID, "ores", MAConfig.defaults.veination.ores()));
		
		List<String> order = new ArrayList<String>(2);
		order.add(Functions.localize(Constants.VEINATION_ID + ".enabled"));
		order.add(Functions.localize(Constants.VEINATION_ID + ".ores"));
		
		_config.setCategoryPropertyOrder(Constants.VEINATION_ID, order);
	}
	
}
