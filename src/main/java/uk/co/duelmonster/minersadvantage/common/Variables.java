package uk.co.duelmonster.minersadvantage.common;

import java.util.HashMap;
import java.util.UUID;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import uk.co.duelmonster.minersadvantage.MA;
import uk.co.duelmonster.minersadvantage.client.ClientFunctions;
import uk.co.duelmonster.minersadvantage.network.packets.PacketSynchronization;

public class Variables {

  private static HashMap<UUID, Variables> playerVariables = new HashMap<UUID, Variables>();

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
    this.prevHeldItem = Constants.EMPTY_ITEMSTACK;
  }

  public static void syncToPlayer(ServerPlayer playerEntity) {

    if (playerEntity != null) {
      Variables variables = Variables.get();
      if (variables.hasChanged())
        MA.NETWORK.sendTo(playerEntity, new PacketSynchronization(playerEntity.getUUID(), SyncType.Variables, JsonHelper.toJson(variables)));
    }

  }

  public static void syncToServer() {

    LocalPlayer player = ClientFunctions.getPlayer();
    if (player != null) {
      Variables variables = Variables.get();
      if (variables.hasChanged())
        MA.NETWORK.sendToServer(new PacketSynchronization(player.getUUID(), SyncType.Variables, JsonHelper.toJson(variables)));
    }

  }

  public boolean hasChanged() {
    String current = JsonHelper.toJson(this);
    if (history == null || history.isEmpty() || !history.equals(current)) {
      history = current;
      return true;
    }

    return false;
  }

  private transient String history = null;

  public boolean HasPlayerSpawned = false;

  public boolean skipNext       = false;
  public boolean skipNextShaft  = false;
  public boolean HungerNotified = false;

  public Direction faceHit = Direction.SOUTH;

  // ====================================================================================================
  // = Substitution variables
  // ====================================================================================================
  public transient ItemStack prevHeldItem;
  public boolean             shouldSwitchBack  = false;
  public boolean             currentlySwitched = false;
  public int                 prevSlot          = -99;
  public int                 optimalSlot       = -99;

  public void resetSubstitution() {
    shouldSwitchBack  = false;
    currentlySwitched = false;
    prevSlot          = -99;
    optimalSlot       = -99;
    prevHeldItem      = Constants.EMPTY_ITEMSTACK;
  }
  // ====================================================================================================

  public boolean IsExcavationToggled   = false;
  public boolean IsSingleLayerToggled  = false;
  public boolean IsShaftanationToggled = false;
  public boolean IsPlayerAttacking     = false;
  public boolean IsCropinating         = false;
  public boolean IsExcavating          = false;
  public boolean IsIlluminating        = false;
  public boolean IsLumbinating         = false;
  public boolean IsPathanating         = false;
  public boolean IsShaftanating        = false;
  public boolean IsVeinating           = false;

  public boolean areAgentsProcessing() {
    return (IsCropinating ||
        IsExcavating ||
        IsIlluminating ||
        IsLumbinating ||
        IsPathanating ||
        IsShaftanating ||
        IsVeinating);
  }

  public boolean IsInToggleMode() {
    return this.IsExcavationToggled || this.IsSingleLayerToggled;
  }
}
