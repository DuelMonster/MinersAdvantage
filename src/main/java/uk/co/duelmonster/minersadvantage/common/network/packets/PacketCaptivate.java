package uk.co.duelmonster.minersadvantage.common.network.packets;

import uk.co.duelmonster.minersadvantage.common.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

public class PacketCaptivate implements IMAPacket {
    @Override
    public PacketId getPacketId() {
        return PacketId.CAPTIVATE;
    }

    public static void process(Object player, PacketCaptivate pkt) {
        PacketProcessSupport.dispatchFeature(player, FeatureId.CAPTIVATION);
    }
}
