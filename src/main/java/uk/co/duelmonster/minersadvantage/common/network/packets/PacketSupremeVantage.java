package uk.co.duelmonster.minersadvantage.common.network.packets;

import uk.co.duelmonster.minersadvantage.common.network.packetids.PacketId;

/**
 * PacketSupremeVantage is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public class PacketSupremeVantage implements IMAPacket {
    public final String code;

    /**
     * PacketSupremeVantage exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public PacketSupremeVantage(String code) {
        this.code = code == null ? "" : code;
    }

    @Override
    /**
     * getPacketId exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public PacketId getPacketId() {
        return PacketId.SUPREME_VANTAGE;
    }

    /**
     * Deprecated: Legacy compatibility handler removed. All SupremeVantage logic is migrated to SupremeVantagePacket and SupremeVantageService.
     */
    @Deprecated
    /**
     * process exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void process(Object player, PacketSupremeVantage pkt) {
        // Why this exists: No-op: This handler is intentionally left blank. See SupremeVantagePacket for modern logic. (future-you will thank present-you).
    }
}
