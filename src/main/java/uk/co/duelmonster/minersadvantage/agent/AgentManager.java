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
 * Central scheduler for active and pending agents per player.
 * It keeps tick-time mutations safe by buffering adds while iteration is in progress.
 */
public class AgentManager {
    private static final AgentManager INSTANCE = new AgentManager();
    private final Map<UUID, List<Agent>> agents = new ConcurrentHashMap<>();
    private final Map<UUID, List<Agent>> pendingAdds = new ConcurrentHashMap<>();
    private final AtomicBoolean ticking = new AtomicBoolean(false);

    /**
     * Singleton accessor because agent scheduling is global runtime state.
     */
    public static AgentManager get() { return INSTANCE; }

    /**
     * Queue agent immediately unless we are mid-tick, in which case stage it for post-loop merge.
     */
    public void addAgent(ServerPlayer player, Agent agent) {
        Map<UUID, List<Agent>> target = ticking.get() ? pendingAdds : agents;
        List<Agent> agentList = target.computeIfAbsent(player.getUUID(), k -> new ArrayList<>());
        agentList.add(agent);
        if (!(agent instanceof CaptivationAgent)) {
            int activeCount = agents.getOrDefault(player.getUUID(), List.of()).size();
            int pendingCount = pendingAdds.getOrDefault(player.getUUID(), List.of()).size();
            LogUtils.logDebug("Queued {} for player={} activeAgents={} pendingAgents={}", agent.getClass().getSimpleName(), player.getScoreboardName(), activeCount, pendingCount);
        }
    }

    /**
     * Tick all active agents for the specified world dimension and retire completed ones.
     */
    public void tick(Level world) {
        ticking.set(true);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        try {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (Map.Entry<UUID, List<Agent>> entry : agents.entrySet()) {
                List<Agent> agentList = entry.getValue();
                int removed = 0;
                int removedNonCaptivation = 0;
                for (int i = agentList.size() - 1; i >= 0; i--) {
                    Agent agent = agentList.get(i);
                    if (!world.dimension().equals(agent.world.dimension()) || !agent.tick()) {
                        continue;
                    }

                    removed++;
                    if (!(agent instanceof CaptivationAgent)) {
                        removedNonCaptivation++;
                    }
                    agentList.remove(i);
                }

                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (removed > 0 && removedNonCaptivation > 0) {
                    LogUtils.logDebug("Processed {} completed agents in dimension={} remaining={}", removed, world.dimension(), agentList.size());
                }
            }
            agents.entrySet().removeIf(e -> e.getValue().isEmpty());
        } finally {
            ticking.set(false);
            flushPendingAdds();
        }
    }

    /**
     * Clear all active and pending agents for one player instance.
     */
    public void clearAgents(ServerPlayer player) {
        clearAgents(player.getUUID(), player.getScoreboardName());
    }

    /**
     * Clear all active and pending agents by long player id.
     */
    public void clearAgents(long playerId) {
        UUID playerUuid = findPlayerUuid(playerId);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (playerUuid == null) {
            return;
        }
        clearAgents(playerUuid, Long.toString(playerId));
    }

    /**
     * Internal clear helper used by both clear entry points.
     */
    private void clearAgents(UUID playerUuid, String playerLabel) {
        agents.remove(playerUuid);
        pendingAdds.remove(playerUuid);
        LogUtils.logDebug("Cleared all agents for player={}", playerLabel);
    }

    /**
     * Resolve UUID by least-significant-bits player id from active and pending maps.
     */
    private UUID findPlayerUuid(long playerId) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (UUID playerUuid : agents.keySet()) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (playerUuid.getLeastSignificantBits() == playerId) {
                return playerUuid;
            }
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (UUID playerUuid : pendingAdds.keySet()) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (playerUuid.getLeastSignificantBits() == playerId) {
                return playerUuid;
            }
        }

        return null;
    }

    /**
     * Check both active and pending queues for an agent type so duplicate fan-out workers can be avoided.
     */
    public boolean hasAgentType(ServerPlayer player, Class<? extends Agent> agentType) {
        List<Agent> agentList = agents.get(player.getUUID());
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (agentList != null && !agentList.isEmpty()) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (Agent agent : agentList) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (agentType.isInstance(agent)) {
                    return true;
                }
            }
        }

        List<Agent> pendingList = pendingAdds.get(player.getUUID());
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (pendingList != null && !pendingList.isEmpty()) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (Agent agent : pendingList) {
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (agentType.isInstance(agent)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Merge queued additions collected during ticking into active map.
     */
    private void flushPendingAdds() {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (pendingAdds.isEmpty()) {
            return;
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (Map.Entry<UUID, List<Agent>> entry : pendingAdds.entrySet()) {
            List<Agent> queued = entry.getValue();
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (queued == null || queued.isEmpty()) {
                continue;
            }

            List<Agent> active = agents.computeIfAbsent(entry.getKey(), ignored -> new ArrayList<>());
            active.addAll(queued);
        }

        pendingAdds.clear();
    }
}
