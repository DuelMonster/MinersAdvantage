package uk.co.duelmonster.minersadvantage.common.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.network.packetids.PacketId;

/**
 * PacketSubstituteTool is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public class PacketSubstituteTool extends BaseBlockPacket {
    public final int slot;

    /**
     * PacketSubstituteTool exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public PacketSubstituteTool(int slot) {
        super(BlockPos.ZERO, Direction.UP, 0);
        this.slot = slot;
    }

    /**
     * PacketSubstituteTool exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public PacketSubstituteTool(BlockPos pos) {
        super(pos, Direction.UP, 0);
        this.slot = -1;
    }

    /**
     * PacketSubstituteTool exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public PacketSubstituteTool(BlockPos pos, Direction faceHit, int stateID) {
        super(pos, faceHit, stateID);
        this.slot = -1;
    }

    @Override
    /**
     * getPacketId exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public PacketId getPacketId() {
        return PacketId.SUBSTITUTE;
    }

    /**
     * process exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void process(Object player, PacketSubstituteTool pkt) {
        PacketProcessSupport.dispatchSubstitution(player, pkt);
    }
}
