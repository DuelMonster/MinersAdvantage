package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages all active agents for all players. Modernized for production wiring.
 */
public class AgentManager {
    private static final AgentManager INSTANCE = new AgentManager();
    private final Map<UUID, List<Agent>> agents = new ConcurrentHashMap<>();
    private final Map<UUID, List<Agent>> pendingAdds = new ConcurrentHashMap<>();
    private final AtomicBoolean ticking = new AtomicBoolean(false);

    public static AgentManager get() { return INSTANCE; }

    public void addAgent(ServerPlayer player, Agent agent) {
        Map<UUID, List<Agent>> target = ticking.get() ? pendingAdds : agents;
        List<Agent> agentList = target.computeIfAbsent(player.getUUID(), k -> new ArrayList<>());
        agentList.add(agent);
        int activeCount = agents.getOrDefault(player.getUUID(), List.of()).size();
        int pendingCount = pendingAdds.getOrDefault(player.getUUID(), List.of()).size();
        LogUtils.logDebug("Queued {} for player={} activeAgents={} pendingAgents={}", agent.getClass().getSimpleName(), player.getScoreboardName(), activeCount, pendingCount);
    }

    public void tick(Level world) {
        ticking.set(true);
        try {
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
        } finally {
            ticking.set(false);
            flushPendingAdds();
        }
    }

    public void clearAgents(ServerPlayer player) {
        agents.remove(player.getUUID());
        pendingAdds.remove(player.getUUID());
        LogUtils.logDebug("Cleared all agents for player={}", player.getScoreboardName());
    }

    public boolean hasAgentType(ServerPlayer player, Class<? extends Agent> agentType) {
        List<Agent> agentList = agents.get(player.getUUID());
        if (agentList != null && !agentList.isEmpty()) {
            for (Agent agent : agentList) {
                if (agentType.isInstance(agent)) {
                    return true;
                }
            }
        }

        List<Agent> pendingList = pendingAdds.get(player.getUUID());
        if (pendingList != null && !pendingList.isEmpty()) {
            for (Agent agent : pendingList) {
                if (agentType.isInstance(agent)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void flushPendingAdds() {
        if (pendingAdds.isEmpty()) {
            return;
        }

        for (Map.Entry<UUID, List<Agent>> entry : pendingAdds.entrySet()) {
            List<Agent> queued = entry.getValue();
            if (queued == null || queued.isEmpty()) {
                continue;
            }

            List<Agent> active = agents.computeIfAbsent(entry.getKey(), ignored -> new ArrayList<>());
            active.addAll(queued);
        }

        pendingAdds.clear();
    }
}
