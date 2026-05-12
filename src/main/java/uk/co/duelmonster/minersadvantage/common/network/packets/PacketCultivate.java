package uk.co.duelmonster.minersadvantage.common.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.network.packetids.PacketId;

public class PacketCultivate extends BaseBlockPacket {
    public PacketCultivate(BlockPos pos) {
        super(pos, Direction.UP, 0);
    }

    public PacketCultivate(BlockPos pos, Direction faceHit, int stateID) {
        super(pos, faceHit, stateID);
    }

    @Override
    public PacketId getPacketId() {
        return PacketId.CULTIVATE;
    }

    public static void process(Object player, PacketCultivate pkt) {
        // Compatibility no-op.
    }
}
