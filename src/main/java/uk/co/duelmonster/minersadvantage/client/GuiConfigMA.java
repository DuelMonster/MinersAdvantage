package uk.co.duelmonster.minersadvantage.client;

import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiConfigMA extends Screen {
	
	public GuiConfigMA(Screen parentScreen) {
		super(parentScreen.getTitle());
		// super(parentScreen,
		// getConfigElements(),
		// Constants.MOD_ID,
		// false,
		// false,
		// Constants.MOD_NAME + ".cfg");
	}
	
	/** Compiles a list of config elements */
	// private static List<IConfigElement> getConfigElements() {
	// List<IConfigElement> list = new ArrayList<IConfigElement>();
	//
	// // Add categories to config GUI
	// list.add(categoryElement(Constants.COMMON_ID, "Common", Constants.COMMON_ID + ".config"));
	// list.add(categoryElement(Constants.CAPTIVATION_ID, "Captivation", Constants.CAPTIVATION_ID + ".config"));
	// list.add(categoryElement(Constants.CROPINATION_ID, "Cropination", Constants.CROPINATION_ID + ".config"));
	// list.add(categoryElement(Constants.EXCAVATION_ID, "Excavation", Constants.EXCAVATION_ID + ".config"));
	// list.add(categoryElement(Constants.ILLUMINATION_ID, "Illumination", Constants.ILLUMINATION_ID + ".config"));
	// list.add(categoryElement(Constants.LUMBINATION_ID, "Lumbination", Constants.LUMBINATION_ID + ".config"));
	// list.add(categoryElement(Constants.SHAFTANATION_ID, "Shaftanation", Constants.SHAFTANATION_ID + ".config"));
	// list.add(categoryElement(Constants.SUBSTITUTION_ID, "Substitution", Constants.SUBSTITUTION_ID + ".config"));
	// list.add(categoryElement(Constants.VEINATION_ID, "Veination", Constants.VEINATION_ID + ".config"));
	//
	// return list;
	// }
	
	/** Creates a button linking to another screen where all options of the category are available */
	// private static IConfigElement categoryElement(String category, String name, String tooltip_key) {
	// return new DummyConfigElement.DummyCategoryElement(name, tooltip_key, new ConfigElement(ConfigHandler.getCategory(category)).getChildElements());
	// }
}
