package uk.co.duelmonster.minersadvantage.common.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.services.policy.PolicyCoreService;

class PolicyCoreServiceTest {
    @Test
    void validatesRangesAndOverrides() {
        PolicyCoreService service = new PolicyCoreService();
        assertEquals(5, service.clampRange(9, 1, 5));
        assertTrue(service.featureEnabled(true, true));
        assertFalse(service.featureEnabled(true, false));
    }
}
