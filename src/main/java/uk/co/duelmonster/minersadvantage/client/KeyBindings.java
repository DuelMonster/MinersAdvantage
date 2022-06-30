package uk.co.duelmonster.minersadvantage.client;

import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.InputConstants.Key;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import uk.co.duelmonster.minersadvantage.common.Constants;

@OnlyIn(Dist.CLIENT)
public class KeyBindings {

  public static final KeyMapping captivation;
  public static final KeyMapping excavation;
  public static final KeyMapping excavation_toggle;
  public static final KeyMapping excavation_layer_only_toggle;
  public static final KeyMapping illumination;
  public static final KeyMapping illumination_place;
  public static final KeyMapping illumination_area;
  public static final KeyMapping lumbination;
  public static final KeyMapping shaftanation;
  public static final KeyMapping shaft_vent_toggle;
  public static final KeyMapping substitution;
  public static final KeyMapping veination;
  public static final KeyMapping cropination;
  public static final KeyMapping abortAgents;
  // public static final KeyMapping inGameConfig;

  private static final List<KeyMapping> allBindings;

  public static Key getKey(int key) {
    return InputConstants.Type.KEYSYM.getOrCreate(key);
  }

  static {
    allBindings = ImmutableList.of(
        captivation = new KeyMapping(Constants.MOD_ID + ".captivation.enabled.comment", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_KP_1), Constants.MOD_NAME), // Keyboard.KEY_NUMPAD1
        excavation = new KeyMapping(Constants.MOD_ID + ".excavation.enabled.comment", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_KP_2), Constants.MOD_NAME), // Keyboard.KEY_NUMPAD2
        excavation_toggle = new KeyMapping(Constants.MOD_ID + ".excavation.toggle", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_GRAVE_ACCENT), Constants.MOD_NAME), // Keyboard.KEY_GRAVE
        excavation_layer_only_toggle = new KeyMapping(Constants.MOD_ID + ".excavation.toggle.sl", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_TAB), Constants.MOD_NAME), // Keyboard.KEY_TAB
        illumination = new KeyMapping(Constants.MOD_ID + ".illumination.enabled.comment", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_KP_3), Constants.MOD_NAME), // Keyboard.KEY_NUMPAD3
        illumination_place = new KeyMapping(Constants.MOD_ID + ".illumination.place", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_V), Constants.MOD_NAME), // Keyboard.KEY_V
        illumination_area = new KeyMapping(Constants.MOD_ID + ".illumination.area", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_F12), Constants.MOD_NAME), // Keyboard.KEY_F12
        lumbination = new KeyMapping(Constants.MOD_ID + ".lumbination.enabled.comment", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_KP_4), Constants.MOD_NAME), // Keyboard.KEY_NUMPAD4
        shaftanation = new KeyMapping(Constants.MOD_ID + ".shaftanation.enabled.comment", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_KP_5), Constants.MOD_NAME), // Keyboard.KEY_NUMPAD5
        shaft_vent_toggle = new KeyMapping(Constants.MOD_ID + ".shaft.vent.toggle", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_LEFT_ALT), Constants.MOD_NAME), // Keyboard.KEY_LMENU
        substitution = new KeyMapping(Constants.MOD_ID + ".substitution.enabled.comment", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_KP_6), Constants.MOD_NAME), // Keyboard.KEY_NUMPAD6
        veination = new KeyMapping(Constants.MOD_ID + ".veination.enabled.comment", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_KP_7), Constants.MOD_NAME), // Keyboard.KEY_NUMPAD7
        cropination = new KeyMapping(Constants.MOD_ID + ".cropination.enabled.comment", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_KP_8), Constants.MOD_NAME), // Keyboard.KEY_NUMPAD8
        abortAgents = new KeyMapping(Constants.MOD_ID + ".abort.agents", KeyConflictContext.IN_GAME, KeyModifier.NONE, getKey(GLFW.GLFW_KEY_DELETE), Constants.MOD_NAME) // Keyboard.KEY_DELETE

    // inGameConfig = new KeyMapping(Constants.MOD_ID + ".inGameConfig", KeyConflictContext.IN_GAME,
    // KeyModifier.CONTROL, getKey(GLFW.GLFW_KEY_INSERT), Constants.MOD_NAME) // Keyboard.KEY_DELETE
    );
  }

  public static void registerKeys() {
    // Register the KeyBindings to the ClientRegistry
    for (KeyMapping binding : allBindings)
      ClientRegistry.registerKeyBinding(binding);
  }

  public static boolean isEnterKey(int keyCode) {
    return keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER;
  }

  public static boolean isDown(int keyCode) {
    return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keyCode);
  }

  public static String getKeyName(int keyCode) {
    return GLFW.glfwGetKeyName(keyCode, GLFW.glfwGetKeyScancode(keyCode));
  }

}