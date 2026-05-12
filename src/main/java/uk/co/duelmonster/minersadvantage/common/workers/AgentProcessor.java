package uk.co.duelmonster.minersadvantage.common.workers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Legacy compatibility facade for worker lifecycle tracking.
 */
public final class AgentProcessor {
    public static final AgentProcessor INSTANCE = new AgentProcessor();

    public final List<Agent> currentAgents = new ArrayList<>();
    private final Map<UUID, Agent> perPlayerCurrentAgent = new LinkedHashMap<>();

    private AgentProcessor() {}

    public void fireAgentTicks(Object worldContext) {
        for (Agent agent : List.copyOf(currentAgents)) {
            agent.tick(worldContext);
        }
    }

    public void setCurrentAgent(UUID playerId, Agent agent) {
        if (playerId == null) {
            return;
        }
        if (agent == null) {
            perPlayerCurrentAgent.remove(playerId);
        } else {
            perPlayerCurrentAgent.put(playerId, agent);
            if (!currentAgents.contains(agent)) {
                currentAgents.add(agent);
            }
        }
    }

    public Agent getCurrentAgent(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        return perPlayerCurrentAgent.get(playerId);
    }

    public void resetAgentList() {
        currentAgents.clear();
        perPlayerCurrentAgent.clear();
    }
}
