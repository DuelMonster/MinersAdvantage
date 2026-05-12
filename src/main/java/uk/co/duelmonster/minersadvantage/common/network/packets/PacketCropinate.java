package uk.co.duelmonster.minersadvantage.common.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.network.packetids.PacketId;

public class PacketCropinate extends BaseBlockPacket {
    public PacketCropinate(BlockPos pos) {
        super(pos, Direction.UP, 0);
    }

    public PacketCropinate(BlockPos pos, Direction faceHit, int stateID) {
        super(pos, faceHit, stateID);
    }

    @Override
    public PacketId getPacketId() {
        return PacketId.CROPINATE;
    }

    public static void process(Object player, PacketCropinate pkt) {
        PacketProcessSupport.dispatchFeature(player, FeatureId.CROPINATION, pkt);
    }
}
