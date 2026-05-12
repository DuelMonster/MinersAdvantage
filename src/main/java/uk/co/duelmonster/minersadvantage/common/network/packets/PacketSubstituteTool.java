package uk.co.duelmonster.minersadvantage.common.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.network.packetids.PacketId;

public class PacketSubstituteTool extends BaseBlockPacket {
    public final int slot;

    public PacketSubstituteTool(int slot) {
        super(BlockPos.ZERO, Direction.UP, 0);
        this.slot = slot;
    }

    public PacketSubstituteTool(BlockPos pos) {
        super(pos, Direction.UP, 0);
        this.slot = -1;
    }

    public PacketSubstituteTool(BlockPos pos, Direction faceHit, int stateID) {
        super(pos, faceHit, stateID);
        this.slot = -1;
    }

    @Override
    public PacketId getPacketId() {
        return PacketId.SUBSTITUTE;
    }

    public static void process(Object player, PacketSubstituteTool pkt) {
        PacketProcessSupport.dispatchSubstitution(player, pkt);
    }
}
