package uk.co.duelmonster.minersadvantage.common.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.TorchPlacement;
import uk.co.duelmonster.minersadvantage.common.network.packetids.PacketId;

public class PacketIlluminate extends BaseBlockPacket {
    public final BlockPos startPos;
    public final BlockPos endPos;
    public final TorchPlacement placement;

    public PacketIlluminate(BlockPos pos, Direction faceHit) {
        super(pos, faceHit, 0);
        this.startPos = pos;
        this.endPos = pos;
        this.placement = TorchPlacement.FLOOR;
    }

    public PacketIlluminate(PacketId ignored, BlockPos startPos, BlockPos endPos, TorchPlacement placement) {
        super(startPos, Direction.UP, 0);
        this.startPos = startPos;
        this.endPos = endPos;
        this.placement = placement == null ? TorchPlacement.FLOOR : placement;
    }

    public PacketIlluminate(BlockPos pos, Direction faceHit, int stateID) {
        super(pos, faceHit, stateID);
        this.startPos = pos;
        this.endPos = pos;
        this.placement = TorchPlacement.FLOOR;
    }

    @Override
    public PacketId getPacketId() {
        return PacketId.ILLUMINATE;
    }

    public static void process(Object player, PacketIlluminate pkt) {
        // Compatibility no-op.
    }
}
