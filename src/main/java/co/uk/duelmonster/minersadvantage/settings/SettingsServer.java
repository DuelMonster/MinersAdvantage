package co.uk.duelmonster.minersadvantage.settings;

public class SettingsServer extends Settings {
	
	public static SettingsServer	serverSettings	= new SettingsServer();
	public static SettingsServer	defaults		= new SettingsServer();
	
	public boolean	bOverrideFeatureEnablement		= false;
	public boolean	bEnforceCommonSettings			= false;
	public boolean	bEnforceCaptivationSettings		= false;
	public boolean	bEnforceCropinationSettings		= false;
	public boolean	bEnforceExcavationSettings		= false;
	public boolean	bEnforceIlluminationSettings	= false;
	public boolean	bEnforceLumbinationSettings		= false;
	public boolean	bEnforceShaftanationSettings	= false;
	public boolean	bEnforceSubstitutionSettings	= false;
	public boolean	bEnforceVeinationSettings		= false;
	
	public static SettingsServer get() {
		return serverSettings;
	}
}
