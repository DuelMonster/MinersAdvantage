package uk.co.duelmonster.minersadvantage.common.network.packetids;

public enum PacketId implements IPacketId {
    INVALID,

    GIVE_ITEM,
    SET_HOTBAR_ITEM,

    SYNCHRONIZATION,

    CAPTIVATE,
    CROPINATE,
    CULTIVATE,
    EXCAVATE,
    ILLUMINATE,
    LUMBINATE,
    PATHANATE,
    SHAFTANATE,
    SUBSTITUTE,
    VEINATE,
    VENTILATE,

    ABORT_AGENTS,

    SUPREME_VANTAGE;

    public static final PacketId[] VALUES = values();
}
