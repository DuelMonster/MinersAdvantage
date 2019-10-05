package uk.co.duelmonster.minersadvantage.workers;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.google.common.base.Stopwatch;
import com.mojang.authlib.GameProfile;

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
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.common.TorchPlacement;
import uk.co.duelmonster.minersadvantage.common.Variables;
import uk.co.duelmonster.minersadvantage.config.MAConfig;
import uk.co.duelmonster.minersadvantage.helpers.BreakBlockController;
import uk.co.duelmonster.minersadvantage.helpers.IlluminationHelper;
import uk.co.duelmonster.minersadvantage.network.NetworkHandler;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.network.packets.IMAPacket;
import uk.co.duelmonster.minersadvantage.network.packets.PacketIlluminate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketVeinate;

public abstract class Agent {

	public final World world;
	public final ServerPlayerEntity player;

	public List<Entity> dropsHistory = Collections.synchronizedList(new ArrayList<Entity>());

	public WeakReference<FakePlayer> _fakePlayer;

	public FakePlayer fakePlayer() {
		FakePlayer ret = _fakePlayer != null ? _fakePlayer.get() : null;
		if (ret == null) {
			ret = FakePlayerFactory.get(player.getServerWorld(), new GameProfile(UUID.randomUUID(), Constants.MOD_NAME));
			_fakePlayer = new WeakReference<FakePlayer>(ret);
		}
		return ret;
	}

	public PacketId packetId = PacketId.INVALID;

	public Stopwatch timer = Stopwatch.createUnstarted();
	public ItemStack heldItemStack = null;
	public Item heldItem = null;
	public int heldItemSlot = -1;
	public Direction playerFacing;
	public Direction faceHit = Direction.SOUTH;
	public int feetPos = 0;
	public BlockPos originPos;
	public BlockState originState = null;
	public Block originBlock = null;
	public boolean isRedStone = false;
	public AxisAlignedBB harvestArea = null;
	public BreakBlockController breakController = null;
	public boolean awaitingAutoIllumination = false;
	public boolean shouldAutoIlluminate = false;

	public List<BlockPos> processed = new ArrayList<BlockPos>();
	public List<BlockPos> queued = new ArrayList<BlockPos>();

	public List<BlockPos> illuminationPositions = null;
	
	public Agent(ServerPlayerEntity player, IMAPacket pkt) {

		this.packetId = (PacketId)pkt.getPacketId();

		this.world = player.world;
		this.player = player;
		this.feetPos = (int)player.getBoundingBox().minY;

		this.playerFacing = Functions.getPlayerFacing(player);

		this.heldItemStack = Functions.getHeldItemStack(player);
		this.heldItem = Functions.getHeldItem(player);
		this.heldItemSlot = player.inventory.currentItem;

		this.fakePlayer().connection = player.connection;
		if (this.heldItemStack != null)
			this.fakePlayer().setHeldItem(Hand.MAIN_HAND, this.heldItemStack);
	}

	protected void setupHarvestArea() {
		// Shaft area info
		int xStart = 0;
		int xEnd = 0;
		int yBottom = feetPos;
		int yTop = feetPos + (MAConfig.CLIENT.common.blockRadius() - 1);
		int zStart = 0;
		int zEnd = 0;

		// if the ShaftWidth is divisible by 2 we don't want to do anything
		double dDivision = ((MAConfig.CLIENT.common.blockRadius() & 1) != 0 ? 0 : 0.5);

		int iHalfRadius = ((int)((MAConfig.CLIENT.common.blockRadius() / 2) - dDivision));

		xStart = originPos.getX() + iHalfRadius;
		xEnd = originPos.getX() - iHalfRadius;
		yTop = originPos.getY() + iHalfRadius;
		yBottom = originPos.getY() - iHalfRadius;
		zStart = originPos.getZ() + iHalfRadius;
		zEnd = originPos.getZ() - iHalfRadius;

		if (packetId == PacketId.Excavate || packetId == PacketId.Shaftanate || packetId == PacketId.Pathinate) {
			int blockRadius = (MAConfig.CLIENT.common.blockRadius() - 1);
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

		harvestArea = new AxisAlignedBB(
				xStart, yBottom, zStart,
				xEnd, yTop, zEnd);

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

		if (state == null || state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.TORCH ||
				state.getBlock() == Blocks.BEDROCK || state.getMaterial() == Material.WATER ||
				state.getMaterial() == Material.LAVA || pos == null || processed.contains(pos) ||
				queued.contains(pos) || (harvestArea != null && !Functions.isWithinArea(pos, harvestArea)))
			return;

		queued.add(pos);
	}

	// Returns true when complete or cancelled
	public boolean tick() { return queued.isEmpty(); }

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
					snap.getReplacedBlock(),
					snap.getCurrentBlock(),
					snap.getFlag());
		}
		AgentProcessor.INSTANCE.setCurrentAgent(player.getUniqueID(), this);
	}

	public void autoIlluminateArea() {
		if (shouldAutoIlluminate) {
			if (illuminationPositions == null) {
				illuminationPositions = Functions.getAllPositionsInAreaTorchable(world, harvestArea);
			}

			for (int i = 0; i < illuminationPositions.size(); i++) {
				if (!illuminationPositions.isEmpty()) {
					boolean torchPlaced = autoIlluminate(illuminationPositions.get(0));
					illuminationPositions.remove(0);
					
					if (torchPlaced) break;
				}
			}
		}
	}

	public boolean autoIlluminate(BlockPos pos) { return autoIlluminate(pos, TorchPlacement.FLOOR); }

	public boolean autoIlluminate(BlockPos pos, TorchPlacement torchPlacement) {
		if (shouldAutoIlluminate) {
			Direction placeOnFace = Direction.UP;
			BlockPos finalPos = pos;

			if (torchPlacement.getIndex() > TorchPlacement.FLOOR.getIndex()) {
				finalPos = pos.up();

				switch (playerFacing) {
				case NORTH:
					if (torchPlacement == TorchPlacement.RIGHT_WALL) { placeOnFace = Direction.WEST; }
					if (torchPlacement == TorchPlacement.LEFT_WALL) { placeOnFace = Direction.EAST; }
					break;
				case EAST:
					if (torchPlacement == TorchPlacement.RIGHT_WALL) { placeOnFace = Direction.NORTH; }
					if (torchPlacement == TorchPlacement.LEFT_WALL) { placeOnFace = Direction.SOUTH; }
					break;
				case SOUTH:
					if (torchPlacement == TorchPlacement.RIGHT_WALL) { placeOnFace = Direction.EAST; }
					if (torchPlacement == TorchPlacement.LEFT_WALL) { placeOnFace = Direction.WEST; }
					break;
				case WEST:
					if (torchPlacement == TorchPlacement.RIGHT_WALL) { placeOnFace = Direction.SOUTH; }
					if (torchPlacement == TorchPlacement.LEFT_WALL) { placeOnFace = Direction.NORTH; }
					break;
				default:
					break;
				}
			}

			// Offset the torch placement position to allow block updates to calculate light levels
			BlockPos lightCheckPos = (packetId == PacketId.Shaftanate ? pos.offset(faceHit, 3) : pos);
			BlockPos torchPlacePos = (packetId == PacketId.Shaftanate ? finalPos.offset(faceHit, 3) : finalPos);
			BlockPos torchPlaceOnFacePos = torchPlacePos.offset(placeOnFace.getOpposite());
			//BlockState statePlaceOnFace = world.getBlockState(torchPlaceOnFacePos);
			BlockState statePlace = world.getBlockState(torchPlacePos);

			boolean canTorchBePlacedOnFace = IlluminationHelper.canPlaceTorchOnFace(world, torchPlaceOnFacePos, placeOnFace);
			
			int blockLightLevel = world.getLightFor((MAConfig.CLIENT.illumination.useBlockLight() ? LightType.BLOCK : LightType.SKY), lightCheckPos);

			if (canTorchBePlacedOnFace
					&& blockLightLevel <= MAConfig.CLIENT.illumination.lowestLightLevel()
					&& (world.isAirBlock(torchPlacePos) || statePlace.getMaterial().isReplaceable())) {

				NetworkHandler.sendToServer(new PacketIlluminate(torchPlacePos, placeOnFace));
				return true;
			}
		}
		return false;
	}

	public void excavateOreVein(BlockState state, BlockPos pos) {

		// If mine veins is enabled send message back to player from processing.
		NetworkHandler.sendToServer(new PacketVeinate(pos, Variables.get(player.getUniqueID()).faceHit, Block.getStateId(state)));

	}

	public boolean hasBeenProcessed(BlockPos pos) { return processed.contains(pos); }

	public boolean shouldProcess(BlockPos pos) { return !processed.contains(pos) && queued.contains(pos) && (harvestArea == null || Functions.isWithinArea(pos, harvestArea)); }

	public void reportProgessToClient(BlockPos pos) { reportProgessToClient(pos, SoundEvents.BLOCK_STONE_BREAK); }

	public void reportProgessToClient(BlockPos pos, SoundEvent soundType) {
		if (world instanceof ServerWorld) {

			Functions.playSound(world, pos, soundType, SoundCategory.BLOCKS, 1.0F, 1.0F);
			Functions.spawnAreaEffectCloud(world, player, pos);

		}
	}

	public boolean HarvestBlock(BlockPos pos) {

		boolean bResult = fakePlayer().interactionManager.tryHarvestBlock(pos);
		player.connection.sendPacket(new SChangeBlockPacket(world, pos));

		final int range = 20;
		BlockState state = world.getBlockState(pos);

		List<ServerPlayerEntity> localPlayers = world.getEntitiesWithinAABB(ServerPlayerEntity.class, new AxisAlignedBB(pos.add(-range, -range, -range), pos.add(range, range, range)));

		world.playBroadcastSound(2001, pos, Block.getStateId(state));
		world.getChunkProvider().getLightManager().checkBlock(pos);

		Block blockAbove = Functions.getBlockFromWorld(world, pos.up());
		if (blockAbove instanceof FallingBlock && HarvestBlock(pos.up())) {
			for (Entity entity : localPlayers)
				Functions.spawnAreaEffectCloud(world, (PlayerEntity)entity, pos);
		}

		return bResult;
	}
}
