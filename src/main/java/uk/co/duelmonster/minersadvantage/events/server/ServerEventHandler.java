package uk.co.duelmonster.minersadvantage.events.server;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.server.ServerLifecycleHooks;
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

@Mod.EventBusSubscriber(modid = Constants.MOD_ID,
                        bus = Bus.FORGE)
public class ServerEventHandler {

  // Player Logged In event
  @SubscribeEvent
  public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    ServerPlayer player    = (ServerPlayer) event.getPlayer();
    Variables    variables = Variables.get(player.getUUID());

    variables.HasPlayerSpawned = true;

    // Sync Variables with the player
    Variables.syncToPlayer(player);
  }

  // Player Logged Out event
  @SubscribeEvent
  public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
    ServerPlayer player    = (ServerPlayer) event.getPlayer();
    Variables    variables = Variables.get(player.getUUID());

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

    List<ServerPlayer> players = server.getPlayerList().getPlayers();
    if (players != null && !players.isEmpty()) {
      for (ServerPlayer player : players) {
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
    Level world = (Level) event.getWorld();
    if (world.isClientSide || world.getServer().isRunning())
      return;

    AgentProcessor.INSTANCE.resetAgentList();
  }

  @SubscribeEvent(priority = EventPriority.LOWEST)
  public void onEntitySpawn(EntityJoinWorldEvent event) {
    Level  world  = event.getWorld();
    Entity entity = event.getEntity();
    if (!(entity instanceof LivingEntity)) {
      if (world.isClientSide
          || !event.getEntity().isAlive()
          || event.isCanceled()
          || (!(entity instanceof ItemEntity) && !(entity instanceof ExperienceOrb))) {
        return;
      }

      if (!AgentProcessor.INSTANCE.currentAgents.isEmpty()) {
        if (event.getEntity() instanceof ItemEntity) {
          ItemEntity eItem = (ItemEntity) event.getEntity();

          DropsSpawner.recordDrop(eItem);

          event.setCanceled(true);
        } else if (event.getEntity() instanceof ExperienceOrb) {
          ExperienceOrb orb = (ExperienceOrb) event.getEntity();

          DropsSpawner.addXP(orb.getValue());

          event.setCanceled(true);
        }
      }
    }
  }

  @SubscribeEvent
  public void onBlockBreak(BlockEvent.BreakEvent event) {

    if (!(event.getPlayer() instanceof ServerPlayer)
        || (event.getPlayer() instanceof FakePlayer)
        || event.getState().isAir())
      return;

    ServerPlayer player = (ServerPlayer) event.getPlayer();

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

    ItemStack heldItem = player.getItemInHand(InteractionHand.MAIN_HAND);
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
  public void onBlockBreak(BlockEvent.BlockToolModificationEvent event) {
    boolean isHoe    = event.getToolAction() == ToolActions.HOE_TILL;
    boolean isShovel = event.getToolAction() == ToolActions.SHOVEL_FLATTEN;

    if (isHoe || isShovel) {
      Player playerEntity = event.getPlayer();
      Level  world        = playerEntity.level;
      if (world.isClientSide || !(playerEntity instanceof ServerPlayer) || (playerEntity instanceof FakePlayer))
        return;

      ServerPlayer player = (ServerPlayer) playerEntity;
      BlockPos     pos    = event.getPos();
      Block        block  = event.getState().getBlock();

      if (Constants.DIRT_BLOCKS.contains(block) || block.equals(Blocks.DIRT_PATH)) {
        if (isShovel) {

          PacketPathanate.process(player, new PacketPathanate(pos));

        } else if (isHoe) {

          PacketCultivate.process(player, new PacketCultivate(pos));

        }
      } else if (isHoe && (block instanceof BonemealableBlock || block instanceof IPlantable || block instanceof CropBlock || block instanceof NetherWartBlock)) {

        PacketCropinate.process(player, new PacketCropinate(pos));

      }
    }
  }
}
