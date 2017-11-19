package co.uk.duelmonster.minersadvantage.workers;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Level;

import com.google.common.base.Stopwatch;

import co.uk.duelmonster.minersadvantage.MinersAdvantage;
import co.uk.duelmonster.minersadvantage.common.Functions;
import co.uk.duelmonster.minersadvantage.common.PacketID;
import co.uk.duelmonster.minersadvantage.common.Variables;
import co.uk.duelmonster.minersadvantage.packets.PacketBase;
import co.uk.duelmonster.minersadvantage.settings.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.BlockSnapshot;

public abstract class Agent {
	
	public final BlockPos	originPos;
	public final PacketID	packetID;
	
	public final World			world;
	public final EntityPlayerMP	player;
	public final Settings		settings;
	
	public Stopwatch		timer			= Stopwatch.createUnstarted();
	public ItemStack		heldItemStack	= null;
	public Item				heldItem		= null;
	public EnumFacing		sideHit			= EnumFacing.SOUTH;
	public int				iFeetPos		= 0;
	public Block			originBlock		= null;
	public int				originMeta		= 0;
	public boolean			isRedStone		= false;
	public AxisAlignedBB	harvestArea		= null;
	
	public List<BlockPos>	processed	= new ArrayList<BlockPos>();
	public List<BlockPos>	queued		= new ArrayList<BlockPos>();
	
	public Agent(EntityPlayerMP player, NBTTagCompound tags) {
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
		final IBlockState state = Block.getStateById(iStateID);
		
		if (state == null || state.getBlock() == Blocks.AIR)
			MinersAdvantage.logger.log(Level.INFO, "Invalid BlockState ID recieved from message packet. [ " + iStateID + " ]");
		
		this.originBlock = state.getBlock();
		this.originMeta = originBlock.getMetaFromState(state);
		this.isRedStone = (originBlock == Blocks.REDSTONE_ORE || originBlock == Blocks.LIT_REDSTONE_ORE);
		
		this.heldItemStack = Functions.getHeldItemStack(player);
		this.heldItem = Functions.getHeldItem(player);
		
		addConnectedToQueue(originPos);
	}
	
	// Adds the block position to the current block queue.
	public void addToQueue(BlockPos oPos) {
		if (oPos != null && !processed.contains(oPos) && !queued.contains(oPos) && (harvestArea == null || Functions.isWithinArea(oPos, harvestArea)))
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
		if (settings.bIlluminationEnabled && settings.bAutoIlluminate) {
			NBTTagCompound tags = new NBTTagCompound();
			tags.setInteger("ID", PacketID.Illuminate.value());
			tags.setInteger("x", oPos.getX());
			tags.setInteger("y", oPos.getY());
			tags.setInteger("z", oPos.getZ());
			
			MinersAdvantage.instance.network.sendToServer(new PacketBase(tags));
		}
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
		
		MinersAdvantage.instance.network.sendTo(new PacketBase(tags), player);
	}
	
	public void addConnectedToQueue(BlockPos oPos) {
		for (int xOffset = -1; xOffset <= 1; xOffset++)
			for (int yOffset = -1; yOffset <= 1; yOffset++)
				for (int zOffset = -1; zOffset <= 1; zOffset++)
					addToQueue(oPos.add(xOffset, yOffset, zOffset));
	}
	
	public boolean hasBeenProcessed(BlockPos pos) {
		return processed.contains(pos);
	}
	
	public boolean shouldProcess(BlockPos oPos) {
		return !processed.contains(oPos) && queued.contains(oPos) && (harvestArea == null || Functions.isWithinArea(oPos, harvestArea));
	}
	
	public void spawnProgressParticle(BlockPos oPos) {
		if (world instanceof WorldServer) {
			
			world.playSound(null, oPos, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 1.0F, world.rand.nextFloat() + 0.5F);
			
			double x = oPos.getX() + 0.4D;
			double y = oPos.getY() + 0.6D + world.rand.nextDouble() / 32F;
			double z = oPos.getZ() + 0.4D;
			
			// double motionX = world.rand.nextGaussian() * 0.01D;
			// double motionY = world.rand.nextGaussian() * 0.02D;
			// double motionZ = world.rand.nextGaussian() * 0.01D;
			
			// ((WorldServer) this.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, oPos.getX(), oPos.getY(),
			// oPos.getZ(), 8, motionX, motionY, motionZ, 0.4D, Block.getStateId(world.getBlockState(oPos)));
			
			int particles = world.rand.nextInt(5);
			
			for (int i = 0; i < 1 + particles; ++i) {
				world.spawnParticle(
						EnumParticleTypes.BLOCK_DUST,
						x + (world.rand.nextDouble() / 16F * (world.rand.nextBoolean() ? 1 : -1)),
						y,
						z + (world.rand.nextDouble() / 16F * (world.rand.nextBoolean() ? 1 : -1)),
						0.0D,
						0.0D,
						0.0D);
			}
		}
	}
}
