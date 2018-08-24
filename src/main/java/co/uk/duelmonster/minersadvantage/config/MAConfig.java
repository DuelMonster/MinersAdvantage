package co.uk.duelmonster.minersadvantage.config;

import java.util.HashMap;
import java.util.UUID;

import co.uk.duelmonster.minersadvantage.common.Constants;
import co.uk.duelmonster.minersadvantage.common.JsonHelper;

public class MAConfig {
	
	// ====================================================================================================
	// = Non-config variables
	// ====================================================================================================
	
	public static String	CURRENT_CONFIG_VERSION	= "2.0";
	public static float		CONVERSION_VERSION		= 2.0f;
	public static MAConfig	defaults				= new MAConfig();
	
	private static HashMap<UUID, MAConfig> playerSettings = new HashMap<UUID, MAConfig>();
	
	// transient variables = non-serialised because we don't want these being transfered during a settings sync.
	private transient String		history			= null;
	public transient MAServerConfig	serverOverrides	= null;
	
	// ====================================================================================================
	// = Sub Categories
	// ====================================================================================================
	
	public MAConfig_Common			common			= new MAConfig_Common(this);
	public MAConfig_Captivation		captivation		= new MAConfig_Captivation(this);
	public MAConfig_Cropination		cropination		= new MAConfig_Cropination(this);
	public MAConfig_Excavation		excavation		= new MAConfig_Excavation(this);
	public MAConfig_Pathanation		pathanation		= new MAConfig_Pathanation(this);
	public MAConfig_Illumination	illumination	= new MAConfig_Illumination(this);
	public MAConfig_Lumbination		lumbination		= new MAConfig_Lumbination(this);
	public MAConfig_Shaftanation	shaftanation	= new MAConfig_Shaftanation(this);
	public MAConfig_Substitution	substitution	= new MAConfig_Substitution(this);
	public MAConfig_Veination		veination		= new MAConfig_Veination(this);
	
	public void setSubCategoryParents() {
		common.parentConfig = this;
		captivation.parentConfig = this;
		cropination.parentConfig = this;
		excavation.parentConfig = this;
		pathanation.parentConfig = this;
		illumination.parentConfig = this;
		lumbination.parentConfig = this;
		shaftanation.parentConfig = this;
		substitution.parentConfig = this;
		veination.parentConfig = this;
	}
	
	// ====================================================================================================
	// = Config retrieval functions
	// ====================================================================================================
	
	public static MAConfig get() {
		return get(Constants.instanceUID);
	}
	
	public static MAConfig get(UUID uid) {
		if (playerSettings.isEmpty() || playerSettings.get(uid) == null)
			set(uid, new MAConfig());
		
		return playerSettings.get(uid);
	}
	
	public static MAConfig set(UUID uid, MAConfig settings) {
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
	
	public boolean allEnabled() {
		return (captivation.bEnabled() &&
				cropination.bEnabled() &&
				excavation.bEnabled() &&
				lumbination.bEnabled() &&
				illumination.bEnabled() &&
				pathanation.bEnabled() &&
				shaftanation.bEnabled() &&
				substitution.bEnabled() &&
				veination.bEnabled());
	}
	
}