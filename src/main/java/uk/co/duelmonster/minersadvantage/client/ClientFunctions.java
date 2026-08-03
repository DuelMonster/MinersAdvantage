package uk.co.duelmonster.minersadvantage.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import uk.co.duelmonster.minersadvantage.common.Functions;

/**
 * Legacy compatibility facade for client utility calls.
 */
public final class ClientFunctions {
  public static Minecraft mc = Minecraft.getInstance();

  private ClientFunctions() {
  }

  /**
   * getPlayer exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public static LocalPlayer getPlayer() {
    return mc != null ? mc.player : null;
  }

  /**
   * isAttacking exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public static boolean isAttacking() {
    return mc != null && mc.options != null && mc.options.keyAttack.isDown();
  }

  /**
   * isUsingItem exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public static boolean isUsingItem() {
    return mc != null && mc.options != null && mc.options.keyUse.isDown();
  }

  /**
   * DebugNotifyClient exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public static void DebugNotifyClient(String message) {
    LocalPlayer player = getPlayer();
    if (player != null) {
      Functions.DebugNotifyClient(player, message);
    }
  }

  /**
   * DebugNotifyClient exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public static void DebugNotifyClient(boolean enabled, String featureName) {
    LocalPlayer player = getPlayer();
    if (player != null) {
      Functions.DebugNotifyClient(player, enabled, featureName);
    }
  }

  /**
   * NotifyClient exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public static void NotifyClient(String message) {
    LocalPlayer player = getPlayer();
    if (player != null) {
      Functions.NotifyClient(player, message);
    }
  }

  /**
   * NotifyClient exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public static void NotifyClient(boolean enabled, String featureName) {
    LocalPlayer player = getPlayer();
    if (player != null) {
      Functions.NotifyClient(player, enabled, featureName);
    }
  }

  /**
   * playSound exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public static void playSound(Level world, SoundEvent sound, BlockPos pos) {
    LocalPlayer player = getPlayer();
    world.playSound(player, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, sound, SoundSource.BLOCKS, 2.0F,
        1.0F);
  }

  /**
   * doJoinWorldEventStuff exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public static void doJoinWorldEventStuff() {
    // Update notifier is platform-specific in the new architecture. (future-you will thank present-you).
  }

  /**
   * syncCurrentPlayItem exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public static void syncCurrentPlayItem(int slotIndex) {
    LocalPlayer player = getPlayer();
    if (player == null) {
      return;
    }
    try {
      Object inventory = player.getInventory();
      Method setter = findSelectedSlotSetter(inventory.getClass());
      if (setter != null) {
        setter.invoke(inventory, slotIndex);
      } else {
        Field selected = findSelectedSlotField(inventory.getClass());
        if (selected == null) {
          return;
        }
        selected.setAccessible(true);
        selected.setInt(inventory, slotIndex);
      }
    } catch (Exception noSetter) {
      return;
    }
    if (mc.gameMode != null) {
      mc.gameMode.tick();
    }
    Functions.sleep(100);
  }

  private static Method findSelectedSlotSetter(Class<?> inventoryType) {
    for (Method method : inventoryType.getMethods()) {
      if (method.getParameterCount() != 1 || method.getParameterTypes()[0] != int.class
          || method.getReturnType() != void.class) {
        continue;
      }

      String lowered = method.getName().toLowerCase(java.util.Locale.ROOT);
      // Prefer semantic candidates but keep signature-based filtering as the primary compatibility gate.
      if (lowered.contains("select") || lowered.contains("slot") || lowered.contains("held")) {
        return method;
      }
    }

    for (Method method : inventoryType.getMethods()) {
      if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == int.class
          && method.getReturnType() == void.class) {
        return method;
      }
    }
    return null;
  }

  private static Field findSelectedSlotField(Class<?> inventoryType) {
    for (Field field : inventoryType.getDeclaredFields()) {
      if (field.getType() != int.class) {
        continue;
      }

      String lowered = field.getName().toLowerCase(java.util.Locale.ROOT);
      if (lowered.contains("select") || lowered.contains("slot") || lowered.contains("held")) {
        return field;
      }
    }
    return null;
  }
}
