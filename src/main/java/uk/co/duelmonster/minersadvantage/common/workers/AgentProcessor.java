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

    /**
     * fireAgentTicks exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public void fireAgentTicks(Object worldContext) {
        for (Agent agent : List.copyOf(currentAgents)) {
            agent.tick(worldContext);
        }
    }

    /**
     * setCurrentAgent exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
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

    /**
     * getCurrentAgent exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public Agent getCurrentAgent(UUID playerId) {
        if (playerId == null) {
            return null;
        }
        return perPlayerCurrentAgent.get(playerId);
    }

    /**
     * resetAgentList exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public void resetAgentList() {
        currentAgents.clear();
        perPlayerCurrentAgent.clear();
    }
}
