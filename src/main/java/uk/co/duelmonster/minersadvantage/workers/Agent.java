package uk.co.duelmonster.minersadvantage.workers;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Stopwatch;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FallingBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SChangeBlockPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.common.Variables;
import uk.co.duelmonster.minersadvantage.config.MAConfig_Client;
import uk.co.duelmonster.minersadvantage.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.helpers.BreakBlockController;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.network.packets.IMAPacket;
import uk.co.duelmonster.minersadvantage.network.packets.PacketVeinate;

public abstract class Agent {
	
	public final World              world;
	public final ServerPlayerEntity player;
	public final SyncedClientConfig clientConfig;
	
	public List<Entity> dropsHistory = Collections.synchronizedList(new ArrayList<Entity>());
	
	public WeakReference<FakePlayer> _fakePlayer;
	
	public ServerPlayerEntity getPlayer() {
		return player;
		// FakePlayer ret = _fakePlayer != null ? _fakePlayer.get() : null;
		// if (ret == null) {
		// ret = FakePlayerFactory.get(player.getServerWorld(), new GameProfile(UUID.randomUUID(), Constants.MOD_NAME));
		// _fakePlayer = new WeakReference<FakePlayer>(ret);
		// }
		// return ret;
	}
	
	public PacketId packetId = PacketId.INVALID;
	
	public Stopwatch            timer                    = Stopwatch.createUnstarted();
	public ItemStack            heldItemStack            = null;
	public Item                 heldItem                 = null;
	public int                  heldItemSlot             = -1;
	public Direction            playerFacing;
	public Direction            faceHit                  = Direction.SOUTH;
	public int                  feetPos                  = 0;
	public BlockPos             originPos;
	public BlockState           originState              = null;
	public Block                originBlock              = null;
	public boolean              isRedStone               = false;
	public BreakBlockController breakController          = null;
	public boolean              awaitingAutoIllumination = false;
	public boolean              shouldAutoIlluminate     = false;
	
	public AxisAlignedBB interimArea = null;
	public AxisAlignedBB refinedArea = null;
	public BlockPos      minAreaPos  = BlockPos.ZERO;
	public BlockPos      maxAreaPos  = BlockPos.ZERO;
	
	public List<BlockPos> processed = new ArrayList<BlockPos>();
	public List<BlockPos> queued    = new ArrayList<BlockPos>();
	
	public Agent(ServerPlayerEntity player, IMAPacket pkt) {
		
		this.packetId = (PacketId) pkt.getPacketId();
		
		this.world        = player.world;
		this.player       = player;
		this.clientConfig = MAConfig_Client.getPlayerConfig(player.getUniqueID());
		
		this.feetPos = (int) player.getBoundingBox().minY;
		
		this.playerFacing = Functions.getPlayerFacing(player);
		
		this.heldItemStack = Functions.getHeldItemStack(player);
		this.heldItem      = Functions.getHeldItem(player);
		this.heldItemSlot  = player.inventory.currentItem;
		
		this.getPlayer().connection = player.connection;
		if (this.heldItemStack != null)
			this.getPlayer().setHeldItem(Hand.MAIN_HAND, this.heldItemStack);
	}
	
	protected void setupHarvestArea() {
		// Shaft area info
		int blockRadius = (packetId != PacketId.Veinate ? clientConfig.common.blockRadius - 1 : 16);
		
		int xStart  = 0;
		int xEnd    = 0;
		int yBottom = feetPos;
		int yTop    = feetPos + (blockRadius - 1);
		int zStart  = 0;
		int zEnd    = 0;
		
		// if the ShaftWidth is divisible by 2 we don't want to do anything
		double dDivision = ((blockRadius & 1) != 0 ? 0 : 0.5);
		
		int iHalfRadius = ((int) ((blockRadius / 2) - dDivision));
		
		xStart  = originPos.getX() + iHalfRadius;
		xEnd    = originPos.getX() - iHalfRadius;
		yTop    = originPos.getY() + iHalfRadius;
		yBottom = originPos.getY() - iHalfRadius;
		zStart  = originPos.getZ() + iHalfRadius;
		zEnd    = originPos.getZ() - iHalfRadius;
		
		if (packetId == PacketId.Excavate || packetId == PacketId.Shaftanate || packetId == PacketId.Pathinate || packetId == PacketId.Illuminate) {
			
			switch (faceHit) {
			case SOUTH: // Positive Z
				zStart = originPos.getZ();
				zEnd = originPos.getZ() - blockRadius;
				break;
			case NORTH: // Negative Z
				xStart = originPos.getX() - iHalfRadius;
				xEnd = originPos.getX() + iHalfRadius;
				zStart = originPos.getZ();
				zEnd = originPos.getZ() + blockRadius;
				break;
			case EAST: // Positive X
				xStart = originPos.getX();
				xEnd = originPos.getX() - blockRadius;
				break;
			case WEST: // Negative X
				xStart = originPos.getX();
				xEnd = originPos.getX() + blockRadius;
				zStart = originPos.getZ() - iHalfRadius;
				zEnd = originPos.getZ() + iHalfRadius;
				break;
			case UP:
				yTop = originPos.getY();
				yBottom = originPos.getY() - blockRadius;
				break;
			case DOWN:
				yTop = originPos.getY();
				yBottom = originPos.getY() + blockRadius;
				break;
			default:
				break;
			}
		}
		
		interimArea = new AxisAlignedBB(
				xStart, yBottom, zStart,
				xEnd, yTop, zEnd);
		
	}
	
	public BlockPos harvestAreaStartPos() {
		if (refinedArea != null) {
			return new BlockPos(refinedArea.minX, refinedArea.minY, refinedArea.minZ);
		} else {
			return new BlockPos(interimArea.minX, interimArea.minY, interimArea.minZ);
		}
	}
	
	public BlockPos harvestAreaEndPos() {
		if (refinedArea != null) {
			return new BlockPos(refinedArea.maxX, refinedArea.maxY, refinedArea.maxZ);
		} else {
			return new BlockPos(interimArea.maxX, interimArea.maxY, interimArea.maxZ);
		}
	}
	
	public void addConnectedToQueue(BlockPos pos) {
		for (int xOffset = -1; xOffset <= 1; xOffset++)
			for (int yOffset = -1; yOffset <= 1; yOffset++)
				for (int zOffset = -1; zOffset <= 1; zOffset++)
					addToQueue(pos.add(xOffset, yOffset, zOffset));
	}
	
	// Adds the block position to the current block queue.
	public void addToQueue(BlockPos pos) {
		final BlockState state = world.getBlockState(pos);
		
		if (state == null || state.isAir(world, originPos) ||
				state.getBlock() == Blocks.TORCH || state.getBlock() == Blocks.BEDROCK ||
				state.getMaterial() == Material.WATER || state.getMaterial() == Material.LAVA ||
				pos == null || processed.contains(pos) || queued.contains(pos) ||
				(interimArea != null && !Functions.isWithinArea(pos, interimArea)))
			return;
		
		queued.add(pos);
		
		calculateRefinedArea(pos);
	}
	
	public void calculateRefinedArea(BlockPos currentPos) {
		// Set initial minimum area position
		if (minAreaPos.equals(BlockPos.ZERO)) {
			minAreaPos = currentPos;
		}
		// Set initial maximum area position
		if (maxAreaPos.equals(BlockPos.ZERO)) {
			maxAreaPos = currentPos;
		}
		
		// Update minimum area position
		if (currentPos.getX() < minAreaPos.getX()) {
			minAreaPos = new BlockPos(currentPos.getX(), minAreaPos.getY(), minAreaPos.getZ());
		}
		if (currentPos.getY() < minAreaPos.getY()) {
			minAreaPos = new BlockPos(minAreaPos.getX(), currentPos.getY(), minAreaPos.getZ());
		}
		if (currentPos.getZ() < minAreaPos.getZ()) {
			minAreaPos = new BlockPos(minAreaPos.getX(), minAreaPos.getY(), currentPos.getZ());
		}
		// Update maximum area position
		if (currentPos.getX() > maxAreaPos.getX()) {
			maxAreaPos = new BlockPos(currentPos.getX(), maxAreaPos.getY(), maxAreaPos.getZ());
		}
		if (currentPos.getY() > maxAreaPos.getY()) {
			maxAreaPos = new BlockPos(maxAreaPos.getX(), currentPos.getY(), maxAreaPos.getZ());
		}
		if (currentPos.getZ() > maxAreaPos.getZ()) {
			maxAreaPos = new BlockPos(maxAreaPos.getX(), maxAreaPos.getY(), currentPos.getZ());
		}
		
		// Update the refined harvest area for auto illumination usage
		refinedArea = new AxisAlignedBB(minAreaPos, maxAreaPos);
	}
	
	// Returns true when complete or cancelled
	public boolean tick() {
		return queued.isEmpty();
	}
	
	public void processBlockSnapshots() {
		world.captureBlockSnapshots = false;
		AgentProcessor.INSTANCE.setCurrentAgent(player.getUniqueID(), null);
		while (world.capturedBlockSnapshots.size() > 0) {
			BlockSnapshot snap = world.capturedBlockSnapshots.get(0);
			// if(pos.equals(snap.getPos()))
			// history.addRecordedBlock(new BlockHistory(snap));
			world.capturedBlockSnapshots.remove(0);
			
			world.markAndNotifyBlock(
					snap.getPos(),
					world.getChunk(snap.getPos().getX() >> 4, snap.getPos().getZ() >> 4),
					snap.getReplacedBlock().getBlockState(),
					snap.getCurrentBlock().getBlockState(),
					snap.getFlag(),
					1); // 1 = force diagonal neighbour updates
		}
		AgentProcessor.INSTANCE.setCurrentAgent(player.getUniqueID(), this);
	}
	
	public void excavateOreVein(BlockState state, BlockPos pos) {
		
		// If mine veins is enabled send message back to player from processing.
		PacketVeinate.process(player, new PacketVeinate(pos, Variables.get(player.getUniqueID()).faceHit, Block.getStateId(state)));
		
	}
	
	public boolean hasBeenProcessed(BlockPos pos) {
		return processed.contains(pos);
	}
	
	public boolean shouldProcess(BlockPos pos) {
		return !processed.contains(pos) && queued.contains(pos) && (interimArea == null || Functions.isWithinArea(pos, interimArea));
	}
	
	public void reportProgessToClient(BlockPos pos) {
		reportProgessToClient(pos, SoundEvents.BLOCK_STONE_BREAK);
	}
	
	public void reportProgessToClient(BlockPos pos, SoundEvent soundType) {
		if (world instanceof ServerWorld) {
			
			Functions.playSound(world, pos, soundType, SoundCategory.BLOCKS, 1.0F, 1.0F);
			Functions.spawnAreaEffectCloud(world, player, pos);
			
		}
	}
	
	public boolean HarvestBlock(BlockPos pos) {
		
		boolean bResult = getPlayer().interactionManager.tryHarvestBlock(pos);
		player.connection.sendPacket(new SChangeBlockPacket(world, pos));
		
		final int  range = 20;
		BlockState state = world.getBlockState(pos);
		
		List<ServerPlayerEntity> localPlayers = world.getEntitiesWithinAABB(ServerPlayerEntity.class, new AxisAlignedBB(pos.add(-range, -range, -range), pos.add(range, range, range)));
		
		world.playBroadcastSound(2001, pos, Block.getStateId(state));
		world.getChunkProvider().getLightManager().checkBlock(pos);
		
		Block blockAbove = Functions.getBlockFromWorld(world, pos.up());
		if (blockAbove instanceof FallingBlock && HarvestBlock(pos.up())) {
			for (Entity entity : localPlayers)
				Functions.spawnAreaEffectCloud(world, (PlayerEntity) entity, pos);
		}
		
		return bResult;
	}
}
