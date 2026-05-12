package uk.co.duelmonster.minersadvantage.common.network.packetids;

/**
 * PacketId is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
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
