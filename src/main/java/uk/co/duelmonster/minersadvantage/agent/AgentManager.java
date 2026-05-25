package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages all active agents for all players. Modernized for production wiring.
 */
public class AgentManager {
    private static final AgentManager INSTANCE = new AgentManager();
    private final Map<UUID, List<Agent>> agents = new ConcurrentHashMap<>();

    public static AgentManager get() { return INSTANCE; }

    public void addAgent(ServerPlayer player, Agent agent) {
        List<Agent> agentList = agents.computeIfAbsent(player.getUUID(), k -> new ArrayList<>());
        agentList.add(agent);
        LogUtils.logDebug("Queued {} for player={} activeAgents={}", agent.getClass().getSimpleName(), player.getScoreboardName(), agentList.size());
    }

    public void tick(Level world) {
        for (Map.Entry<UUID, List<Agent>> entry : agents.entrySet()) {
            List<Agent> agentList = entry.getValue();
            int before = agentList.size();
            agentList.removeIf(agent -> world.dimension().equals(agent.world.dimension()) && agent.tick());
            int removed = before - agentList.size();
            if (removed > 0) {
                LogUtils.logDebug("Processed {} completed agents in dimension={} remaining={}", removed, world.dimension(), agentList.size());
            }
        }
        agents.entrySet().removeIf(e -> e.getValue().isEmpty());
    }

    public void clearAgents(ServerPlayer player) {
        agents.remove(player.getUUID());
        LogUtils.logDebug("Cleared all agents for player={}", player.getScoreboardName());
    }

    public boolean hasAgentType(ServerPlayer player, Class<? extends Agent> agentType) {
        List<Agent> agentList = agents.get(player.getUUID());
        if (agentList == null || agentList.isEmpty()) {
            return false;
        }
        for (Agent agent : agentList) {
            if (agentType.isInstance(agent)) {
                return true;
            }
        }
        return false;
    }
}
