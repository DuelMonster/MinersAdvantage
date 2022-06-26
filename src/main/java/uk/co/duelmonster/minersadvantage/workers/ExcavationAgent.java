package uk.co.duelmonster.minersadvantage.workers;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Level;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.Tags;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.common.Variables;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.network.packets.BaseBlockPacket;

public class ExcavationAgent extends Agent {

  private boolean bIsSingleLayerToggled = false;

  public ExcavationAgent(ServerPlayer player, BaseBlockPacket pkt) {
    super(player, pkt);

    this.originPos   = pkt.pos;
    this.faceHit     = pkt.faceHit;
    this.originState = Block.stateById(pkt.stateID);

    if (originState == null || originState.isAir())
      Constants.LOGGER.log(Level.INFO, "Invalid BlockState ID recieved from message packet. [ " + pkt.stateID + " ]");

    this.originBlock = originState.getBlock();

    this.bIsSingleLayerToggled = (pkt.getPacketId() != PacketId.Veinate && Variables.get(player.getUUID()).IsSingleLayerToggled);

    this.shouldAutoIlluminate = (pkt.getPacketId() != PacketId.Veinate && clientConfig.common.autoIlluminate);

    setupHarvestArea();

    if (this.bIsSingleLayerToggled) {
      interimArea = new AABB(
          interimArea.minX, originPos.getY(), interimArea.minZ,
          interimArea.maxX, originPos.getY(), interimArea.maxZ);
    }

    addConnectedToQueue(originPos);
  }

  // Returns true when Excavation is complete or cancelled
  @Override
  public boolean tick() {
    if (originPos == null || player == null || !player.isAlive() || processed.size() >= clientConfig.common.blockLimit)
      return true;

    boolean bIsComplete = false;

    for (int iQueueCount = 0; queued.size() > 0; iQueueCount++) {
      if (iQueueCount >= clientConfig.common.blocksPerTick
          || (packetId != PacketId.Veinate && processed.size() >= clientConfig.common.blockLimit)
          || (clientConfig.common.tpsGuard && timer.elapsed(TimeUnit.MILLISECONDS) > 40))
        break;

      if (Functions.IsPlayerStarving(player)) {
        bIsComplete = true;
        break;
      }

      BlockPos oPos = queued.remove(0);
      if (oPos == null)
        continue;

      BlockState state = world.getBlockState(oPos);
      Block      block = state.getBlock();

      if (!getPlayer().hasCorrectToolForDrops(state)) {
        // Avoid the non-harvestable blocks.
        processed.add(oPos);
        continue;
      }

      // Process the current block if it is valid.
      if (packetId != PacketId.Veinate && clientConfig.common.mineVeins && state.is(Tags.Blocks.ORES)) {

        processed.add(oPos);
        excavateOreVein(state, oPos);

      } else if (block == originBlock || clientConfig.excavation.ignoreBlockVariants) {

        world.captureBlockSnapshots = true;
        world.capturedBlockSnapshots.clear();

        boolean bBlockHarvested = HarvestBlock(oPos);

        if (bBlockHarvested) {
          processBlockSnapshots();

          SoundType soundtype = block.getSoundType(state, world, oPos, null);
          reportProgessToClient(oPos, soundtype.getBreakSound());

          addConnectedToQueue(oPos);

          processed.add(oPos);
        }
      }
    }

    return (bIsComplete || queued.isEmpty());
  }

  @Override
  public void addConnectedToQueue(BlockPos oPos) {
    int xStart = -1, yStart = -1, zStart = -1;
    int xEnd   = 1, yEnd = 1, zEnd = 1;

    if (packetId == PacketId.Excavate && (oPos.getX() == originPos.getX() || oPos.getY() == originPos.getY() || oPos.getZ() == originPos.getZ()))
      switch (faceHit.getOpposite()) {
      case SOUTH: // Positive Z
      zStart = 0;
      break;
      case NORTH: // Negative Z
      zEnd = 0;
      break;
      case EAST: // Positive X
      xStart = 0;
      break;
      case WEST: // Negative X
      xEnd = 0;
      break;
      case UP: // Positive Y
      yStart = 0;
      break;
      case DOWN: // Negative Y
      yEnd = 0;
      break;
      default:
      break;
      }

    if (bIsSingleLayerToggled) {
      yStart = 0;
      yEnd   = 0;
    }

    for (int xOffset = xStart; xOffset <= xEnd; xOffset++)
      for (int yOffset = yStart; yOffset <= yEnd; yOffset++)
        for (int zOffset = zStart; zOffset <= zEnd; zOffset++)
          addToQueue(oPos.offset(xOffset, yOffset, zOffset));
  }

  @Override
  public void addToQueue(BlockPos oPos) {
    BlockState state = world.getBlockState(oPos);

    if (getPlayer().hasCorrectToolForDrops(state)) {
      Block block = state.getBlock();

      if ((clientConfig.common.mineVeins && state.is(Tags.Blocks.ORES))
          || (block == originBlock || clientConfig.excavation.ignoreBlockVariants)) {
        super.addToQueue(oPos);
      }
    }
  }
}
