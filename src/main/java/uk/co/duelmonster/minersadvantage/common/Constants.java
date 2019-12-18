package uk.co.duelmonster.minersadvantage.common;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.loading.FMLPaths;

public class Constants {
	
	public static final Logger LOGGER = LogManager.getLogger();
	
	public static final ItemStack	EMPTY_ITEMSTACK;
	public static final ItemStack	DUMMY_SILKTOUCH;
	
	static {
		ItemStack emptyStack;
		try {
			emptyStack = ItemStack.EMPTY;
		}
		catch (NoSuchFieldError err) {
			emptyStack = null;
		}
		EMPTY_ITEMSTACK = emptyStack;
		
		DUMMY_SILKTOUCH = new ItemStack(Items.DIAMOND_PICKAXE);
		DUMMY_SILKTOUCH.setCount(1);
		DUMMY_SILKTOUCH.addEnchantment(Enchantments.SILK_TOUCH, 1);
	}
	
	private static File _configFolder = null;
	
	public static final File getConfigFolder() {
		if (_configFolder == null)
			_configFolder = new File(FMLPaths.CONFIGDIR.get().toFile(), Constants.MOD_ID);
		
		if (!_configFolder.exists())
			try {
				if (!_configFolder.mkdir())
					throw new RuntimeException("Could not create config directory " + _configFolder);
				
			}
			catch (SecurityException e) {
				throw new RuntimeException("Could not create config directory " + _configFolder, e);
			}
		
		return _configFolder;
	}
	
	public final static UUID instanceUID = UUID.randomUUID();
	
	public static final String	MOD_ID			= "minersadvantage";
	public static final String	MOD_NAME		= "MinersAdvantage";
	public static final String	MOD_NAME_MSG	= TextFormatting.DARK_PURPLE + "[" + Constants.MOD_NAME + "] ";
	
	public static final String				PROTOCOL_VERSION	= "1.4";
	public static final ResourceLocation	CHANNEL_ID			= new ResourceLocation(Constants.MOD_ID, "main_channel");
	
	public static final String	FVC_URL		= "http://duelmonster.000webhostapp.com/Minecraft/MinersAdvantage/forge_update.json";
	public static final String	NAMESPACE	= "uk.co.duelmonster.minersadvantage.";
	public static final String	PROXY		= NAMESPACE + "proxies.";
	public static final String	GUI_FACTORY	= NAMESPACE + "client.config.ConfigGuiFactory";
	
	public static final String	COMMON_ID		= Constants.MOD_ID + ".common";
	public static final String	SERVER_ID		= Constants.MOD_ID + ".server";
	public static final String	CAPTIVATION_ID	= Constants.MOD_ID + ".captivation";
	public static final String	CROPINATION_ID	= Constants.MOD_ID + ".cropination";
	public static final String	EXCAVATION_ID	= Constants.MOD_ID + ".excavation";
	public static final String	PATHANATION_ID	= Constants.MOD_ID + ".pathanation";
	public static final String	ILLUMINATION_ID	= Constants.MOD_ID + ".illumination";
	public static final String	LUMBINATION_ID	= Constants.MOD_ID + ".lumbination";
	public static final String	SHAFTANATION_ID	= Constants.MOD_ID + ".shaftanation";
	public static final String	SUBSTITUTION_ID	= Constants.MOD_ID + ".substitution";
	public static final String	VEINATION_ID	= Constants.MOD_ID + ".veination";
	
	public static final int	MIN_HUNGER			= 1;
	public static final int	DEFAULT_BLOCKRADIUS	= 5;
	public static final int	MIN_BLOCKRADIUS		= 2;
	public static final int	MAX_BLOCKRADIUS		= 128;
	public static final int	DEFAULT_BLOCKLIMIT	= (DEFAULT_BLOCKRADIUS * DEFAULT_BLOCKRADIUS) * DEFAULT_BLOCKRADIUS;
	public static final int	MIN_BLOCKLIMIT		= (MIN_BLOCKRADIUS * MIN_BLOCKRADIUS) * MIN_BLOCKRADIUS;
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
