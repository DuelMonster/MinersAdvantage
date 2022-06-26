package uk.co.duelmonster.minersadvantage.workers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.helpers.LumbinationHelper;
import uk.co.duelmonster.minersadvantage.network.packets.PacketLumbinate;

public class LumbinationAgent extends Agent {

  private LumbinationHelper lumbinationHelper = new LumbinationHelper();

  private BlockPos       originLeafPos   = null;
  private BlockState     originLeafState = null;
  private Block          originLeafBlock = null;
  private AxisAlignedBB  trunkArea       = null;
  private List<BlockPos> trunkPositions  = new ArrayList<BlockPos>();

  public LumbinationAgent(ServerPlayerEntity player, PacketLumbinate pkt) {
    super(player, pkt);

    this.originPos   = pkt.pos;
    this.faceHit     = pkt.faceHit;
    this.originState = Block.stateById(pkt.stateID);

    this.originBlock = this.originState.getBlock();

    lumbinationHelper.setPlayer(player);

    this.interimArea    = lumbinationHelper.identifyTree(originPos, originState);
    this.trunkArea      = lumbinationHelper.trunkArea;
    this.trunkPositions = lumbinationHelper.trunkPositions;
    this.originLeafPos  = lumbinationHelper.getLeafPos(originPos);

    if (this.originLeafPos != null) {
      this.originLeafState = world.getBlockState(originLeafPos);
      this.originLeafBlock = originLeafState.getBlock();

      // Add the origin block the queue now that we have all the information. - this.queued.clear();
      addConnectedToQueue(originPos);
    }
  }

  // Returns true when Lumbination is complete or cancelled
  @Override
  public boolean tick() {
    if (originPos == null || getPlayer() == null || !getPlayer().isAlive() || processed.size() >= clientConfig.common.blockLimit)
      return true;

    boolean bCancelHarvest = false;

    for (int iQueueCount = 0; queued.size() > 0; iQueueCount++) {
      if (iQueueCount >= clientConfig.common.blocksPerTick
          || processed.size() >= clientConfig.common.blockLimit
          || (clientConfig.common.tpsGuard && timer.elapsed(TimeUnit.MILLISECONDS) > 40))
        break;

      if (Functions.IsPlayerStarving(getPlayer())) {
        bCancelHarvest = true;
        break;
      }

      BlockPos oPos = queued.remove(0);
      if (oPos == null || !Functions.isWithinArea(oPos, interimArea))
        continue;

      BlockState state     = world.getBlockState(oPos);
      Block      block     = state.getBlock();
      SoundType  soundtype = state.getBlock().getSoundType(state, world, oPos, null);

      world.captureBlockSnapshots = true;
      world.capturedBlockSnapshots.clear();

      boolean isLeaves = lumbinationHelper.isValidLeaves(state) && Functions.isBlockSame(originLeafBlock, block);
      boolean isWood   = lumbinationHelper.isValidLog(state) && Functions.isBlockSame(originBlock, block);

      System.out.print("[CHECK] isLeaves = " + isLeaves);
      System.out.print(" -> isWood = " + isWood);

      // Process the current block if it is valid.
      // if (!getPlayer().hasCorrectToolForDrops(state) || (isLeaves &&
      // !clientConfig.lumbination.destroyLeaves)) {
      if ((!isLeaves && !isWood) || (isLeaves && !clientConfig.lumbination.destroyLeaves)) {
        // Avoid the non-harvestable blocks.
        processed.add(oPos);

        System.out.print(" -> non-harvestable");

      } else if (isLeaves && clientConfig.lumbination.useShearsOnLeaves && lumbinationHelper.playerHasShears()) {
        // Harvest the leaves using the players shears
        this.getPlayer().setItemInHand(Hand.MAIN_HAND, lumbinationHelper.getPlayersShears());

        if (HarvestBlock(oPos)) {
          HarvestBlock(oPos);
          reportProgessToClient(oPos, soundtype.getBreakSound());
          processBlockSnapshots();
          addConnectedToQueue(oPos);

          processed.add(oPos);
        }

        // Reset the Players held item back to the players initial held item
        this.getPlayer().setItemInHand(Hand.MAIN_HAND, this.heldItemStack);

        System.out.print(" -> playerHasShears");

      } else if (isLeaves && !clientConfig.lumbination.leavesAffectDurability) {
        // Remove the Leaves without damaging the tool.
        ItemStack originalStack = this.getPlayer().getMainHandItem().copy();

        if (HarvestBlock(oPos)) {
          reportProgessToClient(oPos, soundtype.getBreakSound());
          processBlockSnapshots();
          addConnectedToQueue(oPos);

          processed.add(oPos);

          this.getPlayer().setItemSlot(EquipmentSlotType.MAINHAND, originalStack);
        }

        System.out.print(" -> durabilityReset");

      } else if (isWood || (isLeaves && clientConfig.lumbination.destroyLeaves)) {

        if (HarvestBlock(oPos)) {
          reportProgessToClient(oPos, soundtype.getBreakSound());
          processBlockSnapshots();
          addConnectedToQueue(oPos);

          processed.add(oPos);
        }

        System.out.print(" -> harvest");
      }

      System.out.println();
    }

    // If the queue is empty then the tree has been harvested, so lets re-plant the sapling(s) if
    // enabled
    if (queued.isEmpty() && originLeafPos != null && clientConfig.lumbination.replantSaplings)
      lumbinationHelper.replantSaplings(dropsHistory, originLeafState, originLeafPos);

    return (bCancelHarvest || queued.isEmpty());
  }

  @Override
  public void addToQueue(BlockPos oPos) {
    BlockState state      = world.getBlockState(oPos);
    Block      checkBlock = state.getBlock();

    if (Functions.isBlockSame(originBlock, checkBlock)
        && (trunkArea == null || Functions.isWithinArea(oPos, trunkArea))
        && (trunkPositions == null || Functions.isPosConnected(trunkPositions, oPos))) {
      System.out.print("[ADD] Wood");
      System.out.println();

      super.addToQueue(oPos);
    }

    if (Functions.isBlockSame(originLeafBlock, checkBlock)
        && clientConfig.lumbination.destroyLeaves
        && (interimArea == null || Functions.isWithinArea(oPos, interimArea))) {
      System.out.print("[ADD] Leaves");
      System.out.println();

      super.addToQueue(oPos);
    }
  }

}
