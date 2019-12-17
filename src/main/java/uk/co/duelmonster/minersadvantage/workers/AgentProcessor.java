package uk.co.duelmonster.minersadvantage.workers;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import uk.co.duelmonster.minersadvantage.common.Variables;
import uk.co.duelmonster.minersadvantage.config.MAConfig_Client;
import uk.co.duelmonster.minersadvantage.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.network.packetids.PacketId;
import uk.co.duelmonster.minersadvantage.network.packets.PacketIlluminate;

public class AgentProcessor {
	public static final AgentProcessor INSTANCE = new AgentProcessor();
	
	public ConcurrentHashMap<UUID, Agent>								currentAgents	= new ConcurrentHashMap<UUID, Agent>();
	private ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, Agent>>	activeAgents	= new ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, Agent>>();
	
	// private HashMap<UUID, ReversalData> reversing = new HashMap<UUID, ReversalData>();
	// private HashMap<UUID, List<ReversalData>> reversalLog = new HashMap<UUID, List<ReversalData>>();
	
	// private Stopwatch timer = Stopwatch.createUnstarted();
	private int tickCount = 0;
	
	public ConcurrentHashMap<Integer, Agent> getAgentsByID(UUID uuid) {
		return activeAgents.get(uuid);
	}
	
	/*
	 * Minecraft's game loop normally runs at a fixed rate of 20 ticks per second, so one tick happens every 0.05
	 * seconds. An in-game day lasts exactly 24000 ticks, or 20 minutes.
	 */
	public void fireAgentTicks(World world) {
		tickCount++;
		
		Iterator<Entry<UUID, ConcurrentHashMap<Integer, Agent>>> allAgents = activeAgents.entrySet().iterator();
		while (allAgents.hasNext()) {
			Entry<UUID, ConcurrentHashMap<Integer, Agent>> allAgentsEntry = allAgents.next();
			UUID uuid = allAgentsEntry.getKey();
			Variables variables = Variables.get(uuid);
			SyncedClientConfig clientConfig = MAConfig_Client.getPlayerConfig(uuid);
			
			Iterator<Entry<Integer, Agent>> playerAgents = allAgentsEntry.getValue().entrySet().iterator();
			while (playerAgents.hasNext()) {
				Agent agent = playerAgents.next().getValue();
				
				// If the Tick Delay is enabled and the current tick doesn't match the delay count we skip the agent for
				// this player until next tick
				if (!clientConfig.common.breakAtToolSpeeds && isAgentDelayable(agent) && clientConfig.common.enableTickDelay && tickCount % clientConfig.common.tickDelay != 0)
					continue;
				
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
						if (agent.packetId == PacketId.Shaftanate) {
							PacketIlluminate.process(agent.player, new PacketIlluminate(agent.harvestAreaStartPos(), agent.harvestAreaEndPos(), agent.clientConfig.shaftanation.torchPlacement));
						} else {
							PacketIlluminate.process(agent.player, new PacketIlluminate(agent.harvestAreaStartPos(), agent.harvestAreaEndPos(), Direction.UP));
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
	
	public Agent startProcessing(ServerPlayerEntity player, Agent agent) {
		UUID uid = player.getUniqueID();
		ConcurrentHashMap<Integer, Agent> playerAgents = activeAgents.get(uid);
		
		if (playerAgents == null) {
			playerAgents = new ConcurrentHashMap<Integer, Agent>();
			activeAgents.put(uid, playerAgents);
		}
		
		playerAgents.put(playerAgents.size() + 1, agent);
		
		return agent;
	}
	
	public void stopProcessing(ServerPlayerEntity player) {
		UUID uid = player.getUniqueID();
		
		if (activeAgents.containsKey(uid)) {
			Iterator<Entry<Integer, Agent>> playerAgents = activeAgents.get(uid).entrySet().iterator();
			
			while (playerAgents.hasNext()) {
				
				Agent agent = playerAgents.next().getValue();
				
				DropsSpawner.spawnDrops(uid, player.world, agent.originPos);
				
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
		
		Variables.syncToPlayer(agent.player);
	}
	
	private boolean isAgentDelayable(Agent agent) {
		return (agent instanceof LumbinationAgent)
				|| (agent instanceof CropinationAgent)
				|| (agent instanceof ExcavationAgent)
				|| (agent instanceof PathanationAgent)
				|| (agent instanceof ShaftanationAgent);
	}
}
