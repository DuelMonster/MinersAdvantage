package uk.co.duelmonster.minersadvantage.common;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

/**
 * Variables is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public class Variables {
  private static final Map<UUID, Variables> playerVariables = new HashMap<>();

  /**
   * get exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public static Variables get() {
    return get(Constants.instanceUID);
  }

  /**
   * get exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public static Variables get(UUID uid) {
    if (playerVariables.isEmpty() || playerVariables.get(uid) == null)
      set(uid, new Variables());
    return playerVariables.get(uid);
  }

  /**
   * set exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public static Variables set(String payload) {
    return set(JsonHelper.fromJson(payload, Variables.class));
  }

  /**
   * set exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public static Variables set(Variables variables) {
    return set(Constants.instanceUID, variables);
  }

  /**
   * set exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public static Variables set(UUID uid, String payload) {
    return set(uid, JsonHelper.fromJson(payload, Variables.class));
  }

  /**
   * set exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public static Variables set(UUID uid, Variables variables) {
    return playerVariables.put(uid, variables);
  }

  /**
   * Variables exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public Variables() {
    this.prevHeldItem = ItemStack.EMPTY;
  }

  // --- Core migrated fields from legacy --- (future-you will thank present-you).
  public boolean HasPlayerSpawned = false;
  public boolean skipNext = false;
  public boolean skipNextShaft = false;
  public boolean HungerNotified = false;

  public Direction faceHit = Direction.SOUTH;

  // Substitution variables (future-you will thank present-you).
  public transient ItemStack prevHeldItem;
  public boolean shouldSwitchBack = false;
  public boolean currentlySwitched = false;
  public int prevSlot = -99;
  public int optimalSlot = -99;

  // Feature toggles (future-you will thank present-you).
  public boolean IsExcavationToggled = false;
  public boolean IsShaftanationToggled = false;
  public boolean IsPlayerAttacking = false;
  public boolean IsCropinating = false;
  public boolean IsExcavating = false;
  public boolean IsIlluminating = false;
  public boolean IsLumbinating = false;
  public boolean IsPathanating = false;
  public boolean IsShaftanating = false;
  public boolean IsVeinating = false;
  public boolean IsVentilating = false;

  /**
   * areAgentsProcessing exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public boolean areAgentsProcessing() {
    return (IsCropinating || IsExcavating || IsIlluminating || IsLumbinating || IsPathanating || IsShaftanating
        || IsVeinating || IsVentilating);
  }

  /**
   * IsInToggleMode exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public boolean IsInToggleMode() {
    return this.IsExcavationToggled;
  }

  private transient String history = null;

  /**
   * resetSubstitution exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public void resetSubstitution() {
    shouldSwitchBack = false;
    currentlySwitched = false;
    prevSlot = -99;
    optimalSlot = -99;
    prevHeldItem = ItemStack.EMPTY;
  }

  /**
   * hasChanged exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public boolean hasChanged() {
    String current = JsonHelper.toJson(this);
    if (history == null || history.isEmpty() || !history.equals(current)) {
      history = current;
      return true;
    }
    return false;
  }

  /**
   * syncToPlayer exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public static void syncToPlayer(Object playerEntity) {
    if (playerEntity != null) {
      Variables.get().hasChanged();
    }
  }

  /**
   * syncToServer exists to keep this step focused, predictable, and debuggable.
   * In short: one clear job here beats ten confusing side-effects elsewhere.
   */
  public static void syncToServer() {
    Variables.get().hasChanged();
  }
}
