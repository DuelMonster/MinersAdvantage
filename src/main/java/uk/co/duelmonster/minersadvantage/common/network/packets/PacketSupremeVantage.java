package uk.co.duelmonster.minersadvantage.common.network.packets;

import uk.co.duelmonster.minersadvantage.common.network.packetids.PacketId;

public class PacketSupremeVantage implements IMAPacket {
    public final String code;

    public PacketSupremeVantage(String code) {
        this.code = code == null ? "" : code;
    }

    @Override
    public PacketId getPacketId() {
        return PacketId.SUPREME_VANTAGE;
    }

    /**
     * Deprecated: Legacy compatibility handler removed. All SupremeVantage logic is migrated to SupremeVantagePacket and SupremeVantageService.
     */
    @Deprecated
    public static void process(Object player, PacketSupremeVantage pkt) {
        // No-op: This handler is intentionally left blank. See SupremeVantagePacket for modern logic.
    }
}
