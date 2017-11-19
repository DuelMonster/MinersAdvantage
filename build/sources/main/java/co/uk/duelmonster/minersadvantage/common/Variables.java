package co.uk.duelmonster.minersadvantage.common;

import java.util.HashMap;
import java.util.UUID;

import net.minecraft.util.EnumFacing;

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
	
	public static Variables set(Variables variables) {
		return set(Constants.instanceUID, variables);
	}
	
	public static Variables set(UUID uid, Variables variables) {
		return playerVariables.put(uid, variables);
	}
	
	public boolean hasChanged() {
		String current = JsonHelper.gson.toJson(this);
		if (current.equals(current) || history == null || history.isEmpty()) {
			history = current;
			return true;
		}
		
		return false;
	}
	
	private transient String history = null;
	
	public boolean	skipNext		= false;
	public boolean	skipNextShaft	= false;
	public boolean	HungerNotified	= false;
	
	public EnumFacing sideHit = EnumFacing.SOUTH;
	
	public boolean	IsExcavationToggled		= false;
	public boolean	IsSingleLayerToggled	= false;
	public boolean	IsShaftanationToggled	= false;
	public boolean	IsPlayerAttacking		= false;
	
	public boolean	IsCropinating	= false;
	public boolean	IsExcavating	= false;
	public boolean	IsLumbinating	= false;
	public boolean	IsPathanating	= false;
	public boolean	IsShaftanating	= false;
	public boolean	IsVeinating		= false;
	
	public boolean IsProcessing() {
		return (IsCropinating ||
				IsExcavating ||
				IsLumbinating ||
				IsPathanating ||
				IsShaftanating ||
				IsVeinating);
	}
}
