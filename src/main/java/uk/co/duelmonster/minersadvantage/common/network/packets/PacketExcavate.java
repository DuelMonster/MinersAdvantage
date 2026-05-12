package uk.co.duelmonster.minersadvantage.common.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.network.packetids.PacketId;

public class PacketExcavate extends BaseBlockPacket {
    public PacketExcavate(BlockPos pos, Direction faceHit, int stateID) {
        super(pos, faceHit, stateID);
    }

    @Override
    public PacketId getPacketId() {
        return PacketId.EXCAVATE;
    }

    public static void process(Object player, PacketExcavate pkt) {
        PacketProcessSupport.dispatchFeature(player, FeatureId.EXCAVATION, pkt);
    }
}
