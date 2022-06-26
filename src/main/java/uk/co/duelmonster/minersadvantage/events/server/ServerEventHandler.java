package uk.co.duelmonster.minersadvantage.events.server;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.IGrowable;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.common.Functions;
import uk.co.duelmonster.minersadvantage.common.Variables;
import uk.co.duelmonster.minersadvantage.config.MAConfig_Client;
import uk.co.duelmonster.minersadvantage.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.helpers.LumbinationHelper;
import uk.co.duelmonster.minersadvantage.network.packets.PacketCropinate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketCultivate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketExcavate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketLumbinate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketPathanate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketShaftanate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketVeinate;
import uk.co.duelmonster.minersadvantage.workers.AgentProcessor;
import uk.co.duelmonster.minersadvantage.workers.DropsSpawner;

public class ServerEventHandler {

  // Player Logged In event
  @SubscribeEvent
  public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    ServerPlayerEntity player    = (ServerPlayerEntity) event.getPlayer();
    Variables          variables = Variables.get(player.getUUID());

    variables.HasPlayerSpawned = true;

    // Sync Variables with the player
    Variables.syncToPlayer(player);
  }

  // Player Logged Out event
  @SubscribeEvent
  public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
    ServerPlayerEntity player    = (ServerPlayerEntity) event.getPlayer();
    Variables          variables = Variables.get(player.getUUID());

    variables.HasPlayerSpawned = false;

    // Sync Variables with the player
    Variables.syncToPlayer(player);
  }

  @SubscribeEvent
  public void onServerTick(TickEvent.ServerTickEvent event) {
    if (event.phase != TickEvent.Phase.END)
      return;

    MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
    if (null == server)
      return;

    List<ServerPlayerEntity> players = server.getPlayerList().getPlayers();
    if (players != null && !players.isEmpty()) {
      for (ServerPlayerEntity player : players) {
        Variables vars = Variables.get(player.getUUID());

        if (vars != null) {

          boolean shouldSkip = vars.skipNext;

          if (shouldSkip) {
            vars.skipNext = false;
            return;
          }

          // Sync Variables and Settings with the player
          Variables.syncToPlayer(player);

          if (!shouldSkip) {
            AgentProcessor.INSTANCE.fireAgentTicks(player.level);
            AgentProcessor.INSTANCE.setCurrentAgent(player.getUUID(), null);
          }
        }
      }
    }
  }

  @SubscribeEvent
  public void onWorldUnload(WorldEvent.Unload event) {
    World world = (World) event.getWorld();
    if (world.isClientSide || world.getServer().isRunning())
      return;

    AgentProcessor.INSTANCE.resetAgentList();
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onEntitySpawn(EntityJoinWorldEvent event) {
    World  world  = event.getWorld();
    Entity entity = event.getEntity();
    if (!(entity instanceof LivingEntity)) {
      if (world.isClientSide
          || !event.getEntity().isAlive()
          || event.isCanceled()
          || (!(entity instanceof ItemEntity) && !(entity instanceof ExperienceOrbEntity))) {
        return;
      }

      if (!AgentProcessor.INSTANCE.currentAgents.isEmpty()) {
        if (event.getEntity() instanceof ItemEntity) {
          ItemEntity eItem = (ItemEntity) event.getEntity();

          DropsSpawner.recordDrop(eItem);

          event.setCanceled(true);
        } else if (event.getEntity() instanceof ExperienceOrbEntity) {
          ExperienceOrbEntity orb = (ExperienceOrbEntity) event.getEntity();

          DropsSpawner.addXP(orb.getValue());

          event.setCanceled(true);
        }
      }
    }
  }

  @SubscribeEvent
  public void onBlockBreak(BlockEvent.BreakEvent event) {

    if (!(event.getPlayer() instanceof ServerPlayerEntity)
        || (event.getPlayer() instanceof FakePlayer)
        || event.getState().getBlock().isAir(event.getState(), event.getWorld(), event.getPos()))
      return;

    ServerPlayerEntity player = (ServerPlayerEntity) event.getPlayer();

    // Ensure that the currently active agent wasn't responsible for this Break Event
    if (!AgentProcessor.INSTANCE.currentAgents.isEmpty() &&
        AgentProcessor.INSTANCE.getCurrentAgent(player.getUUID()) != null &&
        !AgentProcessor.INSTANCE.getCurrentAgent(player.getUUID()).shouldProcess(event.getPos()))
      return;

    Variables          variables    = Variables.get(player.getUUID());
    SyncedClientConfig clientConfig = MAConfig_Client.getPlayerConfig(player.getUUID());

    BlockPos   pos     = event.getPos();
    BlockState state   = event.getState();
    Direction  faceHit = variables.faceHit;

    ItemStack heldItem = player.getItemInHand(Hand.MAIN_HAND);
    if (heldItem.isEmpty())
      return;

    LumbinationHelper lumbinationHelper = new LumbinationHelper();
    lumbinationHelper.setPlayer(player);

    if (variables.IsShaftanationToggled) {

      PacketShaftanate.process(player, new PacketShaftanate(pos, faceHit, Block.getId(state)));

    } else if (clientConfig.lumbination.enabled && lumbinationHelper.isValidAxe(heldItem.getItem())) {

      PacketLumbinate.process(player, new PacketLumbinate(pos, faceHit, Block.getId(state)));

    } else if (clientConfig.veination.enabled && Functions.isValidOre(state, clientConfig)) {

      PacketVeinate.process(player, new PacketVeinate(pos, faceHit, Block.getId(state)));

    } else if (variables.IsExcavationToggled || variables.IsSingleLayerToggled) {
      if (clientConfig.excavation.isBlacklisted(event.getState().getBlock()))
        return;

      PacketExcavate.process(player, new PacketExcavate(pos, faceHit, Block.getId(state)));

    }
  }

  @SubscribeEvent
  public void onBlockBreak(BlockEvent.BlockToolInteractEvent event) {
    boolean isHoe    = event.getToolType() == ToolType.HOE;
    boolean isShovel = event.getToolType() == ToolType.SHOVEL;

    if (isHoe || isShovel) {
      PlayerEntity playerEntity = event.getPlayer();
      World        world        = playerEntity.level;
      if (world.isClientSide || !(playerEntity instanceof ServerPlayerEntity) || (playerEntity instanceof FakePlayer))
        return;

      ServerPlayerEntity player = (ServerPlayerEntity) playerEntity;
      BlockPos           pos    = event.getPos();
      Block              block  = event.getState().getBlock();

      if (Constants.DIRT_BLOCKS.contains(block) || block.equals(Blocks.GRASS_PATH)) {
        if (isShovel) {

          PacketPathanate.process(player, new PacketPathanate(pos));

        } else if (isHoe) {

          PacketCultivate.process(player, new PacketCultivate(pos));

        }
      } else if (isHoe && (block instanceof IGrowable || block instanceof IPlantable || block instanceof CropsBlock || block instanceof NetherWartBlock)) {

        PacketCropinate.process(player, new PacketCropinate(pos));

      }
    }
  }
}
