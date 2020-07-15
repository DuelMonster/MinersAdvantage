package uk.co.duelmonster.minersadvantage.helpers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CommandBlockBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.StructureBlock;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;
import uk.co.duelmonster.minersadvantage.common.Functions;

public class BreakBlockController {
	
	public final World				world;
	public final ServerPlayerEntity	player;
	// private final NetHandlerPlayClient connection;
	
	/** The Item currently being used to destroy a block */
	public ItemStack	heldItemStack	= null;
	public Item			heldItem		= null;
	public int			heldItemSlot	= -1;
	public Direction	faceHit			= Direction.SOUTH;
	public boolean		bBlockDestroyed	= false;
	
	private BlockPos	currentBlockPos	= new BlockPos(-1, -1, -1);
	/** Current block damage (MP) */
	private float		curBlockDamageMP;
	/** Tick counter, when it hits 4 it resets back to 0 and plays the step sound */
	private float		stepSoundTickCounter;
	/** Delays the first damage on the block after the first click on the block */
	private int			blockHitDelay;
	/** Tells if the player is hitting a block */
	private boolean		isHittingBlock;
	
	public BreakBlockController(ServerPlayerEntity player) {
		this.world = player.world;
		this.player = player;
		// this.connection = player.connection;
		
		this.heldItemStack = Functions.getHeldItemStack(player);
		this.heldItem = Functions.getHeldItem(player);
		this.heldItemSlot = player.inventory.currentItem;
	}
	
	public boolean DestroyBlock(BlockPos oPos) {
		if (!world.isBlockModifiable(player, oPos))
			return false;
		
		// this mirrors ItemInWorldManager.tryHarvestBlock
		final BlockState state = world.getBlockState(oPos);
		
		BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, oPos, state, player);
		if (MinecraftForge.EVENT_BUS.post(event))
			return false;
		
		final TileEntity te = world.getTileEntity(oPos); // OHHHHH YEEEEAAAH
		
		boolean	canHarvest	= state.canHarvestBlock(world, oPos, player);
		boolean	isRemoved	= removeBlock(oPos, state, canHarvest);
		if (isRemoved && canHarvest) {
			state.getBlock().harvestBlock(world, player, oPos, state, te, heldItemStack);
			world.playEvent(player, 2001, oPos, Block.getStateId(state));
		}
		
		return isRemoved && canHarvest;
	}
	
	private boolean removeBlock(BlockPos oPos, BlockState state, boolean canHarvest) {
		final Block block = state.getBlock();
		block.onBlockHarvested(world, oPos, state, player);
		final boolean result = state.removedByPlayer(world, oPos, player, canHarvest, state.getFluidState());
		if (result)
			block.onPlayerDestroy(world, oPos, state);
		return result;
	}
	
	public boolean onPlayerDamageBlock(BlockPos posBlock, Direction directionFacing) {
		if (this.blockHitDelay > 0) {
			--this.blockHitDelay;
			return true;
		} else if (this.isHittingPosition(posBlock)) {
			BlockState	iblockstate	= world.getBlockState(posBlock);
			Block		block		= iblockstate.getBlock();
			
			if (iblockstate.isAir(world, posBlock)) {
				this.isHittingBlock = false;
				return false;
			} else {
				this.curBlockDamageMP += iblockstate.getPlayerRelativeBlockHardness(player, world, posBlock);
				
				if (this.stepSoundTickCounter % 4.0F == 0.0F) {
					SoundType soundType = block.getSoundType(iblockstate, world, posBlock, player);
					Functions.playSound(world, posBlock, soundType.getHitSound(), SoundCategory.NEUTRAL, (soundType.getVolume() + 1.0F) / 8.0F, soundType.getPitch() * 0.5F);
				}
				
				++this.stepSoundTickCounter;
				
				if (this.curBlockDamageMP >= 1.0F) {
					this.isHittingBlock = false;
					// this.connection.sendPacket(new
					// CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, posBlock, directionFacing));
					bBlockDestroyed = true;// this.onPlayerDestroyBlock(posBlock);
					this.curBlockDamageMP = 0.0F;
					this.stepSoundTickCounter = 0.0F;
					this.blockHitDelay = 5;
				}
				
				world.sendBlockBreakProgress(player.getEntityId(), this.currentBlockPos, (int) (this.curBlockDamageMP * 10.0F) - 1);
				return true;
			}
		} else {
			return this.clickBlock(posBlock, directionFacing);
		}
	}
	
	/**
	 * Called when the player is hitting a block with an item.
	 */
	public boolean clickBlock(BlockPos pos, Direction face) {
		if (heldItemStack.isEmpty() || !world.getWorldBorder().contains(pos))
			return false;
		
		if (!this.isHittingBlock || !this.isHittingPosition(pos)) {
			// if (this.isHittingBlock)
			// this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK,
			// this.currentBlockPos, face));
			
			BlockState iblockstate = world.getBlockState(pos);
			
			// this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, loc,
			// face));
			boolean isAir = iblockstate.isAir(world, pos);
			
			if (isAir && this.curBlockDamageMP == 0.0F) {
				iblockstate.onBlockClicked(world, pos, player);
			}
			
			if (isAir && iblockstate.getPlayerRelativeBlockHardness(player, world, pos) >= 1.0F) {
				this.onPlayerDestroyBlock(pos);
			} else {
				this.isHittingBlock = true;
				this.currentBlockPos = pos;
				this.curBlockDamageMP = 0.0F;
				this.stepSoundTickCounter = 0.0F;
				world.sendBlockBreakProgress(player.getEntityId(), this.currentBlockPos, (int) (this.curBlockDamageMP * 10.0F) - 1);
			}
		}
		
		return true;
	}
	
	public boolean onPlayerDestroyBlock(BlockPos pos) {
		if (this.heldItemStack.isEmpty())
			return false;
		
		if (!this.heldItemStack.isEmpty() && this.heldItem.onBlockStartBreak(this.heldItemStack, pos, player))
			return false;
		
		BlockState	state	= world.getBlockState(pos);
		Block		block	= state.getBlock();
		
		if ((block instanceof CommandBlockBlock || block instanceof StructureBlock) && !player.canUseCommandBlock()) {
			return false;
		} else if (state.isAir(world, pos)) {
			return false;
		} else {
			world.playEvent(2001, pos, Block.getStateId(state));
			
			this.currentBlockPos = new BlockPos(this.currentBlockPos.getX(), -1, this.currentBlockPos.getZ());
			
			ItemStack copyBeforeUse = this.heldItemStack.copy();
			
			if (!this.heldItemStack.isEmpty()) {
				this.heldItemStack.onBlockDestroyed(world, state, pos, player);
				
				if (this.heldItemStack.isEmpty()) {
					net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, copyBeforeUse, Hand.MAIN_HAND);
					player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
				}
			}
		}
		
		boolean flag = state.removedByPlayer(world, pos, player, false, state.getFluidState());
		
		if (flag)
			block.onPlayerDestroy(world, pos, state);
		
		return flag;
		
	}
	
	private boolean isHittingPosition(BlockPos pos) {
		return pos.equals(this.currentBlockPos) && !heldItemStack.isEmpty();
	}
	
	// player reach distance = 4F
	public float getBlockReachDistance() {
		return ((float) player.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue()) - 0.5F;
	}
}
