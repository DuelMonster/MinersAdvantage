package uk.co.duelmonster.minersadvantage.common.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * BaseBlockPacket is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public abstract class BaseBlockPacket implements IMAPacket {
    public final BlockPos pos;
    public final Direction faceHit;
    public final int stateID;

    /**
     * BaseBlockPacket exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    protected BaseBlockPacket(BlockPos pos, Direction faceHit) {
        this(pos, faceHit, 0);
    }

    /**
     * BaseBlockPacket exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    protected BaseBlockPacket(BlockPos pos, Direction faceHit, int stateID) {
        this.pos = pos;
        this.faceHit = faceHit;
        this.stateID = stateID;
    }
}


