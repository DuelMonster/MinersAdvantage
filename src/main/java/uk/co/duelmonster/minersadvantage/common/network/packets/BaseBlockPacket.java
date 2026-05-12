package uk.co.duelmonster.minersadvantage.common.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public abstract class BaseBlockPacket implements IMAPacket {
    public final BlockPos pos;
    public final Direction faceHit;
    public final int stateID;

    protected BaseBlockPacket(BlockPos pos, Direction faceHit) {
        this(pos, faceHit, 0);
    }

    protected BaseBlockPacket(BlockPos pos, Direction faceHit, int stateID) {
        this.pos = pos;
        this.faceHit = faceHit;
        this.stateID = stateID;
    }
}
