package uk.co.duelmonster.minersadvantage.common.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.network.packetids.PacketId;

public class PacketVeinate extends BaseBlockPacket {
    public PacketVeinate(BlockPos pos, Direction faceHit, int stateID) {
        super(pos, faceHit, stateID);
    }

    @Override
    public PacketId getPacketId() {
        return PacketId.VEINATE;
    }

    public static void process(Object player, PacketVeinate pkt) {
        // Compatibility no-op.
    }
}
