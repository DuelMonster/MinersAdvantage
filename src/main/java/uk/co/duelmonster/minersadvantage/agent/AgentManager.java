package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import uk.co.duelmonster.minersadvantage.common.config.MAConfig_Base;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
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
  private static final int DEFAULT_MAX_ACTIVE_AGENTS = 4;
  private final Map<UUID, List<Agent>> agents = new ConcurrentHashMap<>();
  private final Map<UUID, List<Agent>> pendingAdds = new ConcurrentHashMap<>();
  private final Map<Class<? extends Agent>, Boolean> runtimeAgentTypeDeduplication = new ConcurrentHashMap<>();
  private final Map<Class<? extends Agent>, Integer> runtimeMaxActiveAgents = new ConcurrentHashMap<>();
  private final AtomicBoolean ticking = new AtomicBoolean(false);

  /**
   * Singleton accessor because agent scheduling is global runtime state.
   */
  public static AgentManager get() {
    return INSTANCE;
  }

  /**
   * Queue agent immediately unless we are mid-tick, in which case stage it for post-loop merge.
   */
  public void addAgent(ServerPlayer player, Agent agent) {
    if (player == null || agent == null) {
      return;
    }

    SyncedClientConfig config = MAConfig_Base.getPlayerConfig(player.getUUID());
    int activeTypeCount = countAgentsOfType(agent.getClass(), agents);
    int pendingTypeCount = countAgentsOfType(agent.getClass(), pendingAdds);
    if (isAgentTypeDeduplicationEnabled(agent.getClass(), config) && hasAgentType(player, agent.getClass())) {
      LogUtils.logDebug(
          "Skipped queueing {} for player={} reason=type-deduped activeTypeCount={} maxActiveAgents={} dedupeEnabled=true",
          agent.getClass().getSimpleName(), player.getScoreboardName(), activeTypeCount + pendingTypeCount,
          effectiveMaxActiveAgents(agent.getClass(), config));
      return;
    }

    if (!canQueueAgent(agent.getClass(), activeTypeCount, pendingTypeCount, config)) {
      LogUtils.logDebug(
          "Skipped queueing {} for player={} reason=max-active-agent-limit reached activeTypeCount={} maxActiveAgents={} dedupeEnabled={}",
          agent.getClass().getSimpleName(),
          player.getScoreboardName(),
          activeTypeCount + pendingTypeCount,
          effectiveMaxActiveAgents(agent.getClass(), config),
          isAgentTypeDeduplicationEnabled(agent.getClass(), config));
      return;
    }

    Map<UUID, List<Agent>> target = ticking.get() ? pendingAdds : agents;
    List<Agent> agentList = target.computeIfAbsent(player.getUUID(), k -> new ArrayList<>());
    agentList.add(agent);
    if (!(agent instanceof CaptivationAgent)) {
      int activeCount = agents.getOrDefault(player.getUUID(), List.of()).size();
      int pendingCount = pendingAdds.getOrDefault(player.getUUID(), List.of()).size();
      LogUtils.logDebug("Queued {} for player={} activeAgents={} pendingAgents={}", agent.getClass().getSimpleName(),
          player.getScoreboardName(), activeCount, pendingCount);
    }
  }

  /**
   * Tick all active agents for the specified world dimension and retire completed ones.
   */
  public void tick(Level world) {
    ticking.set(true);
    try {
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

        if (removed > 0 && removedNonCaptivation > 0) {
          LogUtils.logDebug("Processed {} completed agents in dimension={} remaining={}", removed, world.dimension(),
              agentList.size());
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
    if (playerUuid == null) {
      return;
    }
    clearAgents(playerUuid, Long.toString(playerId));
  }

  /**
   * Clear all active and pending agents of one type across all players.
   */
  public void clearAgentsOfType(Class<? extends Agent> agentType) {
    if (agentType == null) {
      return;
    }

    agents.values().forEach(agentList -> agentList.removeIf(agentType::isInstance));
    pendingAdds.values().forEach(agentList -> agentList.removeIf(agentType::isInstance));
    agents.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    pendingAdds.entrySet().removeIf(entry -> entry.getValue().isEmpty());
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
    for (UUID playerUuid : agents.keySet()) {
      if (playerUuid.getLeastSignificantBits() == playerId) {
        return playerUuid;
      }
    }

    for (UUID playerUuid : pendingAdds.keySet()) {
      if (playerUuid.getLeastSignificantBits() == playerId) {
        return playerUuid;
      }
    }

    return null;
  }

  /**
   * Toggle deduplication for one runtime agent type. Disabled means multiple agents of that type may coexist.
   */
  public void setAgentTypeDeduplication(Class<? extends Agent> agentType, boolean enabled) {
    if (agentType == null) {
      return;
    }
    if (enabled) {
      runtimeAgentTypeDeduplication.remove(agentType);
    } else {
      runtimeAgentTypeDeduplication.put(agentType, false);
    }
  }

  /**
   * Set the maximum number of active agent instances allowed for one runtime agent type.
   */
  public void setMaxActiveAgents(Class<? extends Agent> agentType, int maxActiveAgents) {
    if (agentType == null) {
      return;
    }
    this.runtimeMaxActiveAgents.put(agentType, Math.max(1, maxActiveAgents));
  }

  /**
   * Returns true if the local runtime config and per-type cap allow taking another queue entry.
   */
  public boolean canQueueAgent(Class<? extends Agent> agentType, int activeCount, int pendingCount,
      SyncedClientConfig config) {
    if (agentType == null) {
      return false;
    }
    if (activeCount < 0) {
      activeCount = 0;
    }
    if (pendingCount < 0) {
      pendingCount = 0;
    }

    int activeTypeCount = activeCount + pendingCount;
    int maxAgents = effectiveMaxActiveAgents(agentType, config);
    if (activeTypeCount >= maxAgents) {
      return false;
    }

    if (!isAgentTypeDeduplicationEnabled(agentType, config)) {
      return true;
    }

    return activeTypeCount == 0;
  }

  /**
   * Check both active and pending queues for an agent type so duplicate fan-out workers can be avoided.
   */
  public boolean hasAgentType(ServerPlayer player, Class<? extends Agent> agentType) {
    if (player == null || agentType == null) {
      return false;
    }
    SyncedClientConfig config = MAConfig_Base.getPlayerConfig(player.getUUID());
    if (!isAgentTypeDeduplicationEnabled(agentType, config)) {
      return false;
    }

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

  private boolean isAgentTypeDeduplicationEnabled(Class<? extends Agent> agentType, SyncedClientConfig config) {
    if (agentType == null) {
      return true;
    }
    Boolean runtimeOverride = runtimeAgentTypeDeduplication.get(agentType);
    if (runtimeOverride != null) {
      return runtimeOverride;
    }
    SyncedClientConfig effectiveConfig = config == null ? MAConfig_Base.getGlobalConfig() : config;
    return effectiveConfig.isAgentTypeDeduplicationEnabled(agentType);
  }

  private int effectiveMaxActiveAgents(Class<? extends Agent> agentType, SyncedClientConfig config) {
    SyncedClientConfig effectiveConfig = config == null ? MAConfig_Base.getGlobalConfig() : config;
    if (agentType == null) {
      return DEFAULT_MAX_ACTIVE_AGENTS;
    }
    Integer runtimeOverride = runtimeMaxActiveAgents.get(agentType);
    if (runtimeOverride != null) {
      return runtimeOverride;
    }
    return Math.max(1, effectiveConfig.maxActiveAgentsForType(agentType));
  }

  private static int countAgentsOfType(Class<? extends Agent> agentType, Map<UUID, List<Agent>> map) {
    if (agentType == null || map == null || map.isEmpty()) {
      return 0;
    }

    int count = 0;
    for (List<Agent> agentList : map.values()) {
      if (agentList == null || agentList.isEmpty()) {
        continue;
      }
      for (Agent agent : agentList) {
        if (agentType.isInstance(agent)) {
          count++;
        }
      }
    }
    return count;
  }

  private int totalActiveAgents() {
    int total = 0;
    for (List<Agent> agentList : agents.values()) {
      if (agentList != null) {
        total += agentList.size();
      }
    }
    return total;
  }

  private int totalPendingAgents() {
    int total = 0;
    for (List<Agent> agentList : pendingAdds.values()) {
      if (agentList != null) {
        total += agentList.size();
      }
    }
    return total;
  }

  /**
   * Return true when player has any active/pending worker beyond passive background helpers.
   */
  public boolean hasBlockingAutomationAgent(ServerPlayer player) {
    if (player == null) {
      return false;
    }

    List<Agent> active = agents.get(player.getUUID());
    if (containsBlockingAgent(active)) {
      return true;
    }

    List<Agent> pending = pendingAdds.get(player.getUUID());
    return containsBlockingAgent(pending);
  }

  private static boolean containsBlockingAgent(List<Agent> agentList) {
    if (agentList == null || agentList.isEmpty()) {
      return false;
    }

    for (Agent agent : agentList) {
      if (agent instanceof CaptivationAgent || agent instanceof SubstitutionAgent) {
        continue;
      }
      return true;
    }
    return false;
  }

  /**
   * Merge queued additions collected during ticking into active map.
   */
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
