package co.uk.duelmonster.minersadvantage.client.config;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;

import co.uk.duelmonster.minersadvantage.MinersAdvantage;
import co.uk.duelmonster.minersadvantage.common.Constants;
import co.uk.duelmonster.minersadvantage.common.Functions;
import co.uk.duelmonster.minersadvantage.common.JsonHelper;
import co.uk.duelmonster.minersadvantage.settings.Settings;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ConfigHandler {
	private static Configuration	_config		= null;
	private final static Settings	settings	= Settings.get();
	
	public static void initConfigs(Configuration config) {
		if (config == null) {
			MinersAdvantage.logger.log(Level.ERROR, "Config attempted to be loaded before it was initialised!");
			return;
		}
		
		_config = config;
		
		initConfigs();
	}
	
	public static void initConfigs() {
		_config.load();
		
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
		if (_config != null && _config.hasChanged())
			_config.save();
	}
	
	public static ConfigCategory getCategory(String category) {
		if (_config != null)
			return _config.getCategory(category);
		return null;
	}
	
	public static void setValue(String category, String key, Object value) {
		if (_config != null)
			if (value instanceof Boolean)
				_config.getCategory(category).get(Functions.localize(category + "." + key)).setValue((boolean) value);;
	}
	
	private static void getCommonSettings() {
		settings.iBreakSpeed = _config.getInt(Functions.localize(Constants.COMMON_ID + ".break_speed"), Constants.COMMON_ID, Settings.defaults.iBreakSpeed, 1, 16, Functions.localize(Constants.COMMON_ID + ".break_speed.desc"));
		settings.bGatherDrops = _config.getBoolean(Functions.localize(Constants.COMMON_ID + ".gather_drops"), Constants.COMMON_ID, Settings.defaults.bGatherDrops, Functions.localize(Constants.COMMON_ID + ".gather_drops.desc"));
		settings.bAutoIlluminate = _config.getBoolean(Functions.localize(Constants.COMMON_ID + ".auto_illum"), Constants.COMMON_ID, Settings.defaults.bAutoIlluminate, Functions.localize(Constants.COMMON_ID + ".auto_illum.desc"));
		settings.bMineVeins = _config.getBoolean(Functions.localize(Constants.COMMON_ID + ".mine_veins"), Constants.COMMON_ID, Settings.defaults.bMineVeins, Functions.localize(Constants.COMMON_ID + ".mine_veins.desc"));
		
		List<String> order = new ArrayList<String>(7);
		order.add(Functions.localize(Constants.COMMON_ID + ".gather_drops"));
		order.add(Functions.localize(Constants.COMMON_ID + ".auto_illum"));
		order.add(Functions.localize(Constants.COMMON_ID + ".mine_veins"));
		
		_config.setCategoryPropertyOrder(Constants.COMMON_ID, order);
	}
	
	private static void getCaptivationSettings() {
		settings.bCaptivationEnabled = _config.getBoolean(Functions.localize(Constants.CAPTIVATION_ID + ".enabled"), Constants.CAPTIVATION_ID, Settings.defaults.bCaptivationEnabled, Functions.localize(Constants.CAPTIVATION_ID + ".enabled.desc"));
		settings.bAllowInGUI = _config.getBoolean(Functions.localize(Constants.CAPTIVATION_ID + ".allow_in_gui"), Constants.CAPTIVATION_ID, Settings.defaults.bAllowInGUI, Functions.localize(Constants.CAPTIVATION_ID + ".allow_in_gui.desc"));
		settings.radiusHorizontal = _config.getFloat(Functions.localize(Constants.CAPTIVATION_ID + ".h_radius"), Constants.CAPTIVATION_ID, (float) Settings.defaults.radiusHorizontal, 0.0F, 128.0F, Functions.localize(Constants.CAPTIVATION_ID + ".h_radius.desc"));
		settings.radiusVertical = _config.getFloat(Functions.localize(Constants.CAPTIVATION_ID + ".v_radius"), Constants.CAPTIVATION_ID, (float) Settings.defaults.radiusVertical, 0.0F, 128.0F, Functions.localize(Constants.CAPTIVATION_ID + ".v_radius.desc"));
		settings.bIsWhitelist = _config.getBoolean(Functions.localize(Constants.CAPTIVATION_ID + ".is_whitelist"), Constants.CAPTIVATION_ID, Settings.defaults.bIsWhitelist, Functions.localize(Constants.CAPTIVATION_ID + ".is_whitelist.desc"));
		settings.captivationBlacklist = JsonHelper.ParseObject(_config.getString(Functions.localize(Constants.CAPTIVATION_ID + ".blacklist"), Constants.CAPTIVATION_ID, Settings.defaults.captivationBlacklist.toString(), Functions.localize(Constants.CAPTIVATION_ID + ".blacklist.desc")));
		
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
		settings.bCropinationEnabled = _config.getBoolean(Functions.localize(Constants.CROPINATION_ID + ".enabled"), Constants.CROPINATION_ID, Settings.defaults.bCropinationEnabled, Functions.localize(Constants.CROPINATION_ID + ".enabled.desc"));
		settings.bHarvestSeeds = _config.getBoolean(Functions.localize(Constants.CROPINATION_ID + ".harvest_seeds"), Constants.CROPINATION_ID, Settings.defaults.bHarvestSeeds, Functions.localize(Constants.CROPINATION_ID + ".harvest_seeds.desc"));
		
		List<String> order = new ArrayList<String>(9);
		order.add(Functions.localize(Constants.CROPINATION_ID + ".enabled"));
		order.add(Functions.localize(Constants.CROPINATION_ID + ".harvest_seeds"));
		
		_config.setCategoryPropertyOrder(Constants.CROPINATION_ID, order);
	}
	
	private static void getExcavationSettings() {
		settings.bExcavationEnabled = _config.getBoolean(Functions.localize(Constants.EXCAVATION_ID + ".enabled"), Constants.EXCAVATION_ID, Settings.defaults.bExcavationEnabled, Functions.localize(Constants.EXCAVATION_ID + ".enabled.desc"));
		settings.bToggleMode = _config.getBoolean(Functions.localize(Constants.EXCAVATION_ID + ".toggle_mode"), Constants.EXCAVATION_ID, Settings.defaults.bToggleMode, Functions.localize(Constants.EXCAVATION_ID + ".toggle_mode.desc"));
		settings.bIgnoreBlockVariants = _config.getBoolean(Functions.localize(Constants.EXCAVATION_ID + ".ignore_variants"), Constants.EXCAVATION_ID, Settings.defaults.bIgnoreBlockVariants, Functions.localize(Constants.EXCAVATION_ID + ".ignore_variants.desc"));
		settings.iBlockRadius = _config.getInt(Functions.localize(Constants.EXCAVATION_ID + ".radius"), Constants.EXCAVATION_ID, Settings.defaults.iBlockRadius, Constants.MIN_BLOCKRADIUS, Constants.MAX_BLOCKRADIUS, Functions.localize(Constants.EXCAVATION_ID + ".radius.desc"));
		settings.iBlockLimit = _config.getInt(Functions.localize(Constants.EXCAVATION_ID + ".limit"), Constants.EXCAVATION_ID, Settings.defaults.iBlockLimit, ((Constants.MIN_BLOCKRADIUS * Constants.MIN_BLOCKRADIUS) * Constants.MIN_BLOCKRADIUS), Constants.MAX_BLOCKLIMIT, Functions.localize(Constants.EXCAVATION_ID + ".limit.desc"));
		settings.iPathWidth = _config.getInt(Functions.localize(Constants.EXCAVATION_ID + ".path_width"), Constants.EXCAVATION_ID, Settings.defaults.iPathWidth, 1, 16, Functions.localize(Constants.EXCAVATION_ID + ".path_width.desc"));
		settings.iPathLength = _config.getInt(Functions.localize(Constants.EXCAVATION_ID + ".path_length"), Constants.EXCAVATION_ID, Settings.defaults.iPathLength, 1, 64, Functions.localize(Constants.EXCAVATION_ID + ".path_length.desc"));
		settings.bIsToolWhitelist = _config.getBoolean(Functions.localize(Constants.EXCAVATION_ID + ".is_tool_whitelist"), Constants.EXCAVATION_ID, Settings.defaults.bIsToolWhitelist, Functions.localize(Constants.EXCAVATION_ID + ".ignore_variants.desc"));
		settings.bIsBlockWhitelist = _config.getBoolean(Functions.localize(Constants.EXCAVATION_ID + ".is_block_whitelist"), Constants.EXCAVATION_ID, Settings.defaults.bIsBlockWhitelist, Functions.localize(Constants.EXCAVATION_ID + ".ignore_variants.desc"));
		settings.toolBlacklist = JsonHelper.ParseObject(_config.getString(Functions.localize(Constants.EXCAVATION_ID + ".tool_blacklist"), Constants.EXCAVATION_ID, Settings.defaults.toolBlacklist.toString(), Functions.localize(Constants.EXCAVATION_ID + ".tool_blacklist.desc")));
		settings.blockBlacklist = JsonHelper.ParseObject(_config.getString(Functions.localize(Constants.EXCAVATION_ID + ".block_blacklist"), Constants.EXCAVATION_ID, Settings.defaults.blockBlacklist.toString(), Functions.localize(Constants.EXCAVATION_ID + ".block_blacklist.desc")));
		
		List<String> order = new ArrayList<String>(7);
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".enabled"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".toggle_mode"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".ignore_variants"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".radius"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".limit"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".path_width"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".path_length"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".is_tool_whitelist"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".is_block_whitelist"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".tool_whitelist"));
		order.add(Functions.localize(Constants.EXCAVATION_ID + ".block_whitelist"));
		
		_config.setCategoryPropertyOrder(Constants.EXCAVATION_ID, order);
	}
	
	private static void getIlluminationSettings() {
		settings.bIlluminationEnabled = _config.getBoolean(Functions.localize(Constants.ILLUMINATION_ID + ".enabled"), Constants.ILLUMINATION_ID, Settings.defaults.bIlluminationEnabled, Functions.localize(Constants.ILLUMINATION_ID + ".enabled.desc"));
		settings.iLowestLightLevel = _config.getInt(Functions.localize(Constants.ILLUMINATION_ID + ".light_level"), Constants.ILLUMINATION_ID, Settings.defaults.iLowestLightLevel, 0, 16, Functions.localize(Constants.ILLUMINATION_ID + ".light_level.desc"));
		
		List<String> order = new ArrayList<String>(2);
		order.add(Functions.localize(Constants.ILLUMINATION_ID + ".enabled"));
		order.add(Functions.localize(Constants.ILLUMINATION_ID + ".light_level"));
		
		_config.setCategoryPropertyOrder(Constants.ILLUMINATION_ID, order);
	}
	
	private static void getLumbinationSettings() {
		settings.bLumbinationEnabled = _config.getBoolean(Functions.localize(Constants.LUMBINATION_ID + ".enabled"), Constants.LUMBINATION_ID, settings.bLumbinationEnabled, Functions.localize(Constants.LUMBINATION_ID + ".enabled.desc"));
		settings.bChopTreeBelow = _config.getBoolean(Functions.localize(Constants.LUMBINATION_ID + ".chop_below"), Constants.LUMBINATION_ID, settings.bChopTreeBelow, Functions.localize(Constants.LUMBINATION_ID + ".chop_below.desc"));
		settings.bDestroyLeaves = _config.getBoolean(Functions.localize(Constants.LUMBINATION_ID + ".destroy_leaves"), Constants.LUMBINATION_ID, settings.bDestroyLeaves, Functions.localize(Constants.LUMBINATION_ID + ".destroy_leaves.desc"));
		settings.bLeavesAffectDurability = _config.getBoolean(Functions.localize(Constants.LUMBINATION_ID + ".leaves_affect_durability"), Constants.LUMBINATION_ID, settings.bLeavesAffectDurability, Functions.localize(Constants.LUMBINATION_ID + ".leaves_affect_durability.desc"));
		settings.iLeafRange = _config.getInt(Functions.localize(Constants.LUMBINATION_ID + ".leaf_range"), Constants.LUMBINATION_ID, settings.iLeafRange, 0, Integer.MAX_VALUE, Functions.localize(Constants.LUMBINATION_ID + ".leaf_range.desc"));
		settings.lumbinationLogs = JsonHelper.ParseObject(_config.getString(Functions.localize(Constants.LUMBINATION_ID + ".logs"), Constants.LUMBINATION_ID, Settings.defaults.lumbinationLogs.toString(), Functions.localize(Constants.LUMBINATION_ID + ".logs.desc")));
		settings.lumbinationLeaves = JsonHelper.ParseObject(_config.getString(Functions.localize(Constants.LUMBINATION_ID + ".leaves"), Constants.LUMBINATION_ID, Settings.defaults.lumbinationLeaves.toString(), Functions.localize(Constants.LUMBINATION_ID + ".leaves.desc")));
		settings.lumbinationAxes = JsonHelper.ParseObject(_config.getString(Functions.localize(Constants.LUMBINATION_ID + ".axes"), Constants.LUMBINATION_ID, Settings.defaults.lumbinationAxes.toString(), Functions.localize(Constants.LUMBINATION_ID + ".axes.desc")));
		
		List<String> order = new ArrayList<String>(9);
		order.add(Functions.localize(Constants.LUMBINATION_ID + ".enabled"));
		order.add(Functions.localize(Constants.LUMBINATION_ID + ".chop_below"));
		order.add(Functions.localize(Constants.LUMBINATION_ID + ".destroy_leaves"));
		order.add(Functions.localize(Constants.LUMBINATION_ID + ".leaves_affect_durability"));
		order.add(Functions.localize(Constants.LUMBINATION_ID + ".leaf_range"));
		order.add(Functions.localize(Constants.LUMBINATION_ID + ".logs"));
		order.add(Functions.localize(Constants.LUMBINATION_ID + ".leaves"));
		order.add(Functions.localize(Constants.LUMBINATION_ID + ".axes"));
		
		_config.setCategoryPropertyOrder(Constants.LUMBINATION_ID, order);
	}
	
	private static void getShaftanationSettings() {
		settings.bShaftanationEnabled = _config.getBoolean(Functions.localize(Constants.SHAFTANATION_ID + ".enabled"), Constants.SHAFTANATION_ID, Settings.defaults.bShaftanationEnabled, Functions.localize(Constants.SHAFTANATION_ID + ".enabled.desc"));
		settings.iShaftLength = _config.getInt(Functions.localize(Constants.SHAFTANATION_ID + ".shaft_l"), Constants.SHAFTANATION_ID, Settings.defaults.iShaftLength, 4, 128, Functions.localize(Constants.SHAFTANATION_ID + ".shaft_l.desc"));
		settings.iShaftHeight = _config.getInt(Functions.localize(Constants.SHAFTANATION_ID + ".shaft_h"), Constants.SHAFTANATION_ID, Settings.defaults.iShaftHeight, 2, 16, Functions.localize(Constants.SHAFTANATION_ID + ".shaft_l.desc"));
		settings.iShaftWidth = _config.getInt(Functions.localize(Constants.SHAFTANATION_ID + ".shaft_w"), Constants.SHAFTANATION_ID, Settings.defaults.iShaftWidth, 1, 16, Functions.localize(Constants.SHAFTANATION_ID + ".shaft_w.desc"));
		
		List<String> order = new ArrayList<String>(7);
		order.add(Functions.localize(Constants.SHAFTANATION_ID + ".enabled"));
		order.add(Functions.localize(Constants.SHAFTANATION_ID + ".shaft_l"));
		order.add(Functions.localize(Constants.SHAFTANATION_ID + ".shaft_h"));
		order.add(Functions.localize(Constants.SHAFTANATION_ID + ".shaft_w"));
		
		_config.setCategoryPropertyOrder(Constants.SHAFTANATION_ID, order);
	}
	
	private static void getSubstitutionSettings() {
		settings.bSubstitutionEnabled = _config.getBoolean(Functions.localize(Constants.SUBSTITUTION_ID + ".enabled"), Constants.SUBSTITUTION_ID, Settings.defaults.bSubstitutionEnabled, Functions.localize(Constants.SUBSTITUTION_ID + ".enabled.desc"));
		settings.bSwitchBack = _config.getBoolean(Functions.localize(Constants.SUBSTITUTION_ID + ".switchback"), Constants.SUBSTITUTION_ID, Settings.defaults.bSwitchBack, Functions.localize(Constants.SUBSTITUTION_ID + ".switchback.desc"));
		settings.bFavourSilkTouch = _config.getBoolean(Functions.localize(Constants.SUBSTITUTION_ID + ".favour_silk"), Constants.SUBSTITUTION_ID, Settings.defaults.bFavourSilkTouch, Functions.localize(Constants.SUBSTITUTION_ID + ".favour_silk.desc"));
		settings.bFavourFortune = _config.getBoolean(Functions.localize(Constants.SUBSTITUTION_ID + ".favour_fortune"), Constants.SUBSTITUTION_ID, Settings.defaults.bFavourFortune, Functions.localize(Constants.SUBSTITUTION_ID + ".favour_fortune.desc"));
		settings.bIgnoreIfValidTool = _config.getBoolean(Functions.localize(Constants.SUBSTITUTION_ID + ".ignore_valid"), Constants.SUBSTITUTION_ID, Settings.defaults.bIgnoreIfValidTool, Functions.localize(Constants.SUBSTITUTION_ID + ".ignore_valid.desc"));
		settings.bIgnorePassiveMobs = _config.getBoolean(Functions.localize(Constants.SUBSTITUTION_ID + ".ignore_passive"), Constants.SUBSTITUTION_ID, Settings.defaults.bIgnorePassiveMobs, Functions.localize(Constants.SUBSTITUTION_ID + ".ignore_passive.desc"));
		
		List<String> order = new ArrayList<String>(6);
		order.add(Functions.localize(Constants.SUBSTITUTION_ID + ".enabled"));
		order.add(Functions.localize(Constants.SUBSTITUTION_ID + ".switchback"));
		order.add(Functions.localize(Constants.SUBSTITUTION_ID + ".favour_silk"));
		order.add(Functions.localize(Constants.SUBSTITUTION_ID + ".favour_fortune"));
		order.add(Functions.localize(Constants.SUBSTITUTION_ID + ".ignore_valid"));
		order.add(Functions.localize(Constants.SUBSTITUTION_ID + ".ignore_passive"));
		order.add(Functions.localize(Constants.SUBSTITUTION_ID + ".excluded_ids"));
		
		_config.setCategoryPropertyOrder(Constants.SUBSTITUTION_ID, order);
	}
	
	private static void getVeinationSettings() {
		settings.bVeinationEnabled = _config.getBoolean(Functions.localize(Constants.VEINATION_ID + ".enabled"), Constants.VEINATION_ID, Settings.defaults.bVeinationEnabled, Functions.localize(Constants.VEINATION_ID + ".enabled.desc"));
		settings.veinationOres = JsonHelper.ParseObject(_config.getString(Functions.localize(Constants.VEINATION_ID + ".ore_veins"), Constants.VEINATION_ID, Settings.defaults.veinationOres.toString(), Functions.localize(Constants.VEINATION_ID + ".ore_veins.desc")));
		
		List<String> order = new ArrayList<String>(4);
		order.add(Functions.localize(Constants.VEINATION_ID + ".enabled"));
		order.add(Functions.localize(Constants.VEINATION_ID + ".ore_veins"));
		
		_config.setCategoryPropertyOrder(Constants.VEINATION_ID, order);
	}
	
}
