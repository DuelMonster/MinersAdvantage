package co.uk.duelmonster.minersadvantage.common;

import java.util.HashMap;
import java.util.Map;

public enum PacketID {
	GODITEMS(-1),
	
	SyncSettings(0),
	
	SyncVariables(1),
	
	Captivate(2),
	
	Excavate(3), Veinate(4),
	
	Shaftanate(5),
	
	Illuminate(6),
	
	Lumbinate(7),
	
	LayPath(8),
	
	Substitution(9),
	
	TileFarmland(10),
	
	HarvestCrops(11);
	
	private final int value;
	
	PacketID(int value) {
		this.value = value;
	}
	
	public int value() {
		return value;
	}
	
	private static Map<Integer, PacketID> map = new HashMap<Integer, PacketID>();
	
	static {
		for (PacketID pID : PacketID.values()) {
			map.put(pID.value, pID);
		}
	}
	
	public static PacketID valueOf(int val) {
		return map.get(val);
	}
}
