package uk.co.duelmonster.minersadvantage.workers;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import uk.co.duelmonster.minersadvantage.common.TorchPlacement;
import uk.co.duelmonster.minersadvantage.common.Variables;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.network.packets.PacketIlluminate;

public class AgentProcessor {
  public static final AgentProcessor INSTANCE = new AgentProcessor();

  public ConcurrentHashMap<UUID, Agent>                              currentAgents = new ConcurrentHashMap<UUID, Agent>();
  private ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, Agent>> activeAgents  = new ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, Agent>>();

  // private HashMap<UUID, ReversalData> reversing = new HashMap<UUID, ReversalData>();
  // private HashMap<UUID, List<ReversalData>> reversalLog = new HashMap<UUID,
  // List<ReversalData>>();

  // private Stopwatch timer = Stopwatch.createUnstarted();
  // private int tickCount = 0;

  public ConcurrentHashMap<Integer, Agent> getAgentsByID(UUID uuid) {
    return activeAgents.get(uuid);
  }

  /*
   * Minecraft's game loop normally runs at a fixed rate of 20 ticks per second, so one tick
   * happens every 0.05
   * seconds. An in-game day lasts exactly 24000 ticks, or 20 minutes.
   */
  public void fireAgentTicks(Level world) {
    // tickCount++;

    Iterator<Entry<UUID, ConcurrentHashMap<Integer, Agent>>> allAgents = activeAgents.entrySet().iterator();
    while (allAgents.hasNext()) {
      Entry<UUID, ConcurrentHashMap<Integer, Agent>> allAgentsEntry = allAgents.next();
      UUID                                           uuid           = allAgentsEntry.getKey();
      Variables                                      variables      = Variables.get(uuid);
      // SyncedClientConfig clientConfig = MAConfig_Client.getPlayerConfig(uuid);

      Iterator<Entry<Integer, Agent>> playerAgents = allAgentsEntry.getValue().entrySet().iterator();
      while (playerAgents.hasNext()) {
        Agent agent = playerAgents.next().getValue();

        // // If the Tick Delay is enabled and the current tick doesn't match the delay count we skip
        // the
        // // agent for
        // // this player until next tick
        // if (!clientConfig.common.breakAtToolSpeeds && isAgentDelayable(agent) &&
        // clientConfig.common.enableTickDelay && tickCount % clientConfig.common.tickDelay != 0)
        // continue;

        boolean isComplete = false;

        if (!agent.awaitingAutoIllumination) {
          setCurrentAgent(allAgentsEntry.getKey(), agent);
          isComplete = agent.tick();
          setCurrentAgent(allAgentsEntry.getKey(), null);

          agent.dropsHistory.addAll(DropsSpawner.getDrops());

          DropsSpawner.spawnDrops(uuid, world, agent.originPos);
        }

        if (isComplete) {
          if (agent.shouldAutoIlluminate) {
            BlockPos startPos = agent.harvestAreaStartPos();
            BlockPos endPos   = agent.harvestAreaEndPos();

            if (agent.packetId == PacketId.Shaftanate) {
              // Adjust Area End Pos to only 1 Block high
              BlockPos shaftEndPos = new BlockPos(endPos.getX(), Math.min(startPos.getY(), endPos.getY()), endPos.getZ());

              PacketIlluminate.process(agent.player, new PacketIlluminate(agent.packetId, startPos, shaftEndPos, agent.clientConfig.shaftanation.torchPlacement));
            } else if (agent.packetId == PacketId.Ventilate) {
              PacketIlluminate.process(agent.player, new PacketIlluminate(agent.packetId, startPos, endPos, Direction.NORTH, TorchPlacement.BOTH_WALLS));
            } else {
              PacketIlluminate.process(agent.player, new PacketIlluminate(agent.packetId, startPos, endPos, Direction.UP));
            }
          }

          reportAgentCompletionToClient(agent, variables);
          playerAgents.remove();

        }
      }

      if (allAgentsEntry.getValue().isEmpty())
        allAgents.remove();
    }
  }

  public Agent startProcessing(ServerPlayer player, Agent agent) {
    UUID                              uid          = player.getUUID();
    ConcurrentHashMap<Integer, Agent> playerAgents = activeAgents.get(uid);

    if (playerAgents == null) {
      playerAgents = new ConcurrentHashMap<Integer, Agent>();
      activeAgents.put(uid, playerAgents);
    }

    playerAgents.put(playerAgents.size() + 1, agent);

    return agent;
  }

  public void stopProcessing(ServerPlayer player) {
    UUID uid = player.getUUID();

    if (activeAgents.containsKey(uid)) {
      Iterator<Entry<Integer, Agent>> playerAgents = activeAgents.get(uid).entrySet().iterator();

      while (playerAgents.hasNext()) {

        Agent agent = playerAgents.next().getValue();

        DropsSpawner.spawnDrops(uid, player.level, agent.originPos);

        reportAgentCompletionToClient(agent, Variables.get(uid));
        playerAgents.remove();
      }

      activeAgents.remove(uid);
    }
  }

  public Agent getCurrentAgent(UUID uid) {
    return currentAgents.get(uid);
  }

  public Agent setCurrentAgent(UUID uid, Agent agent) {
    if (agent != null)
      return currentAgents.put(uid, agent);
    else if (currentAgents.get(uid) != null)
      currentAgents.remove(uid);

    return null;
  }

  public void resetAgentList() {
    currentAgents.clear();
    activeAgents.clear();
  }

  private void reportAgentCompletionToClient(Agent agent, Variables variables) {
    if (agent instanceof CropinationAgent)
      variables.IsCropinating = false;
    else if (agent instanceof ExcavationAgent && agent.packetId == PacketId.Excavate)
      variables.IsExcavating = false;
    else if (agent instanceof ExcavationAgent && agent.packetId == PacketId.Veinate)
      variables.IsVeinating = false;
    else if (agent instanceof IlluminationAgent)
      variables.IsIlluminating = false;
    else if (agent instanceof LumbinationAgent)
      variables.IsLumbinating = false;
    else if (agent instanceof PathanationAgent)
      variables.IsPathanating = false;
    else if (agent instanceof ShaftanationAgent)
      variables.IsShaftanating = false;
    else if (agent instanceof VentilationAgent)
      variables.IsVentilating = false;

    Variables.syncToPlayer(agent.player);
  }

  // private boolean isAgentDelayable(Agent agent) {
  // return (agent instanceof LumbinationAgent)
  // || (agent instanceof CropinationAgent)
  // || (agent instanceof ExcavationAgent)
  // || (agent instanceof PathanationAgent)
  // || (agent instanceof ShaftanationAgent);
  // }
}
