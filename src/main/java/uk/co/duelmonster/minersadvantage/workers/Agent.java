package uk.co.duelmonster.minersadvantage.workers;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Stopwatch;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.common.Variables;
import uk.co.duelmonster.minersadvantage.config.MAConfig_Client;
import uk.co.duelmonster.minersadvantage.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.network.packets.IMAPacket;
import uk.co.duelmonster.minersadvantage.network.packets.PacketVeinate;

public abstract class Agent {

  public final Level              world;
  public final ServerPlayer       player;
  public final SyncedClientConfig clientConfig;

  public List<Entity> dropsHistory = Collections.synchronizedList(new ArrayList<Entity>());

  public WeakReference<FakePlayer> _fakePlayer;

  public ServerPlayer getPlayer() {
    return player;
    // FakePlayer ret = _fakePlayer != null ? _fakePlayer.get() : null;
    // if (ret == null) {
    // ret = FakePlayerFactory.get(player.getServerWorld(), new GameProfile(UUID.randomUUID(),
    // Constants.MOD_NAME));
    // _fakePlayer = new WeakReference<FakePlayer>(ret);
    // }
    // return ret;
  }

  public PacketId packetId = PacketId.INVALID;

  public Stopwatch  timer                    = Stopwatch.createUnstarted();
  public ItemStack  heldItemStack            = null;
  public Item       heldItem                 = null;
  public int        heldItemSlot             = -1;
  public Direction  playerFacing;
  public Direction  faceHit                  = Direction.SOUTH;
  public int        feetPos                  = 0;
  public BlockPos   originPos;
  public BlockState originState              = null;
  public Block      originBlock              = null;
  public boolean    isRedStone               = false;
  public boolean    awaitingAutoIllumination = false;
  public boolean    shouldAutoIlluminate     = false;

  public AABB     interimArea = null;
  public AABB     refinedArea = null;
  public BlockPos minAreaPos  = BlockPos.ZERO;
  public BlockPos maxAreaPos  = BlockPos.ZERO;

  public List<BlockPos> processed = new ArrayList<BlockPos>();
  public List<BlockPos> queued    = new ArrayList<BlockPos>();

  public Agent(ServerPlayer player, IMAPacket pkt) {

    this.packetId = (PacketId) pkt.getPacketId();

    this.world        = player.level();
    this.player       = player;
    this.clientConfig = MAConfig_Client.getPlayerConfig(player.getUUID());

    this.feetPos = (int) player.getBoundingBox().minY;

    this.playerFacing = Functions.getPlayerFacing(player);

    this.heldItemStack = Functions.getHeldItemStack(player);
    this.heldItem      = Functions.getHeldItem(player);
    this.heldItemSlot  = player.getInventory().selected;

    this.getPlayer().connection = player.connection;
    if (this.heldItemStack != null)
      this.getPlayer().setItemInHand(InteractionHand.MAIN_HAND, this.heldItemStack);
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

    interimArea = new AABB(
        xStart, yBottom, zStart,
        xEnd, yTop, zEnd);

  }

  public BlockPos harvestAreaStartPos() {
    AABB area = refinedArea != null ? refinedArea : interimArea;
    return new BlockPos((int) area.minX, (int) area.minY, (int) area.minZ);
  }

  public BlockPos harvestAreaEndPos() {
    AABB area = refinedArea != null ? refinedArea : interimArea;
    return new BlockPos((int) area.maxX, (int) area.maxY, (int) area.maxZ);
  }

  public void addConnectedToQueue(BlockPos pos) {
    for (int xOffset = -1; xOffset <= 1; xOffset++)
      for (int yOffset = -1; yOffset <= 1; yOffset++)
        for (int zOffset = -1; zOffset <= 1; zOffset++)
          addToQueue(pos.offset(xOffset, yOffset, zOffset));
  }

  // Adds the block position to the current block queue.
  public void addToQueue(BlockPos pos) {
    final BlockState state = world.getBlockState(pos);

    if (state == null || state.isAir() ||
        state.getBlock() == Blocks.TORCH || state.getBlock() == Blocks.BEDROCK ||
        state.getFluidState().is(Fluids.WATER) || state.getFluidState().is(Fluids.LAVA) ||
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
    refinedArea = new AABB(minAreaPos, maxAreaPos);
  }

  // Returns true when complete or cancelled
  public boolean tick() {
    return queued.isEmpty();
  }

  public void processBlockSnapshots() {
    world.captureBlockSnapshots = false;
    AgentProcessor.INSTANCE.setCurrentAgent(getPlayer().getUUID(), null);
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
          snap.getFlag(),
          1); // 1 = force diagonal neighbour updates
    }
    AgentProcessor.INSTANCE.setCurrentAgent(getPlayer().getUUID(), this);
  }

  public void excavateOreVein(BlockState state, BlockPos pos) {

    // If mine veins is enabled send message back to player from processing.
    PacketVeinate.process(getPlayer(), new PacketVeinate(pos, Variables.get(getPlayer().getUUID()).faceHit, Block.getId(state)));

  }

  public boolean hasBeenProcessed(BlockPos pos) {
    return processed.contains(pos);
  }

  public boolean shouldProcess(BlockPos pos) {
    return !processed.contains(pos) && queued.contains(pos) && (interimArea == null || Functions.isWithinArea(pos, interimArea));
  }

  public void reportProgessToClient(BlockPos pos) {
    reportProgessToClient(pos, SoundEvents.BONE_BLOCK_BREAK);
  }

  public void reportProgessToClient(BlockPos pos, SoundEvent soundType) {
    if (world instanceof ServerLevel) {

      Functions.playSound(world, pos, soundType, SoundSource.BLOCKS, 1.0F, 1.0F);
      Functions.spawnAreaEffectCloud(world, getPlayer(), pos);

    }
  }

  public boolean HarvestBlock(BlockPos pos) {

    boolean bResult = getPlayer().gameMode.destroyBlock(pos);
    getPlayer().connection.send(new ClientboundBlockUpdatePacket(world, pos));

    final int  range = 20;
    BlockState state = world.getBlockState(pos);

    List<ServerPlayer> localPlayers = world.getEntitiesOfClass(ServerPlayer.class, new AABB(pos.offset(-range, -range, -range), pos.offset(range, range, range)));

    world.globalLevelEvent(2001, pos, Block.getId(state));
    world.getChunkSource().getLightEngine().checkBlock(pos);

    Block blockAbove = Functions.getBlockFromWorld(world, pos.above());
    if (blockAbove instanceof FallingBlock && HarvestBlock(pos.above())) {
      for (Entity entity : localPlayers)
        Functions.spawnAreaEffectCloud(world, (Player) entity, pos);
    }

    return bResult;
  }
}
