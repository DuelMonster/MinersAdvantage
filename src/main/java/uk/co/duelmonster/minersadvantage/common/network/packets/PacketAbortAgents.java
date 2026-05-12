package uk.co.duelmonster.minersadvantage.common.network.packets;

import uk.co.duelmonster.minersadvantage.common.network.packetids.PacketId;

public class PacketAbortAgents implements IMAPacket {
    @Override
    public PacketId getPacketId() {
        return PacketId.ABORT_AGENTS;
    }

    public static void process(Object player, PacketAbortAgents pkt) {
        // Compatibility no-op.
    }
}
