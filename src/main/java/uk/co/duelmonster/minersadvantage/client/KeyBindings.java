package uk.co.duelmonster.minersadvantage.client;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import uk.co.duelmonster.minersadvantage.common.Constants;

@OnlyIn(Dist.CLIENT)
public class KeyBindings {
	
	public static final KeyBinding	captivation;
	public static final KeyBinding	excavation;
	public static final KeyBinding	excavation_toggle;
	public static final KeyBinding	excavation_layer_only_toggle;
	public static final KeyBinding	illumination;
	public static final KeyBinding	illumination_place;
	public static final KeyBinding	illumination_area;
	public static final KeyBinding	lumbination;
	public static final KeyBinding	shaftanation;
	public static final KeyBinding	shaftanation_toggle;
	public static final KeyBinding	substitution;
	public static final KeyBinding	veination;
	public static final KeyBinding	cropination;
	public static final KeyBinding	abortAgents;
	
	private static final List<KeyBinding> allBindings;
	
	public static InputMappings.Input getKey(int key) {
		return InputMappings.Type.KEYSYM.getOrMakeInput(key);
	}
	
	static {
		allBindings = ImmutableList.of(
				captivation = new KeyBinding(Constants.MOD_ID + ".captivation.enabled.comment", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_KP_1), Constants.MOD_NAME), // Keyboard.KEY_NUMPAD1
				excavation = new KeyBinding(Constants.MOD_ID + ".excavation.enabled.comment", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_KP_2), Constants.MOD_NAME), // Keyboard.KEY_NUMPAD2
				excavation_toggle = new KeyBinding(Constants.MOD_ID + ".excavation.toggle", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_GRAVE_ACCENT), Constants.MOD_NAME), // Keyboard.KEY_GRAVE
				excavation_layer_only_toggle = new KeyBinding(Constants.MOD_ID + ".excavation.toggle.sl", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_TAB), Constants.MOD_NAME), // Keyboard.KEY_TAB
				illumination = new KeyBinding(Constants.MOD_ID + ".illumination.enabled.comment", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_KP_3), Constants.MOD_NAME), // Keyboard.KEY_NUMPAD3
				illumination_place = new KeyBinding(Constants.MOD_ID + ".illumination.place", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_V), Constants.MOD_NAME), // Keyboard.KEY_V
				illumination_area = new KeyBinding(Constants.MOD_ID + ".illumination.area", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_F12), Constants.MOD_NAME), // Keyboard.KEY_F12
				lumbination = new KeyBinding(Constants.MOD_ID + ".lumbination.enabled.comment", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_KP_4), Constants.MOD_NAME), // Keyboard.KEY_NUMPAD4
				shaftanation = new KeyBinding(Constants.MOD_ID + ".shaftanation.enabled.comment", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_KP_5), Constants.MOD_NAME), // Keyboard.KEY_NUMPAD5
				shaftanation_toggle = new KeyBinding(Constants.MOD_ID + ".shaftanation.toggle", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_LEFT_ALT), Constants.MOD_NAME), // Keyboard.KEY_LMENU
				substitution = new KeyBinding(Constants.MOD_ID + ".substitution.enabled.comment", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_KP_6), Constants.MOD_NAME), // Keyboard.KEY_NUMPAD6
				veination = new KeyBinding(Constants.MOD_ID + ".veination.enabled.comment", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_KP_7), Constants.MOD_NAME), // Keyboard.KEY_NUMPAD7
				cropination = new KeyBinding(Constants.MOD_ID + ".cropination.enabled.comment", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_KP_8), Constants.MOD_NAME), // Keyboard.KEY_NUMPAD8
				abortAgents = new KeyBinding(Constants.MOD_ID + ".abort.agents", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_DELETE), Constants.MOD_NAME) // Keyboard.KEY_DELETE
		);
	}
	
	public static void registerKeys() {
		// Register the KeyBindings to the ClientRegistry
		for (KeyBinding binding : allBindings)
			ClientRegistry.registerKeyBinding(binding);
	}
	
	public static boolean isEnterKey(int keyCode) {
		return keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER;
	}
	
	public static boolean isKeyDown(int keyCode) {
		return InputMappings.isKeyDown(Minecraft.getInstance().mainWindow.getHandle(), keyCode);
	}
	
	public static String getKeyName(int keyCode) {
		return GLFW.glfwGetKeyName(keyCode, GLFW.glfwGetKeyScancode(keyCode));
	}
	
}