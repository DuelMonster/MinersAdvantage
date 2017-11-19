package co.uk.duelmonster.minersadvantage.settings;

import java.util.HashMap;
import java.util.UUID;

import com.google.gson.JsonObject;

import co.uk.duelmonster.minersadvantage.common.Constants;
import co.uk.duelmonster.minersadvantage.common.JsonHelper;
import net.minecraft.init.Items;

public class Settings {
	
	public static Settings defaults = new Settings();
	
	private static HashMap<UUID, Settings> playerSettings = new HashMap<UUID, Settings>();
	
	public static Settings get() {
		return get(Constants.instanceUID);
	}
	
	public static Settings get(UUID uid) {
		if (playerSettings.isEmpty() || playerSettings.get(uid) == null)
			set(uid, new Settings());
		
		return playerSettings.get(uid);
	}
	
	public static Settings set(UUID uid, Settings settings) {
		return playerSettings.put(uid, settings);
	}
	
	public boolean hasChanged() {
		String current = JsonHelper.gson.toJson(this);
		if (current.equals(current) || history == null || history.isEmpty()) {
			history = current;
			return true;
		}
		
		return false;
	}
	
	private transient String history = null;
	
	// Common Settings
	public boolean	tpsGuard		= true;
	public boolean	bGatherDrops	= false;
	public boolean	bAutoIlluminate	= true;
	public boolean	bMineVeins		= true;
	public int		iBreakSpeed		= 2;
	
	// Captivation Settings
	public boolean		bCaptivationEnabled		= true;
	public boolean		bAllowInGUI				= false;
	public boolean		bIsWhitelist			= false;
	public double		radiusHorizontal		= 16;
	public double		radiusVertical			= 16;
	public JsonObject	captivationBlacklist	= JsonHelper.ParseObject("{\"" + Items.ROTTEN_FLESH.getUnlocalizedName() + "\":\"\"}");
	
	// Cropination Settings
	public boolean	bCropinationEnabled	= true;
	public boolean	bHarvestSeeds		= true;
	
	// Excavation + Pathanation Settings
	public boolean		bExcavationEnabled		= true;
	public boolean		bIgnoreBlockVariants	= false;
	public boolean		bToggleMode				= false;
	public boolean		bIsToolWhitelist		= false;
	public boolean		bIsBlockWhitelist		= false;
	public int			iBlockRadius			= Constants.MIN_BLOCKRADIUS;
	public int			iBlockLimit				= (Constants.MIN_BLOCKRADIUS * Constants.MIN_BLOCKRADIUS) * Constants.MIN_BLOCKRADIUS;
	public int			iPathWidth				= 3;
	public int			iPathLength				= 6;
	public JsonObject	toolBlacklist			= new JsonObject();
	public JsonObject	blockBlacklist			= new JsonObject();
	
	// Illumination Settings
	public boolean	bIlluminationEnabled	= true;
	public int		iLowestLightLevel		= 7;
	
	// Lumbination Settings
	public boolean		bLumbinationEnabled		= true;
	public boolean		bChopTreeBelow			= true;
	public boolean		bDestroyLeaves			= true;
	public boolean		bLeavesAffectDurability	= false;
	public int			iLeafRange				= 3;
	public JsonObject	lumbinationLogs			= new JsonObject();
	public JsonObject	lumbinationLeaves		= new JsonObject();
	public JsonObject	lumbinationAxes			= new JsonObject();
	
	// Shaftanation Settings
	public boolean	bShaftanationEnabled	= true;
	public int		iShaftLength			= 16;
	public int		iShaftHeight			= 2;
	public int		iShaftWidth				= 1;
	
	// Substitution Settings
	public boolean	bSubstitutionEnabled	= true;
	public boolean	bSwitchBack				= true;
	public boolean	bFavourSilkTouch		= false;
	public boolean	bFavourFortune			= true;
	public boolean	bIgnoreIfValidTool		= true;
	public boolean	bIgnorePassiveMobs		= true;
	
	// Veination Settings
	public boolean		bVeinationEnabled	= true;
	public JsonObject	veinationOres		= new JsonObject();
	
	public boolean allEnabled() {
		return (bCaptivationEnabled &&
				bCropinationEnabled &&
				bExcavationEnabled &&
				bLumbinationEnabled &&
				bIlluminationEnabled &&
				bShaftanationEnabled &&
				bSubstitutionEnabled &&
				bVeinationEnabled);
	}
}
