package uk.co.duelmonster.minersadvantage.agent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AgentTest {
    @Test
    void automatedBlockBreakContextIsNestedAndScoped() {
        assertFalse(Agent.isAutomatedBlockBreak());

        boolean nestedResult = Agent.duringAutomatedBlockBreak(() -> {
            assertTrue(Agent.isAutomatedBlockBreak());
            return Agent.duringAutomatedBlockBreak(Agent::isAutomatedBlockBreak);
        });

        assertTrue(nestedResult);
        assertFalse(Agent.isAutomatedBlockBreak());
    }

    @Test
    void automatedBlockBreakContextIsClearedWhenActionThrows() {
        assertThrows(
            IllegalStateException.class,
            () -> Agent.duringAutomatedBlockBreak(() -> {
                throw new IllegalStateException("test");
            }));

        assertFalse(Agent.isAutomatedBlockBreak());
    }
}
