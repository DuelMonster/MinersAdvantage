package co.uk.duelmonster.minersadvantage.common;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

public class BreakBlockController {
	
	public final World			world;
	public final EntityPlayerMP	player;
	// private final NetHandlerPlayClient connection;
	
	/** The Item currently being used to destroy a block */
	public ItemStack	heldItemStack	= null;
	public Item			heldItem		= null;
	public int			heldItemSlot	= -1;
	public EnumFacing	sideHit			= EnumFacing.SOUTH;
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
	
	public BreakBlockController(EntityPlayerMP player) {
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
		final IBlockState state = world.getBlockState(oPos);
		
		BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, oPos, state, player);
		if (MinecraftForge.EVENT_BUS.post(event))
			return false;
		
		final TileEntity te = world.getTileEntity(oPos); // OHHHHH YEEEEAAAH
		
		boolean canHarvest = state.getBlock().canHarvestBlock(world, oPos, player);
		boolean isRemoved = removeBlock(oPos, state, canHarvest);
		if (isRemoved && canHarvest) {
			state.getBlock().harvestBlock(world, player, oPos, state, te, heldItemStack);
			world.playEvent(player, 2001, oPos, Block.getStateId(state));
		}
		
		return isRemoved && canHarvest;
	}
	
	private boolean removeBlock(BlockPos oPos, IBlockState state, boolean canHarvest) {
		final Block block = state.getBlock();
		block.onBlockHarvested(world, oPos, state, player);
		final boolean result = block.removedByPlayer(state, world, oPos, player, canHarvest);
		if (result)
			block.onBlockDestroyedByPlayer(world, oPos, state);
		return result;
	}
	
	public boolean onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing) {
		if (this.blockHitDelay > 0) {
			--this.blockHitDelay;
			return true;
		} else if (this.isHittingPosition(posBlock)) {
			IBlockState iblockstate = world.getBlockState(posBlock);
			Block block = iblockstate.getBlock();
			
			if (iblockstate.getMaterial() == Material.AIR) {
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
	public boolean clickBlock(BlockPos loc, EnumFacing face) {
		if (heldItemStack.isEmpty() || !world.getWorldBorder().contains(loc))
			return false;
		
		if (!this.isHittingBlock || !this.isHittingPosition(loc)) {
			// if (this.isHittingBlock)
			// this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK,
			// this.currentBlockPos, face));
			
			IBlockState iblockstate = world.getBlockState(loc);
			
			// this.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, loc,
			// face));
			boolean flag = iblockstate.getMaterial() != Material.AIR;
			
			if (flag && this.curBlockDamageMP == 0.0F) {
				iblockstate.getBlock().onBlockClicked(world, loc, player);
			}
			
			if (flag && iblockstate.getPlayerRelativeBlockHardness(player, world, loc) >= 1.0F) {
				this.onPlayerDestroyBlock(loc);
			} else {
				this.isHittingBlock = true;
				this.currentBlockPos = loc;
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
		
		IBlockState iblockstate = world.getBlockState(pos);
		Block block = iblockstate.getBlock();
		
		if ((block instanceof BlockCommandBlock || block instanceof BlockStructure) && !player.canUseCommandBlock()) {
			return false;
		} else if (iblockstate.getMaterial() == Material.AIR) {
			return false;
		} else {
			world.playEvent(2001, pos, Block.getStateId(iblockstate));
			
			this.currentBlockPos = new BlockPos(this.currentBlockPos.getX(), -1, this.currentBlockPos.getZ());
			
			ItemStack copyBeforeUse = this.heldItemStack.copy();
			
			if (!this.heldItemStack.isEmpty()) {
				this.heldItemStack.onBlockDestroyed(world, iblockstate, pos, player);
				
				if (this.heldItemStack.isEmpty()) {
					net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, copyBeforeUse, EnumHand.MAIN_HAND);
					player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
				}
			}
		}
		
		boolean flag = block.removedByPlayer(iblockstate, world, pos, player, false);
		
		if (flag)
			block.onBlockDestroyedByPlayer(world, pos, iblockstate);
		
		return flag;
		
	}
	
	private boolean isHittingPosition(BlockPos pos) {
		return pos.equals(this.currentBlockPos) && !heldItemStack.isEmpty();
	}
	
	// player reach distance = 4F
	public float getBlockReachDistance() {
		return ((float) player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue()) - 0.5F;
	}
}
