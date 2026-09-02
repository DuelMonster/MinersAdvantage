package uk.co.duelmonster.minersadvantage.agent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.config.MAConfig_Base;

class AgentManagerTest {
  @AfterEach
  void clearAgentManagerState() throws ReflectiveOperationException {
    clearMaps();
    clearRuntimeOverrides();
  }

  @Test
  void clearAgentsOfType_removesMatchingAgentsFromActiveAndPendingQueues() throws ReflectiveOperationException {
    AgentManager manager = AgentManager.get();
    Map<UUID, List<Agent>> active = agentsMap();
    Map<UUID, List<Agent>> pending = pendingMap();

    UUID activePlayer = UUID.randomUUID();
    UUID pendingPlayer = UUID.randomUUID();
    active.put(activePlayer, new ArrayList<>(List.of(allocateCaptivationAgent())));
    pending.put(pendingPlayer, new ArrayList<>(List.of(allocateCaptivationAgent())));

    manager.clearAgentsOfType(CaptivationAgent.class);

    assertFalse(active.containsKey(activePlayer));
    assertFalse(pending.containsKey(pendingPlayer));
  }

  @Test
  void clearAgentsOfType_preservesOtherAgentTypes() throws ReflectiveOperationException {
    AgentManager manager = AgentManager.get();
    Map<UUID, List<Agent>> active = agentsMap();
    UUID playerId = UUID.randomUUID();
    CaptivationAgent captivationAgent = allocateCaptivationAgent();
    Agent otherAgent = allocateSubstitutionAgent();
    active.put(playerId, new ArrayList<>(List.of(captivationAgent, otherAgent)));

    manager.clearAgentsOfType(SubstitutionAgent.class);

    assertTrue(active.containsKey(playerId));
    assertTrue(active.get(playerId).stream().allMatch(agent -> agent instanceof CaptivationAgent));
  }

  @Test
  void canQueueAgent_allowsUnlimitedAgentsWhenLimitEnforcementDisabled() {
    AgentManager manager = AgentManager.get();
    manager.setAgentLimitEnforced(SubstitutionAgent.class, false);
    manager.setMaxActiveAgents(SubstitutionAgent.class, 4);

    assertTrue(manager.canQueueAgent(SubstitutionAgent.class, 4, 0, MAConfig_Base.getGlobalConfig()));
    assertTrue(manager.canQueueAgent(SubstitutionAgent.class, 40, 0, MAConfig_Base.getGlobalConfig()));
  }

  @Test
  void canQueueAgent_respectsMaxActiveAgentsLimitWhenEnforced() {
    AgentManager manager = AgentManager.get();
    manager.setAgentLimitEnforced(SubstitutionAgent.class, true);
    manager.setMaxActiveAgents(SubstitutionAgent.class, 4);

    assertTrue(manager.canQueueAgent(SubstitutionAgent.class, 3, 0, MAConfig_Base.getGlobalConfig()));
    assertFalse(manager.canQueueAgent(SubstitutionAgent.class, 4, 0, MAConfig_Base.getGlobalConfig()));
  }

  private static void clearMaps() throws ReflectiveOperationException {
    agentsMap().clear();
    pendingMap().clear();
  }

  private static void clearRuntimeOverrides() throws ReflectiveOperationException {
    Field runtimeEnforcementField = AgentManager.class.getDeclaredField("runtimeAgentLimitEnforcement");
    runtimeEnforcementField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<Class<? extends Agent>, Boolean> runtimeEnforcement = (Map<Class<? extends Agent>, Boolean>) runtimeEnforcementField
        .get(AgentManager.get());
    runtimeEnforcement.clear();

    Field runtimeMaxActiveAgentsField = AgentManager.class.getDeclaredField("runtimeMaxActiveAgents");
    runtimeMaxActiveAgentsField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<Class<? extends Agent>, Integer> runtimeMaxActiveAgents = (Map<Class<? extends Agent>, Integer>) runtimeMaxActiveAgentsField
        .get(AgentManager.get());
    runtimeMaxActiveAgents.clear();
  }

  @SuppressWarnings("unchecked")
  private static Map<UUID, List<Agent>> agentsMap() throws ReflectiveOperationException {
    Field field = AgentManager.class.getDeclaredField("agents");
    field.setAccessible(true);
    return (Map<UUID, List<Agent>>) field.get(AgentManager.get());
  }

  @SuppressWarnings("unchecked")
  private static Map<UUID, List<Agent>> pendingMap() throws ReflectiveOperationException {
    Field field = AgentManager.class.getDeclaredField("pendingAdds");
    field.setAccessible(true);
    return (Map<UUID, List<Agent>>) field.get(AgentManager.get());
  }

  private static CaptivationAgent allocateCaptivationAgent() throws ReflectiveOperationException {
    return allocateAgent(CaptivationAgent.class);
  }

  private static SubstitutionAgent allocateSubstitutionAgent() throws ReflectiveOperationException {
    return allocateAgent(SubstitutionAgent.class);
  }

  @SuppressWarnings("unchecked")
  private static <T extends Agent> T allocateAgent(Class<T> agentType) throws ReflectiveOperationException {
    Object unsafe = unsafe();
    return (T) unsafe.getClass().getMethod("allocateInstance", Class.class).invoke(unsafe, agentType);
  }

  private static Object unsafe() throws ReflectiveOperationException {
    Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
    Field field = unsafeClass.getDeclaredField("theUnsafe");
    field.setAccessible(true);
    return field.get(null);
  }
}
