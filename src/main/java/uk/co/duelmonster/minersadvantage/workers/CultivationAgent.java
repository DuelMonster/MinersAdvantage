package uk.co.duelmonster.minersadvantage.workers;

import java.util.concurrent.TimeUnit;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.config.MAConfig;
import uk.co.duelmonster.minersadvantage.helpers.FarmingHelper;
import uk.co.duelmonster.minersadvantage.network.packets.PacketCultivate;

public class CultivationAgent extends Agent {

	public CultivationAgent(ServerPlayerEntity player, final PacketCultivate pkt) {
		super(player, pkt);

		this.originPos = pkt.pos;

		this.harvestArea = FarmingHelper.getFarmableLand(world, originPos);

		addConnectedToQueue(originPos);
	}

	// Returns true when Cultivation is complete or cancelled
	@Override
	public boolean tick() {
		if (originPos == null || player == null || !player.isAlive() || processed.size() >= MAConfig.CLIENT.common.blockLimit())
			return true;

		boolean bIsComplete = false;

		for (int iQueueCount = 0; queued.size() > 0; iQueueCount++) {
			if (iQueueCount >= MAConfig.CLIENT.common.blocksPerTick()
					|| processed.size() >= MAConfig.CLIENT.common.blockLimit()
					|| (MAConfig.CLIENT.common.tpsGuard() && timer.elapsed(TimeUnit.MILLISECONDS) > 40))
				break;

			if (Functions.IsPlayerStarving(player)) {
				bIsComplete = true;
				break;
			}

			BlockPos oPos = queued.remove(0);
			if (oPos == null)
				continue;

			BlockState state = world.getBlockState(oPos);

			if (!state.isIn(BlockTags.DIRT_LIKE)) {
				// Add the non-harvestable blocks to the processed list so that they can be avoided.
				processed.add(oPos);
				iQueueCount--;
				continue;
			}

			world.captureBlockSnapshots = true;
			world.capturedBlockSnapshots.clear();

			if (world.isAirBlock(oPos.up()) || world.getBlockState(oPos.up()).getMaterial().isReplaceable()) {
				Functions.playSound(world, oPos, SoundEvents.ITEM_HOE_TILL, SoundCategory.PLAYERS, 1.0F, world.rand.nextFloat() + 0.5F);

				if (!world.isAirBlock(oPos.up())) {
					world.setBlockState(oPos.up(), Blocks.AIR.getDefaultState());
				}
				
				world.setBlockState(oPos, Blocks.FARMLAND.getDefaultState().with(FarmlandBlock.MOISTURE, Integer.valueOf(7)), 11);

				if (heldItemStack != null && heldItemStack.isDamageable()) {
					heldItemStack.damageItem(1, player, null);

					if (heldItemStack.getMaxDamage() <= 0) {
						player.inventory.removeStackFromSlot(player.inventory.currentItem);
						player.openContainer.detectAndSendChanges();
					}
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
		BlockState state = world.getBlockState(oPos);

		if (state.isIn(BlockTags.DIRT_LIKE))
			super.addToQueue(oPos);
	}
}
