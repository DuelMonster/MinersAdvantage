package uk.co.duelmonster.minersadvantage.workers;

import java.util.concurrent.TimeUnit;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.helpers.FarmingHelper;
import uk.co.duelmonster.minersadvantage.network.packets.PacketCultivate;

public class CultivationAgent extends Agent {

  public CultivationAgent(ServerPlayerEntity player, final PacketCultivate pkt) {
    super(player, pkt);

    this.originPos = pkt.pos;

    this.interimArea = FarmingHelper.getFarmableLand(world, originPos);

    addConnectedToQueue(originPos);
  }

  // Returns true when Cultivation is complete or cancelled
  @Override
  public boolean tick() {
    if (originPos == null || player == null || !player.isAlive() || processed.size() >= clientConfig.common.blockLimit)
      return true;

    boolean bIsComplete = false;

    for (int iQueueCount = 0; queued.size() > 0; iQueueCount++) {
      if (iQueueCount >= clientConfig.common.blocksPerTick
          || processed.size() >= clientConfig.common.blockLimit
          || (clientConfig.common.tpsGuard && timer.elapsed(TimeUnit.MILLISECONDS) > 40))
        break;

      if (Functions.IsPlayerStarving(player)) {
        bIsComplete = true;
        break;
      }

      BlockPos oPos = queued.remove(0);
      if (oPos == null)
        continue;

      Block block = world.getBlockState(oPos).getBlock();
      if (!Constants.DIRT_BLOCKS.contains(block) && !block.equals(Blocks.GRASS_PATH)) {
        // Add the non-harvestable blocks to the processed list so that they can be avoided.
        processed.add(oPos);
        iQueueCount--;
        continue;
      }

      world.captureBlockSnapshots = true;
      world.capturedBlockSnapshots.clear();

      if (world.isEmptyBlock(oPos.above()) || world.getBlockState(oPos.above()).getMaterial().isReplaceable()) {
        Functions.playSound(world, oPos, SoundEvents.HOE_TILL, SoundCategory.PLAYERS, 1.0F, world.random.nextFloat() + 0.5F);

        if (!world.isEmptyBlock(oPos.above())) {
          world.setBlockAndUpdate(oPos.above(), Blocks.AIR.defaultBlockState());
        }

        world.setBlockAndUpdate(oPos, Blocks.FARMLAND.defaultBlockState().setValue(FarmlandBlock.MOISTURE, Integer.valueOf(7)));

        if (heldItemStack != null && heldItemStack.isDamageableItem()) {
          heldItemStack.hurtAndBreak(1, player, null);

          // if (heldItemStack.getMaxDamage() <= 0) {
          // player.inventory.removeStackFromSlot(player.inventory.selected);
          // player.openContainer.detectAndSendChanges();
          // }
        }

        processBlockSnapshots();
        addConnectedToQueue(oPos);
      }

      processed.add(oPos);
    }

    return (bIsComplete || queued.isEmpty());
  }

  @Override
  public void addToQueue(BlockPos oPos) {
    Block block = world.getBlockState(oPos).getBlock();
    if (Constants.DIRT_BLOCKS.contains(block) || block.equals(Blocks.GRASS_PATH))
      super.addToQueue(oPos);
  }
}
