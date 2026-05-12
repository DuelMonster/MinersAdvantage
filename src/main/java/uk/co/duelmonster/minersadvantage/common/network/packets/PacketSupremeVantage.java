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

    public static void process(Object player, PacketSupremeVantage pkt) {
        // Compatibility no-op.
    }
}
