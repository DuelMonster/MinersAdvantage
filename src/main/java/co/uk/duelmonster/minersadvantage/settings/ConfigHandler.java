package co.uk.duelmonster.minersadvantage.settings;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;

import co.uk.duelmonster.minersadvantage.MinersAdvantage;
import co.uk.duelmonster.minersadvantage.common.Constants;
import co.uk.duelmonster.minersadvantage.common.Functions;
import co.uk.duelmonster.minersadvantage.common.JsonHelper;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

public class ConfigHandler {
	private static Configuration	_config		= null;
	private static Settings			settings	= null;
	
	public static void initConfigs(Configuration config) {
		if (config == null) {
			MinersAdvantage.logger.log(Level.ERROR, "Config attempted to be loaded before it was initialised!");
			return;
		}
		
		if (MinersAdvantage.proxy.isClient())
			settings = Settings.get();
		else
			settings = SettingsServer.get();
		
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
			_config.save();
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
				getCategory(category).get(Functions.localize(category + "." + key)).setValue((boolean) value);
			else if (value instanceof String)
				getCategory(category).get(Functions.localize(category + "." + key)).setValue((String) value);
	}
	
	private static void getCommonSettings() {
		settings.setGatherDrops(_config.getBoolean(Functions.localize(Constants.COMMON_ID + ".gather_drops"), Constants.COMMON_ID, Settings.defaults.bGatherDrops(), Functions.localize(Constants.COMMON_ID + ".gather_drops.desc")));
		settings.setAutoIlluminate(_config.getBoolean(Functions.localize(Constants.COMMON_ID + ".auto_illum"), Constants.COMMON_ID, Settings.defaults.bAutoIlluminate(), Functions.localize(Constants.COMMON_ID + ".auto_illum.desc")));
		settings.setMineVeins(_config.getBoolean(Functions.localize(Constants.COMMON_ID + ".mine_veins"), Constants.COMMON_ID, Settings.defaults.bMineVeins(), Functions.localize(Constants.COMMON_ID + ".mine_veins.desc")));
		settings.setBlocksPerTick(_config.getInt(Functions.localize(Constants.COMMON_ID + ".blocks_per_tick"), Constants.COMMON_ID, Settings.defaults.iBlocksPerTick(), 1, 16, Functions.localize(Constants.COMMON_ID + ".blocks_per_tick.desc")));
		settings.setEnableTickDelay(_config.getBoolean(Functions.localize(Constants.COMMON_ID + ".enable_tick_delay"), Constants.COMMON_ID, Settings.defaults.bEnableTickDelay(), Functions.localize(Constants.COMMON_ID + ".enable_tick_delay.desc")));
		settings.setTickDelay(_config.getInt(Functions.localize(Constants.COMMON_ID + ".tick_delay"), Constants.COMMON_ID, Settings.defaults.iTickDelay(), 1, 20, Functions.localize(Constants.COMMON_ID + ".tick_delay.desc")));
		settings.setBlockRadius(_config.getInt(Functions.localize(Constants.COMMON_ID + ".radius"), Constants.COMMON_ID, Settings.defaults.iBlockRadius(), Constants.MIN_BLOCKRADIUS, Constants.MAX_BLOCKRADIUS, Functions.localize(Constants.COMMON_ID + ".radius.desc")));
		settings.setBlockLimit(_config.getInt(Functions.localize(Constants.COMMON_ID + ".limit"), Constants.COMMON_ID, Settings.defaults.iBlockLimit(), ((Constants.MIN_BLOCKRADIUS * Constants.MIN_BLOCKRADIUS) * Constants.MIN_BLOCKRADIUS), Constants.MAX_BLOCKLIMIT, Functions.localize(Constants.COMMON_ID + ".limit.desc")));
		// settings.setBreakAtToolSpeeds(_config.getBoolean(Functions.localize(Constants.COMMON_ID +
		// ".break_at_tool_speeds"), Constants.COMMON_ID, Settings.defaults.bBreakAtToolSpeeds(),
		// Functions.localize(Constants.COMMON_ID + ".break_at_tool_speeds.desc")));
		settings.setDisableParticleEffects(_config.getBoolean(Functions.localize(Constants.COMMON_ID + ".disable_particle_effects"), Constants.COMMON_ID, Settings.defaults.bDisableParticleEffects(), Functions.localize(Constants.COMMON_ID + ".disable_particle_effects.desc")));
		
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
		SettingsServer serverSettings = (SettingsServer) settings;
		
		serverSettings.bOverrideFeatureEnablement = _config.getBoolean(Functions.localize(Constants.SERVER_ID + ".override_client"), Constants.SERVER_ID, SettingsServer.defaults.bOverrideFeatureEnablement, Functions.localize(Constants.SERVER_ID + ".override_client.desc"));
		serverSettings.bEnforceCommonSettings = _config.getBoolean(Functions.localize(Constants.SERVER_ID + ".enforce_common_settings"), Constants.SERVER_ID, SettingsServer.defaults.bEnforceCommonSettings, Functions.localize(Constants.SERVER_ID + ".enforce_common_settings.desc"));
		serverSettings.bEnforceCaptivationSettings = _config.getBoolean(Functions.localize(Constants.SERVER_ID + ".enforce_captivation_settings"), Constants.SERVER_ID, SettingsServer.defaults.bEnforceCaptivationSettings, Functions.localize(Constants.SERVER_ID + ".enforce_captivation_settings.desc"));
		serverSettings.bEnforceCropinationSettings = _config.getBoolean(Functions.localize(Constants.SERVER_ID + ".enforce_cropination_settings"), Constants.SERVER_ID, SettingsServer.defaults.bEnforceCropinationSettings, Functions.localize(Constants.SERVER_ID + ".enforce_cropination_settings.desc"));
		serverSettings.bEnforceExcavationSettings = _config.getBoolean(Functions.localize(Constants.SERVER_ID + ".enforce_excavation_settings"), Constants.SERVER_ID, SettingsServer.defaults.bEnforceExcavationSettings, Functions.localize(Constants.SERVER_ID + ".enforce_excavation_settings.desc"));
		serverSettings.bEnforceIlluminationSettings = _config.getBoolean(Functions.localize(Constants.SERVER_ID + ".enforce_illumination_settings"), Constants.SERVER_ID, SettingsServer.defaults.bEnforceIlluminationSettings, Functions.localize(Constants.SERVER_ID + ".enforce_illumination_settings.desc"));
		serverSettings.bEnforceLumbinationSettings = _config.getBoolean(Functions.localize(Constants.SERVER_ID + ".enforce_lumbination_settings"), Constants.SERVER_ID, SettingsServer.defaults.bEnforceLumbinationSettings, Functions.localize(Constants.SERVER_ID + ".enforce_lumbination_settings.desc"));
		serverSettings.bEnforceShaftanationSettings = _config.getBoolean(Functions.localize(Constants.SERVER_ID + ".enforce_shaftanation_settings"), Constants.SERVER_ID, SettingsServer.defaults.bEnforceShaftanationSettings, Functions.localize(Constants.SERVER_ID + ".enforce_shaftanation_settings.desc"));
		serverSettings.bEnforceSubstitutionSettings = _config.getBoolean(Functions.localize(Constants.SERVER_ID + ".enforce_substitution_settings"), Constants.SERVER_ID, SettingsServer.defaults.bEnforceSubstitutionSettings, Functions.localize(Constants.SERVER_ID + ".enforce_substitution_settings.desc"));
		serverSettings.bEnforceVeinationSettings = _config.getBoolean(Functions.localize(Constants.SERVER_ID + ".enforce_veination_settings"), Constants.SERVER_ID, SettingsServer.defaults.bEnforceVeinationSettings, Functions.localize(Constants.SERVER_ID + ".enforce_veination_settings.desc"));
		
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
		settings.setCaptivationEnabled(_config.getBoolean(Functions.localize(Constants.CAPTIVATION_ID + ".enabled"), Constants.CAPTIVATION_ID, Settings.defaults.bCaptivationEnabled(), Functions.localize(Constants.CAPTIVATION_ID + ".enabled.desc")));
		settings.setAllowInGUI(_config.getBoolean(Functions.localize(Constants.CAPTIVATION_ID + ".allow_in_gui"), Constants.CAPTIVATION_ID, Settings.defaults.bAllowInGUI(), Functions.localize(Constants.CAPTIVATION_ID + ".allow_in_gui.desc")));
		settings.setRadiusHorizontal(_config.getFloat(Functions.localize(Constants.CAPTIVATION_ID + ".h_radius"), Constants.CAPTIVATION_ID, (float) Settings.defaults.radiusHorizontal(), 0.0F, 128.0F, Functions.localize(Constants.CAPTIVATION_ID + ".h_radius.desc")));
		settings.setRadiusVertical(_config.getFloat(Functions.localize(Constants.CAPTIVATION_ID + ".v_radius"), Constants.CAPTIVATION_ID, (float) Settings.defaults.radiusVertical(), 0.0F, 128.0F, Functions.localize(Constants.CAPTIVATION_ID + ".v_radius.desc")));
		settings.setIsWhitelist(_config.getBoolean(Functions.localize(Constants.CAPTIVATION_ID + ".is_whitelist"), Constants.CAPTIVATION_ID, Settings.defaults.bIsWhitelist(), Functions.localize(Constants.CAPTIVATION_ID + ".is_whitelist.desc")));
		settings.setCaptivationBlacklist(JsonHelper.ParseObject(_config.getString(Functions.localize(Constants.CAPTIVATION_ID + ".blacklist"), Constants.CAPTIVATION_ID, Settings.defaults.captivationBlacklist().toString(), Functions.localize(Constants.CAPTIVATION_ID + ".blacklist.desc"))));
		
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
		settings.setCropinationEnabled(_config.getBoolean(Functions.localize(Constants.CROPINATION_ID + ".enabled"), Constants.CROPINATION_ID, Settings.defaults.bCropinationEnabled(), Functions.localize(Constants.CROPINATION_ID + ".enabled.desc")));
		settings.setHarvestSeeds(_config.getBoolean(Functions.localize(Constants.CROPINATION_ID + ".harvest_seeds"), Constants.CROPINATION_ID, Settings.defaults.bHarvestSeeds(), Functions.localize(Constants.CROPINATION_ID + ".harvest_seeds.desc")));
		
		List<String> order = new ArrayList<String>(2);
		order.add(Functions.localize(Constants.CROPINATION_ID + ".enabled"));
		order.add(Functions.localize(Constants.CROPINATION_ID + ".harvest_seeds"));
		
		_config.setCategoryPropertyOrder(Constants.CROPINATION_ID, order);
	}
	
	private static void getExcavationSettings() {
		settings.setExcavationEnabled(_config.getBoolean(Functions.localize(Constants.EXCAVATION_ID + ".enabled"), Constants.EXCAVATION_ID, Settings.defaults.bExcavationEnabled(), Functions.localize(Constants.EXCAVATION_ID + ".enabled.desc")));
		settings.setToggleMode(_config.getBoolean(Functions.localize(Constants.EXCAVATION_ID + ".toggle_mode"), Constants.EXCAVATION_ID, Settings.defaults.bToggleMode(), Functions.localize(Constants.EXCAVATION_ID + ".toggle_mode.desc")));
		settings.setIgnoreBlockVariants(_config.getBoolean(Functions.localize(Constants.EXCAVATION_ID + ".ignore_variants"), Constants.EXCAVATION_ID, Settings.defaults.bIgnoreBlockVariants(), Functions.localize(Constants.EXCAVATION_ID + ".ignore_variants.desc")));
		settings.setIsToolWhitelist(_config.getBoolean(Functions.localize(Constants.EXCAVATION_ID + ".is_tool_whitelist"), Constants.EXCAVATION_ID, Settings.defaults.bIsToolWhitelist(), Functions.localize(Constants.EXCAVATION_ID + ".is_tool_whitelist.desc")));
		settings.setToolBlacklist(JsonHelper.ParseObject(_config.getString(Functions.localize(Constants.EXCAVATION_ID + ".tool_blacklist"), Constants.EXCAVATION_ID, Settings.defaults.toolBlacklist().toString(), Functions.localize(Constants.EXCAVATION_ID + ".tool_blacklist.desc"))));
		settings.setIsBlockWhitelist(_config.getBoolean(Functions.localize(Constants.EXCAVATION_ID + ".is_block_whitelist"), Constants.EXCAVATION_ID, Settings.defaults.bIsBlockWhitelist(), Functions.localize(Constants.EXCAVATION_ID + ".is_block_whitelist.desc")));
		settings.setBlockBlacklist(JsonHelper.ParseObject(_config.getString(Functions.localize(Constants.EXCAVATION_ID + ".block_blacklist"), Constants.EXCAVATION_ID, Settings.defaults.blockBlacklist().toString(), Functions.localize(Constants.EXCAVATION_ID + ".block_blacklist.desc"))));
		settings.setPathWidth(_config.getInt(Functions.localize(Constants.EXCAVATION_ID + ".path_width"), Constants.EXCAVATION_ID, Settings.defaults.iPathWidth(), 1, 16, Functions.localize(Constants.EXCAVATION_ID + ".path_width.desc")));
		settings.setPathLength(_config.getInt(Functions.localize(Constants.EXCAVATION_ID + ".path_length"), Constants.EXCAVATION_ID, Settings.defaults.iPathLength(), 1, 64, Functions.localize(Constants.EXCAVATION_ID + ".path_length.desc")));
		
		List<String> order = new ArrayList<String>(11);
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".enabled"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".toggle_mode"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".ignore_variants"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".is_tool_whitelist"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".tool_whitelist"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".is_block_whitelist"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".block_whitelist"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".path_width"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".path_length"));
		
		_config.setCategoryPropertyOrder(Constants.EXCAVATION_ID, order);
	}
	
	private static void getIlluminationSettings() {
		settings.setIlluminationEnabled(_config.getBoolean(Functions.localize(Constants.ILLUMINATION_ID + ".enabled"), Constants.ILLUMINATION_ID, Settings.defaults.bIlluminationEnabled(), Functions.localize(Constants.ILLUMINATION_ID + ".enabled.desc")));
		settings.setUseBlockLight(_config.getBoolean(Functions.localize(Constants.ILLUMINATION_ID + ".use_block_light"), Constants.ILLUMINATION_ID, Settings.defaults.bUseBlockLight(), Functions.localize(Constants.ILLUMINATION_ID + ".use_block_light.desc")));
		settings.setLowestLightLevel(_config.getInt(Functions.localize(Constants.ILLUMINATION_ID + ".light_level"), Constants.ILLUMINATION_ID, Settings.defaults.iLowestLightLevel(), 0, 16, Functions.localize(Constants.ILLUMINATION_ID + ".light_level.desc")));
		
		List<String> order = new ArrayList<String>(2);
		order.add(Functions.localize(Constants.ILLUMINATION_ID + ".enabled"));
		order.add(Functions.localize(Constants.ILLUMINATION_ID + ".use_block_light"));
		order.add(Functions.localize(Constants.ILLUMINATION_ID + ".light_level"));
		
		_config.setCategoryPropertyOrder(Constants.ILLUMINATION_ID, order);
	}
	
	private static void getLumbinationSettings() {
		settings.setLumbinationEnabled(_config.getBoolean(Functions.localize(Constants.LUMBINATION_ID + ".enabled"), Constants.LUMBINATION_ID, Settings.defaults.bLumbinationEnabled(), Functions.localize(Constants.LUMBINATION_ID + ".enabled.desc")));
		settings.setChopTreeBelow(_config.getBoolean(Functions.localize(Constants.LUMBINATION_ID + ".chop_below"), Constants.LUMBINATION_ID, Settings.defaults.bChopTreeBelow(), Functions.localize(Constants.LUMBINATION_ID + ".chop_below.desc")));
		settings.setDestroyLeaves(_config.getBoolean(Functions.localize(Constants.LUMBINATION_ID + ".destroy_leaves"), Constants.LUMBINATION_ID, Settings.defaults.bDestroyLeaves(), Functions.localize(Constants.LUMBINATION_ID + ".destroy_leaves.desc")));
		settings.setLeavesAffectDurability(_config.getBoolean(Functions.localize(Constants.LUMBINATION_ID + ".leaves_affect_durability"), Constants.LUMBINATION_ID, Settings.defaults.bLeavesAffectDurability(), Functions.localize(Constants.LUMBINATION_ID + ".leaves_affect_durability.desc")));
		settings.setLeafRange(_config.getInt(Functions.localize(Constants.LUMBINATION_ID + ".leaf_range"), Constants.LUMBINATION_ID, Settings.defaults.iLeafRange(), 0, 16, Functions.localize(Constants.LUMBINATION_ID + ".leaf_range.desc")));
		settings.setTrunkRange(_config.getInt(Functions.localize(Constants.LUMBINATION_ID + ".trunk_range"), Constants.LUMBINATION_ID, Settings.defaults.iTrunkRange(), 8, 128, Functions.localize(Constants.LUMBINATION_ID + ".trunk_range.desc")));
		settings.setLumbinationLogs(JsonHelper.ParseObject(_config.getString(Functions.localize(Constants.LUMBINATION_ID + ".logs"), Constants.LUMBINATION_ID, Settings.defaults.lumbinationLogs().toString(), Functions.localize(Constants.LUMBINATION_ID + ".logs.desc"))));
		settings.setLumbinationLeaves(JsonHelper.ParseObject(_config.getString(Functions.localize(Constants.LUMBINATION_ID + ".leaves"), Constants.LUMBINATION_ID, Settings.defaults.lumbinationLeaves().toString(), Functions.localize(Constants.LUMBINATION_ID + ".leaves.desc"))));
		settings.setLumbinationAxes(JsonHelper.ParseObject(_config.getString(Functions.localize(Constants.LUMBINATION_ID + ".axes"), Constants.LUMBINATION_ID, Settings.defaults.lumbinationAxes().toString(), Functions.localize(Constants.LUMBINATION_ID + ".axes.desc"))));
		
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
	
	private static void getShaftanationSettings() {
		settings.setShaftanationEnabled(_config.getBoolean(Functions.localize(Constants.SHAFTANATION_ID + ".enabled"), Constants.SHAFTANATION_ID, Settings.defaults.bShaftanationEnabled(), Functions.localize(Constants.SHAFTANATION_ID + ".enabled.desc")));
		settings.setShaftLength(_config.getInt(Functions.localize(Constants.SHAFTANATION_ID + ".shaft_l"), Constants.SHAFTANATION_ID, Settings.defaults.iShaftLength(), 4, 128, Functions.localize(Constants.SHAFTANATION_ID + ".shaft_l.desc")));
		settings.setShaftHeight(_config.getInt(Functions.localize(Constants.SHAFTANATION_ID + ".shaft_h"), Constants.SHAFTANATION_ID, Settings.defaults.iShaftHeight(), 2, 16, Functions.localize(Constants.SHAFTANATION_ID + ".shaft_l.desc")));
		settings.setShaftWidth(_config.getInt(Functions.localize(Constants.SHAFTANATION_ID + ".shaft_w"), Constants.SHAFTANATION_ID, Settings.defaults.iShaftWidth(), 1, 16, Functions.localize(Constants.SHAFTANATION_ID + ".shaft_w.desc")));
		
		List<String> order = new ArrayList<String>(4);
		order.add(Functions.localize(Constants.SHAFTANATION_ID + ".enabled"));
		order.add(Functions.localize(Constants.SHAFTANATION_ID + ".shaft_l"));
		order.add(Functions.localize(Constants.SHAFTANATION_ID + ".shaft_h"));
		order.add(Functions.localize(Constants.SHAFTANATION_ID + ".shaft_w"));
		
		_config.setCategoryPropertyOrder(Constants.SHAFTANATION_ID, order);
	}
	
	private static void getSubstitutionSettings() {
		settings.setSubstitutionEnabled(_config.getBoolean(Functions.localize(Constants.SUBSTITUTION_ID + ".enabled"), Constants.SUBSTITUTION_ID, Settings.defaults.bSubstitutionEnabled(), Functions.localize(Constants.SUBSTITUTION_ID + ".enabled.desc")));
		settings.setSwitchBack(_config.getBoolean(Functions.localize(Constants.SUBSTITUTION_ID + ".switchback"), Constants.SUBSTITUTION_ID, Settings.defaults.bSwitchBack(), Functions.localize(Constants.SUBSTITUTION_ID + ".switchback.desc")));
		settings.setFavourSilkTouch(_config.getBoolean(Functions.localize(Constants.SUBSTITUTION_ID + ".favour_silk"), Constants.SUBSTITUTION_ID, Settings.defaults.bFavourSilkTouch(), Functions.localize(Constants.SUBSTITUTION_ID + ".favour_silk.desc")));
		settings.setFavourFortune(_config.getBoolean(Functions.localize(Constants.SUBSTITUTION_ID + ".favour_fortune"), Constants.SUBSTITUTION_ID, Settings.defaults.bFavourFortune(), Functions.localize(Constants.SUBSTITUTION_ID + ".favour_fortune.desc")));
		settings.setIgnoreIfValidTool(_config.getBoolean(Functions.localize(Constants.SUBSTITUTION_ID + ".ignore_valid"), Constants.SUBSTITUTION_ID, Settings.defaults.bIgnoreIfValidTool(), Functions.localize(Constants.SUBSTITUTION_ID + ".ignore_valid.desc")));
		settings.setIgnorePassiveMobs(_config.getBoolean(Functions.localize(Constants.SUBSTITUTION_ID + ".ignore_passive"), Constants.SUBSTITUTION_ID, Settings.defaults.bIgnorePassiveMobs(), Functions.localize(Constants.SUBSTITUTION_ID + ".ignore_passive.desc")));
		
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
		settings.setVeinationEnabled(_config.getBoolean(Functions.localize(Constants.VEINATION_ID + ".enabled"), Constants.VEINATION_ID, Settings.defaults.bVeinationEnabled(), Functions.localize(Constants.VEINATION_ID + ".enabled.desc")));
		settings.setVeinationOres(JsonHelper.ParseObject(_config.getString(Functions.localize(Constants.VEINATION_ID + ".ores"), Constants.VEINATION_ID, Settings.defaults.veinationOres().toString(), Functions.localize(Constants.VEINATION_ID + ".ores.desc"))));
		
		List<String> order = new ArrayList<String>(2);
		order.add(Functions.localize(Constants.VEINATION_ID + ".enabled"));
		order.add(Functions.localize(Constants.VEINATION_ID + ".ores"));
		
		_config.setCategoryPropertyOrder(Constants.VEINATION_ID, order);
	}
	
}
