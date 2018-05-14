package co.uk.duelmonster.minersadvantage.workers;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;

import com.google.common.base.Stopwatch;

import co.uk.duelmonster.minersadvantage.MinersAdvantage;
import co.uk.duelmonster.minersadvantage.common.BreakBlockController;
import co.uk.duelmonster.minersadvantage.common.Functions;
import co.uk.duelmonster.minersadvantage.common.PacketID;
import co.uk.duelmonster.minersadvantage.common.Variables;
import co.uk.duelmonster.minersadvantage.packets.NetworkPacket;
import co.uk.duelmonster.minersadvantage.settings.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

public abstract class Agent {
	
	public final BlockPos	originPos;
	public final PacketID	packetID;
	
	public final World			world;
	public final EntityPlayerMP	player;
	public final FakePlayer		fakePlayer;
	public final Settings		settings;
	
	public Stopwatch			timer			= Stopwatch.createUnstarted();
	public ItemStack			heldItemStack	= null;
	public Item					heldItem		= null;
	public int					heldItemSlot	= -1;
	public EnumFacing			sideHit			= EnumFacing.SOUTH;
	public int					iFeetPos		= 0;
	public IBlockState			originState		= null;
	public Block				originBlock		= null;
	public int					originMeta		= 0;
	public boolean				isRedStone		= false;
	public AxisAlignedBB		harvestArea		= null;
	public BreakBlockController	breakController	= null;
	
	public List<BlockPos>	processed	= new ArrayList<BlockPos>();
	public List<BlockPos>	queued		= new ArrayList<BlockPos>();
	
	public Agent(EntityPlayerMP player, NBTTagCompound tags, boolean bOverrideAddConnectedOnInit) {
		this.world = player.world;
		this.player = player;
		this.iFeetPos = (int) player.getEntityBoundingBox().minY;
		this.settings = Settings.get(player.getUniqueID());
		
		this.packetID = PacketID.valueOf(tags.getInteger("ID"));
		
		this.sideHit = EnumFacing.getFront(tags.getInteger("sideHit"));
		this.originPos = new BlockPos(
				tags.getInteger("x"),
				tags.getInteger("y"),
				tags.getInteger("z"));
		
		final int iStateID = tags.getInteger("stateID");
		originState = Block.getStateById(iStateID);
		
		if (originState == null || originState.getBlock() == Blocks.AIR)
			MinersAdvantage.logger.log(Level.INFO, "Invalid BlockState ID recieved from message packet. [ " + iStateID + " ]");
		
		this.originBlock = originState.getBlock();
		this.originMeta = originBlock.getMetaFromState(originState);
		this.isRedStone = (originBlock == Blocks.REDSTONE_ORE || originBlock == Blocks.LIT_REDSTONE_ORE);
		
		this.heldItemStack = Functions.getHeldItemStack(player);
		this.heldItem = Functions.getHeldItem(player);
		this.heldItemSlot = player.inventory.currentItem;
		
		this.fakePlayer = FakePlayerFactory.get((WorldServer) world, player.getGameProfile());
		this.fakePlayer.connection = player.connection;
		if (this.heldItemStack != null)
			this.fakePlayer.setHeldItem(EnumHand.MAIN_HAND, this.heldItemStack);
		
		if (!bOverrideAddConnectedOnInit)
			addConnectedToQueue(originPos);
	}
	
	public void addConnectedToQueue(BlockPos oPos) {
		for (int xOffset = -1; xOffset <= 1; xOffset++)
			for (int yOffset = -1; yOffset <= 1; yOffset++)
				for (int zOffset = -1; zOffset <= 1; zOffset++)
					addToQueue(oPos.add(xOffset, yOffset, zOffset));
	}
	
	// Adds the block position to the current block queue.
	public void addToQueue(BlockPos oPos) {
		final IBlockState state = world.getBlockState(oPos);
		
		if (state == null || state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.BEDROCK ||
				state.getMaterial() == Material.WATER || state.getMaterial() == Material.LAVA ||
				oPos == null || processed.contains(oPos) || queued.contains(oPos) ||
				(harvestArea != null && !Functions.isWithinArea(oPos, harvestArea)))
			return;
		
		queued.add(oPos);
	}
	
	// Returns true when complete or cancelled
	public boolean tick() {
		return queued.isEmpty();
	}
	
	public void processBlockSnapshots() {
		world.captureBlockSnapshots = false;
		AgentProcessor.instance.setCurrentAgent(player.getUniqueID(), null);
		while (world.capturedBlockSnapshots.size() > 0) {
			BlockSnapshot snap = world.capturedBlockSnapshots.get(0);
			// if(oPos.equals(snap.getPos()))
			// history.addRecordedBlock(new BlockHistory(snap));
			world.capturedBlockSnapshots.remove(0);
			
			world.markAndNotifyBlock(
					snap.getPos(),
					world.getChunkFromChunkCoords(snap.getPos().getX() >> 4, snap.getPos().getZ() >> 4),
					snap.getReplacedBlock(),
					snap.getCurrentBlock(),
					snap.getFlag());
		}
		AgentProcessor.instance.setCurrentAgent(player.getUniqueID(), this);
	}
	
	public void autoIlluminate(BlockPos oPos) {
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("ID", PacketID.Illuminate.value());
		tags.setInteger("x", oPos.getX());
		tags.setInteger("y", oPos.getY());
		tags.setInteger("z", oPos.getZ());
		
		MinersAdvantage.instance.network.sendTo(new NetworkPacket(tags), player);
	}
	
	public void excavateOreVein(IBlockState state, BlockPos oPos) {
		
		// If mine veins is enabled send message back to player from processing.
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("ID", PacketID.Veinate.value());
		tags.setInteger("x", oPos.getX());
		tags.setInteger("y", oPos.getY());
		tags.setInteger("z", oPos.getZ());
		tags.setInteger("sideHit", Variables.get(player.getUniqueID()).sideHit.getIndex());
		tags.setInteger("stateID", Block.getStateId(state));
		tags.setBoolean("doSubstitution", true);
		
		MinersAdvantage.instance.network.sendTo(new NetworkPacket(tags), player);
	}
	
	public boolean hasBeenProcessed(BlockPos pos) {
		return processed.contains(pos);
	}
	
	public boolean shouldProcess(BlockPos oPos) {
		return !processed.contains(oPos) && queued.contains(oPos) && (harvestArea == null || Functions.isWithinArea(oPos, harvestArea));
	}
	
	public void reportProgessToClient(BlockPos oPos) {
		reportProgessToClient(oPos, SoundEvents.BLOCK_STONE_BREAK);
	}
	
	public void reportProgessToClient(BlockPos oPos, SoundEvent soundType) {
		if (world instanceof WorldServer) {
			
			Functions.playSound(world, oPos, soundType, SoundCategory.BLOCKS, 1.0F, 1.0F);
			Functions.spawnAreaEffectCloud(world, player, oPos);
			
		}
	}
	
	public boolean HarvestBlock(BlockPos oPos) {
		
		boolean bResult = fakePlayer.interactionManager.tryHarvestBlock(oPos);
		player.connection.sendPacket(new SPacketBlockChange(world, oPos));
		
		final int range = 20;
		IBlockState state = world.getBlockState(oPos);
		
		List<EntityPlayerMP> localPlayers = world.getEntitiesWithinAABB(EntityPlayerMP.class, new AxisAlignedBB(oPos.add(-range, -range, -range), oPos.add(range, range, range)));
		
		SPacketEffect packet = new SPacketEffect(2001, oPos, Block.getStateId(state), false);
		for (Entity entity : localPlayers)
			((EntityPlayerMP) entity).connection.sendPacket(packet);
		
		Block blockAbove = Functions.getBlockFromWorld(world, oPos.up());
		if (blockAbove instanceof BlockFalling && HarvestBlock(oPos.up()))
			for (Entity entity : localPlayers)
				Functions.spawnAreaEffectCloud(world, (EntityPlayer) entity, oPos);
			
		return bResult;
	}
}
