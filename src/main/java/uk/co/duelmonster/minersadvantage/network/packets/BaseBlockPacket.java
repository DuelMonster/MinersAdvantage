package uk.co.duelmonster.minersadvantage.network.packets;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;

public abstract class BaseBlockPacket implements IMAPacket {

  public final BlockPos  pos;
  public final Direction faceHit;
  public final int       stateID;

  public BaseBlockPacket(BlockPos _pos, Direction _faceHit) {
    pos     = _pos;
    faceHit = _faceHit;
    stateID = 0;
  }

  public BaseBlockPacket(BlockPos _pos, Direction _faceHit, int _stateID) {
    pos     = _pos;
    faceHit = _faceHit;
    stateID = _stateID;
  }

  public BaseBlockPacket(FriendlyByteBuf buf) {
    pos     = buf.readBlockPos();
    faceHit = buf.readEnum(Direction.class);
    stateID = buf.readInt();
  }
}
