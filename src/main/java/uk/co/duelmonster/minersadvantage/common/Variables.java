package uk.co.duelmonster.minersadvantage.common;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class Variables {
    private static final Map<UUID, Variables> playerVariables = new HashMap<>();

    public static Variables get() {
        return get(Constants.instanceUID);
    }

    public static Variables get(UUID uid) {
        if (playerVariables.isEmpty() || playerVariables.get(uid) == null)
            set(uid, new Variables());
        return playerVariables.get(uid);
    }

    public static Variables set(String payload) {
        return set(JsonHelper.fromJson(payload, Variables.class));
    }

    public static Variables set(Variables variables) {
        return set(Constants.instanceUID, variables);
    }

    public static Variables set(UUID uid, String payload) {
        return set(uid, JsonHelper.fromJson(payload, Variables.class));
    }

    public static Variables set(UUID uid, Variables variables) {
        return playerVariables.put(uid, variables);
    }

    public Variables() {
        this.prevHeldItem = ItemStack.EMPTY;
    }

    // --- Core migrated fields from legacy ---
    public boolean HasPlayerSpawned = false;
    public boolean skipNext = false;
    public boolean skipNextShaft = false;
    public boolean HungerNotified = false;

    public Direction faceHit = Direction.SOUTH;

    // Substitution variables
    public transient ItemStack prevHeldItem;
    public boolean shouldSwitchBack = false;
    public boolean currentlySwitched = false;
    public int prevSlot = -99;
    public int optimalSlot = -99;

    // Feature toggles
    public boolean IsExcavationToggled = false;
    public boolean IsSingleLayerToggled = false;
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

    public boolean areAgentsProcessing() {
        return (IsCropinating || IsExcavating || IsIlluminating || IsLumbinating || IsPathanating || IsShaftanating || IsVeinating || IsVentilating);
    }

    public boolean IsInToggleMode() {
        return this.IsExcavationToggled || this.IsSingleLayerToggled;
    }

    private transient String history = null;

    public void resetSubstitution() {
        shouldSwitchBack = false;
        currentlySwitched = false;
        prevSlot = -99;
        optimalSlot = -99;
        prevHeldItem = ItemStack.EMPTY;
    }

    public boolean hasChanged() {
        String current = JsonHelper.toJson(this);
        if (history == null || history.isEmpty() || !history.equals(current)) {
            history = current;
            return true;
        }
        return false;
    }

    public static void syncToPlayer(Object playerEntity) {
        if (playerEntity != null) {
            Variables.get().hasChanged();
        }
    }

    public static void syncToServer() {
        Variables.get().hasChanged();
    }
}
