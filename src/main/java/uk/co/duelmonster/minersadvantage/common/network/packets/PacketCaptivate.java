package uk.co.duelmonster.minersadvantage.common.network.packets;

import uk.co.duelmonster.minersadvantage.common.network.packetids.PacketId;

public class PacketCaptivate implements IMAPacket {
    @Override
    public PacketId getPacketId() {
        return PacketId.CAPTIVATE;
    }

    public static void process(Object player, PacketCaptivate pkt) {
        // Compatibility no-op.
    }
}
