package uk.co.duelmonster.minersadvantage.common.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.network.packetids.PacketId;

public class PacketLumbinate extends BaseBlockPacket {
    public PacketLumbinate(BlockPos pos, Direction faceHit, int stateID) {
        super(pos, faceHit, stateID);
    }

    @Override
    public PacketId getPacketId() {
        return PacketId.LUMBINATE;
    }

    public static void process(Object player, PacketLumbinate pkt) {
        PacketProcessSupport.dispatchFeature(player, FeatureId.LUMBINATION, pkt);
    }
}
