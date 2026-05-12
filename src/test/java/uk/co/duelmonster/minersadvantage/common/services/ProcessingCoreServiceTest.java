package uk.co.duelmonster.minersadvantage.common.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.services.processing.ProcessingCoreService;

/**
 * ProcessingCoreServiceTest keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class ProcessingCoreServiceTest {
    @Test
    void limitsQueueAndRespectsPerTickBudget() {
        ProcessingCoreService<Integer> service = new ProcessingCoreService<>(2, 3);

        assertTrue(service.offer(1));
        assertTrue(service.offer(2));
        assertTrue(service.offer(3));
        assertFalse(service.offer(4));

        List<Integer> consumed = new ArrayList<>();
        assertEquals(2, service.processTick(consumed::add));
        assertEquals(List.of(1, 2), consumed);
        assertEquals(1, service.size());
    }
}
