package co.uk.duelmonster.minersadvantage.config;

public class MAServerConfig extends MAConfig {
	
	// ====================================================================================================
	// = Non-config variables
	// ====================================================================================================
	
	public static MAServerConfig	serverSettings	= new MAServerConfig();
	public static MAServerConfig	defaults		= new MAServerConfig();
	
	// ====================================================================================================
	// = Sub Categories
	// ====================================================================================================
	
	public MAConfig_Server server = new MAConfig_Server();
	
	// ====================================================================================================
	// = Config retrieval functions
	// ====================================================================================================
	
	public static MAServerConfig get() {
		return serverSettings;
	}
	
}
