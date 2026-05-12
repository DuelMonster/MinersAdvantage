package uk.co.duelmonster.minersadvantage.common.network.packets;

import uk.co.duelmonster.minersadvantage.common.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

/**
 * PacketCaptivate is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public class PacketCaptivate implements IMAPacket {
    @Override
    /**
     * getPacketId exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public PacketId getPacketId() {
        return PacketId.CAPTIVATE;
    }

    /**
     * process exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void process(Object player, PacketCaptivate pkt) {
        PacketProcessSupport.dispatchFeature(player, FeatureId.CAPTIVATION);
    }
}
