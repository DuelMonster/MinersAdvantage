package co.uk.duelmonster.minersadvantage.client.gui;

import java.util.ArrayList;
import java.util.List;

import co.uk.duelmonster.minersadvantage.client.config.ConfigHandler;
import co.uk.duelmonster.minersadvantage.common.Constants;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.fml.client.config.DummyConfigElement;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

public class GuiConfigMA extends GuiConfig {
	public GuiConfigMA(GuiScreen parentScreen) {
		super(parentScreen,
				getConfigElements(),
				Constants.MOD_ID,
				false,
				false,
				GuiConfig.getAbridgedConfigPath(ConfigHandler.getConfig().toString()));
	}
	
	/** Compiles a list of config elements */
	private static List<IConfigElement> getConfigElements() {
		List<IConfigElement> list = new ArrayList<IConfigElement>();
		
		// Add categories to config GUI
		list.add(categoryElement(Constants.COMMON_ID, "Common", Constants.COMMON_ID + ".config"));
		list.add(categoryElement(Constants.CAPTIVATION_ID, "Captivation", Constants.CAPTIVATION_ID + ".config"));
		list.add(categoryElement(Constants.CROPINATION_ID, "Cropination", Constants.CROPINATION_ID + ".config"));
		list.add(categoryElement(Constants.EXCAVATION_ID, "Excavation", Constants.EXCAVATION_ID + ".config"));
		list.add(categoryElement(Constants.ILLUMINATION_ID, "Illumination", Constants.ILLUMINATION_ID + ".config"));
		list.add(categoryElement(Constants.LUMBINATION_ID, "Lumbination", Constants.LUMBINATION_ID + ".config"));
		list.add(categoryElement(Constants.SHAFTANATION_ID, "Shaftanation", Constants.SHAFTANATION_ID + ".config"));
		list.add(categoryElement(Constants.SUBSTITUTION_ID, "Substitution", Constants.SUBSTITUTION_ID + ".config"));
		list.add(categoryElement(Constants.VEINATION_ID, "Veination", Constants.VEINATION_ID + ".config"));
		
		return list;
	}
	
	/** Creates a button linking to another screen where all options of the category are available */
	private static IConfigElement categoryElement(String category, String name, String tooltip_key) {
		return new DummyConfigElement.DummyCategoryElement(name, tooltip_key, new ConfigElement(ConfigHandler.getCategory(category)).getChildElements());
	}
}
