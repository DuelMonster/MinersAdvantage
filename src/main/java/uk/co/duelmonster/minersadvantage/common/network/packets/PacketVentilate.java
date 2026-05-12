package uk.co.duelmonster.minersadvantage.common.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.network.packetids.PacketId;

public class PacketVentilate extends BaseBlockPacket {
    public PacketVentilate(BlockPos pos, Direction faceHit, int stateID) {
        super(pos, faceHit, stateID);
    }

    @Override
    public PacketId getPacketId() {
        return PacketId.VENTILATE;
    }

    public static void process(Object player, PacketVentilate pkt) {
        PacketProcessSupport.dispatchFeature(player, FeatureId.VENTILATION, pkt);
    }
}
