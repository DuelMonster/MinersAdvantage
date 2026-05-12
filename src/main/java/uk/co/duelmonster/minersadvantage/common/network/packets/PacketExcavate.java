package uk.co.duelmonster.minersadvantage.common.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.network.packetids.PacketId;

/**
 * PacketExcavate is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public class PacketExcavate extends BaseBlockPacket {
    /**
     * PacketExcavate exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public PacketExcavate(BlockPos pos, Direction faceHit, int stateID) {
        super(pos, faceHit, stateID);
    }

    @Override
    /**
     * getPacketId exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public PacketId getPacketId() {
        return PacketId.EXCAVATE;
    }

    /**
     * process exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void process(Object player, PacketExcavate pkt) {
        PacketProcessSupport.dispatchFeature(player, FeatureId.EXCAVATION, pkt);
    }
}


