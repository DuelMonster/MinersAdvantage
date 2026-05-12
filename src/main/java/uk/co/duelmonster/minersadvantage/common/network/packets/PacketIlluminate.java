package uk.co.duelmonster.minersadvantage.common.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.TorchPlacement;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.network.packetids.PacketId;

/**
 * PacketIlluminate is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public class PacketIlluminate extends BaseBlockPacket {
    public final BlockPos startPos;
    public final BlockPos endPos;
    public final TorchPlacement placement;

    /**
     * PacketIlluminate exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public PacketIlluminate(BlockPos pos, Direction faceHit) {
        super(pos, faceHit, 0);
        this.startPos = pos;
        this.endPos = pos;
        this.placement = TorchPlacement.FLOOR;
    }

    /**
     * PacketIlluminate exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public PacketIlluminate(PacketId ignored, BlockPos startPos, BlockPos endPos, TorchPlacement placement) {
        super(startPos, Direction.UP, 0);
        this.startPos = startPos;
        this.endPos = endPos;
        this.placement = placement == null ? TorchPlacement.FLOOR : placement;
    }

    /**
     * PacketIlluminate exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public PacketIlluminate(BlockPos pos, Direction faceHit, int stateID) {
        super(pos, faceHit, stateID);
        this.startPos = pos;
        this.endPos = pos;
        this.placement = TorchPlacement.FLOOR;
    }

    @Override
    /**
     * getPacketId exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public PacketId getPacketId() {
        return PacketId.ILLUMINATE;
    }

    /**
     * process exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void process(Object player, PacketIlluminate pkt) {
        PacketProcessSupport.dispatchFeature(player, FeatureId.ILLUMINATION, pkt);
    }
}


