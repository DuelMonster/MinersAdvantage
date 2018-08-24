package co.uk.duelmonster.minersadvantage.workers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import co.uk.duelmonster.minersadvantage.MinersAdvantage;
import co.uk.duelmonster.minersadvantage.common.JsonHelper;
import co.uk.duelmonster.minersadvantage.common.PacketID;
import co.uk.duelmonster.minersadvantage.common.Variables;
import co.uk.duelmonster.minersadvantage.config.MAConfig;
import co.uk.duelmonster.minersadvantage.packets.NetworkPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class AgentProcessor {
	public static final AgentProcessor instance = new AgentProcessor();
	
	public HashMap<UUID, Agent>						currentAgents	= new HashMap<UUID, Agent>();
	private HashMap<UUID, HashMap<Integer, Agent>>	activeAgents	= new HashMap<UUID, HashMap<Integer, Agent>>();
	
	// private HashMap<UUID, ReversalData> reversing = new HashMap<UUID, ReversalData>();
	// private HashMap<UUID, List<ReversalData>> reversalLog = new HashMap<UUID, List<ReversalData>>();
	
	// private Stopwatch timer = Stopwatch.createUnstarted();
	private int tickCount = 0;
	
	public HashMap<Integer, Agent> getAgentsByID(UUID uuid) {
		return activeAgents.get(uuid);
	}
	
	/*
	 * Minecraft's game loop normally runs at a fixed rate of 20 ticks per second, so one tick happens every 0.05
	 * seconds. An in-game day lasts exactly 24000 ticks, or 20 minutes.
	 */
	public void fireAgentTicks(World world) {
		tickCount++;
		
		Iterator<Entry<UUID, HashMap<Integer, Agent>>> allAgents = activeAgents.entrySet().iterator();
		while (allAgents.hasNext()) {
			Entry<UUID, HashMap<Integer, Agent>> allAgentsEntry = allAgents.next();
			UUID uuid = allAgentsEntry.getKey();
			MAConfig settings = MAConfig.get(uuid);
			Variables variables = Variables.get(uuid);
			
			Iterator<Entry<Integer, Agent>> playerAgents = allAgentsEntry.getValue().entrySet().iterator();
			while (playerAgents.hasNext()) {
				Agent agent = playerAgents.next().getValue();
				
				// If the Tick Delay is enabled and the current tick doesn't match the delay count we skip the agent for
				// this player
				if (!settings.common.bBreakAtToolSpeeds() && isAgentDelayable(agent) && settings.common.bEnableTickDelay() && tickCount % settings.common.iTickDelay() != 0)
					continue;
				
				setCurrentAgent(allAgentsEntry.getKey(), agent);
				boolean bIsComplete = agent.tick();
				setCurrentAgent(allAgentsEntry.getKey(), null);
				
				DropsSpawner.spawnDrops(world, agent.originPos);
				
				if (bIsComplete) {
					reportAgentCompletionToClient(agent, variables);
					playerAgents.remove();
				}
			}
			
			if (allAgentsEntry.getValue().isEmpty())
				allAgents.remove();
		}
	}
	
	public Agent startProcessing(EntityPlayerMP player, Agent agent) {
		UUID uid = player.getUniqueID();
		HashMap<Integer, Agent> playerAgents = activeAgents.get(uid);
		
		if (playerAgents == null) {
			playerAgents = new HashMap<Integer, Agent>();
			activeAgents.put(uid, playerAgents);
		}
		
		playerAgents.put(playerAgents.size() + 1, agent);
		
		return agent;
	}
	
	public void stopProcessing(EntityPlayerMP player) {
		UUID uid = player.getUniqueID();
		
		if (activeAgents.containsKey(uid)) {
			Iterator<Entry<Integer, Agent>> playerAgents = activeAgents.get(uid).entrySet().iterator();
			
			while (playerAgents.hasNext()) {
				
				Agent agent = playerAgents.next().getValue();
				
				DropsSpawner.spawnDrops(player.world, agent.originPos);
				
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
		else if (agent instanceof ExcavationAgent && agent.packetID == PacketID.Excavate)
			variables.IsExcavating = false;
		else if (agent instanceof ExcavationAgent && agent.packetID == PacketID.Veinate)
			variables.IsVeinating = false;
		else if (agent instanceof LumbinationAgent)
			variables.IsLumbinating = false;
		else if (agent instanceof PathanationAgent)
			variables.IsPathanating = false;
		else if (agent instanceof ShaftanationAgent)
			variables.IsShaftanating = false;
		
		NBTTagCompound tags = new NBTTagCompound();
		tags.setInteger("ID", PacketID.SyncVariables.value());
		tags.setString("variables", JsonHelper.gson.toJson(variables));
		
		MinersAdvantage.instance.network.sendTo(new NetworkPacket(tags), agent.player);
	}
	
	private boolean isAgentDelayable(Agent agent) {
		return (agent instanceof LumbinationAgent)
				|| (agent instanceof CropinationAgent)
				|| (agent instanceof ExcavationAgent)
				|| (agent instanceof PathanationAgent)
				|| (agent instanceof ShaftanationAgent);
	}
}
