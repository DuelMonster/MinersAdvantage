package uk.co.duelmonster.minersadvantage.common.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.network.packetids.PacketId;

public class PacketShaftanate extends BaseBlockPacket {
    public PacketShaftanate(BlockPos pos, Direction faceHit, int stateID) {
        super(pos, faceHit, stateID);
    }

    @Override
    public PacketId getPacketId() {
        return PacketId.SHAFTANATE;
    }

    public static void process(Object player, PacketShaftanate pkt) {
        // Compatibility no-op.
    }
}
