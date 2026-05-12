package uk.co.duelmonster.minersadvantage.common.network.packetids;

/**
 * IPacketId is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public interface IPacketId {
    int ordinal();
}
