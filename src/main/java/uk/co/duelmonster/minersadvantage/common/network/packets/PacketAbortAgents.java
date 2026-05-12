package uk.co.duelmonster.minersadvantage.common.network.packets;

import uk.co.duelmonster.minersadvantage.common.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.common.workers.AgentProcessor;

public class PacketAbortAgents implements IMAPacket {
    @Override
    public PacketId getPacketId() {
        return PacketId.ABORT_AGENTS;
    }

    public static void process(Object player, PacketAbortAgents pkt) {
        AgentProcessor.INSTANCE.resetAgentList();
        PacketProcessSupport.markAgentsStopped(player);
    }
}
