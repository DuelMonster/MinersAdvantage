package uk.co.duelmonster.minersadvantage.common.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.network.packetids.PacketId;

/**
 * PacketLumbinate is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public class PacketLumbinate extends BaseBlockPacket {
    /**
     * PacketLumbinate exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public PacketLumbinate(BlockPos pos, Direction faceHit, int stateID) {
        super(pos, faceHit, stateID);
    }

    @Override
    /**
     * getPacketId exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public PacketId getPacketId() {
        return PacketId.LUMBINATE;
    }

    /**
     * process exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void process(Object player, PacketLumbinate pkt) {
        PacketProcessSupport.dispatchFeature(player, FeatureId.LUMBINATION, pkt);
    }
}


