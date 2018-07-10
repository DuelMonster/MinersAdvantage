package co.uk.duelmonster.minersadvantage.common;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import net.minecraft.util.text.TextFormatting;

public final class Constants {
	
	public final static UUID instanceUID = UUID.randomUUID();
	
	public static final String	MOD_ID			= "minersadvantage";
	public static final String	MOD_NAME		= "MinersAdvantage";
	public static final String	MOD_NAME_MSG	= TextFormatting.DARK_PURPLE + "[" + Constants.MOD_NAME + "] ";
	
	// This number is incremented every time we remove deprecated code or make major changes, never reset
	public static final int majorVersion = 1;
	// This number is incremented every minecraft release, never reset
	public static final int minorVersion = 1;
	// This number is incremented every time new features are added, never reset
	public static final int revisionVersion = 16;
	// This number is incremented every time a release is built, and never reset
	public static final int buildVersion = 197;
	
	// This is the full version number of our mod
	public static final String MOD_VERSION = majorVersion + "." + minorVersion + "." + revisionVersion + "." + buildVersion;
	
	// This is the minecraft version we're building for
	public static final String MC_VERSION = "1.10.2";
	
	public static final String	CHANNEL		= "MA_NET_CHAN";
	public static final String	FVC_URL		= "http://www.duelmonster.talktalk.net/Minecraft/MinersAdvantage/forge_update.json";
	public static final String	NAMESPACE	= "co.uk.duelmonster.minersadvantage.";
	public static final String	PROXY		= NAMESPACE + "proxies.";
	public static final String	GUI_FACTORY	= NAMESPACE + "client.config.ConfigGuiFactory";
	
	public static final String	COMMON_ID		= Constants.MOD_ID + ".common";
	public static final String	SERVER_ID		= Constants.MOD_ID + ".server";
	public static final String	CAPTIVATION_ID	= Constants.MOD_ID + ".captivation";
	public static final String	CROPINATION_ID	= Constants.MOD_ID + ".cropination";
	public static final String	EXCAVATION_ID	= Constants.MOD_ID + ".excavation";
	public static final String	ILLUMINATION_ID	= Constants.MOD_ID + ".illumination";
	public static final String	LUMBINATION_ID	= Constants.MOD_ID + ".lumbination";
	public static final String	SHAFTANATION_ID	= Constants.MOD_ID + ".shaftanation";
	public static final String	SUBSTITUTION_ID	= Constants.MOD_ID + ".substitution";
	public static final String	VEINATION_ID	= Constants.MOD_ID + ".veination";
	
	public static final int	MIN_HUNGER			= 1;
	public static final int	DEFAULT_BLOCKRADIUS	= 8;
	public static final int	MIN_BLOCKRADIUS		= 2;
	public static final int	MAX_BLOCKRADIUS		= 128;
	public static final int	MAX_BLOCKLIMIT		= (MAX_BLOCKRADIUS * MAX_BLOCKRADIUS) * MAX_BLOCKRADIUS;
	public static final int	MIN_BREAKSPEED		= 1;
	public static final int	MAX_BREAKSPEED		= 32;
	public static final int	MAX_TREE_WIDTH		= 128;
	
	public static final List<Ranking> RANKING_SILK_TOUCH = new LinkedList<Ranking>();
	static {
		RANKING_SILK_TOUCH.add(Ranking.EFFICIENCY_SILK_TOUCH);
		RANKING_SILK_TOUCH.add(Ranking.SILK_TOUCH);
		RANKING_SILK_TOUCH.add(Ranking.EFFICIENCY_FORTUNE);
		RANKING_SILK_TOUCH.add(Ranking.FORTUNE);
		RANKING_SILK_TOUCH.add(Ranking.EFFICIENCY);
		RANKING_SILK_TOUCH.add(Ranking.NONE);
	};
	
	public static final List<Ranking> RANKING_FORTUNE = new LinkedList<Ranking>();
	static {
		RANKING_FORTUNE.add(Ranking.EFFICIENCY_FORTUNE);
		RANKING_FORTUNE.add(Ranking.FORTUNE);
		RANKING_FORTUNE.add(Ranking.EFFICIENCY_SILK_TOUCH);
		RANKING_FORTUNE.add(Ranking.SILK_TOUCH);
		RANKING_FORTUNE.add(Ranking.EFFICIENCY);
		RANKING_FORTUNE.add(Ranking.NONE);
	};
	
	public static final List<Ranking> RANKING_DEFAULT = new LinkedList<Ranking>();
	static {
		RANKING_DEFAULT.add(Ranking.EFFICIENCY);
		RANKING_DEFAULT.add(Ranking.EFFICIENCY_FORTUNE);
		RANKING_DEFAULT.add(Ranking.EFFICIENCY_SILK_TOUCH);
		RANKING_DEFAULT.add(Ranking.FORTUNE);
		RANKING_DEFAULT.add(Ranking.SILK_TOUCH);
		RANKING_DEFAULT.add(Ranking.NONE);
	};
}
