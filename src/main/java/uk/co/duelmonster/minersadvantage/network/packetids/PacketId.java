package uk.co.duelmonster.minersadvantage.network.packetids;

public enum PacketId implements IPacketId {
	
	INVALID,
	
	GiveItem,
	SetHotbarItem,
	
	Synchronization,
	
	Captivate,
	Cropinate,
	Cultivate,
	Excavate,
	Illuminate,
	Lumbinate,
	Pathinate,
	Shaftanate,
	Substitute,
	Veinate,
	
	AbortAgents,
	
	SupremeVantage;
	
	public static final PacketId[] VALUES = values();
	
}