package co.uk.duelmonster.minersadvantage.client;

import org.lwjgl.input.Keyboard;

import co.uk.duelmonster.minersadvantage.common.Constants;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class KeyBindings {
	
	// Define the bindings, with unlocalised names "key.???" and
	// the unlocalised category name "key.categories.MinersAdvantage"
	public static final KeyBinding	captivation						= new KeyBinding(Constants.MOD_ID + ".captivation.enabled.desc", Keyboard.KEY_NUMPAD1, Constants.MOD_NAME);
	public static final KeyBinding	excavation						= new KeyBinding(Constants.MOD_ID + ".excavation.enabled.desc", Keyboard.KEY_NUMPAD2, Constants.MOD_NAME);
	public static final KeyBinding	excavation_toggle				= new KeyBinding(Constants.MOD_ID + ".excavation.toggle", Keyboard.KEY_GRAVE, Constants.MOD_NAME);
	public static final KeyBinding	excavation_layer_only_toggle	= new KeyBinding(Constants.MOD_ID + ".excavation.toggle.sl", Keyboard.KEY_BACKSLASH, Constants.MOD_NAME);
	public static final KeyBinding	illumination					= new KeyBinding(Constants.MOD_ID + ".illumination.enabled.desc", Keyboard.KEY_NUMPAD3, Constants.MOD_NAME);
	public static final KeyBinding	illumination_place				= new KeyBinding(Constants.MOD_ID + ".illumination.place", Keyboard.KEY_V, Constants.MOD_NAME);
	public static final KeyBinding	lumbination						= new KeyBinding(Constants.MOD_ID + ".lumbination.enabled.desc", Keyboard.KEY_NUMPAD4, Constants.MOD_NAME);
	public static final KeyBinding	shaftanation					= new KeyBinding(Constants.MOD_ID + ".shaftanation.enabled.desc", Keyboard.KEY_NUMPAD5, Constants.MOD_NAME);
	public static final KeyBinding	shaftanation_toggle				= new KeyBinding(Constants.MOD_ID + ".shaftanation.toggle", Keyboard.KEY_LMENU, Constants.MOD_NAME);
	public static final KeyBinding	substitution					= new KeyBinding(Constants.MOD_ID + ".substitution.enabled.desc", Keyboard.KEY_NUMPAD6, Constants.MOD_NAME);
	public static final KeyBinding	veination						= new KeyBinding(Constants.MOD_ID + ".veination.enabled.desc", Keyboard.KEY_NUMPAD7, Constants.MOD_NAME);
	public static final KeyBinding	cropination						= new KeyBinding(Constants.MOD_ID + ".cropination.enabled.desc", Keyboard.KEY_NUMPAD8, Constants.MOD_NAME);
	public static final KeyBinding	abortAgents						= new KeyBinding(Constants.MOD_ID + ".abort.agents", Keyboard.KEY_DELETE, Constants.MOD_NAME);
	
	public static void registerKeys() {
		// Register the KeyBindings to the ClientRegistry
		ClientRegistry.registerKeyBinding(captivation);
		ClientRegistry.registerKeyBinding(excavation);
		ClientRegistry.registerKeyBinding(excavation_toggle);
		ClientRegistry.registerKeyBinding(excavation_layer_only_toggle);
		ClientRegistry.registerKeyBinding(illumination);
		ClientRegistry.registerKeyBinding(illumination_place);
		ClientRegistry.registerKeyBinding(lumbination);
		ClientRegistry.registerKeyBinding(shaftanation);
		ClientRegistry.registerKeyBinding(shaftanation_toggle);
		ClientRegistry.registerKeyBinding(substitution);
		ClientRegistry.registerKeyBinding(veination);
		ClientRegistry.registerKeyBinding(cropination);
		ClientRegistry.registerKeyBinding(abortAgents);
	}
	
}
