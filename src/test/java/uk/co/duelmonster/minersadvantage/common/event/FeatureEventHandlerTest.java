package uk.co.duelmonster.minersadvantage.common.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.orchestration.FeatureDispatchBus;
import uk.co.duelmonster.minersadvantage.common.orchestration.FeatureDispatchContext;

/**
 * FeatureEventHandlerTest keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class FeatureEventHandlerTest {
    @AfterEach
    void resetObserver() {
        FeatureEventHandler.resetDispatchObserverForTesting();
        FeatureDispatchBus.clearContext();
    }

    @Test
    void dispatchesContextAndClearsBus() {
        AtomicReference<FeatureDispatchContext> captured = new AtomicReference<>();
        FeatureEventHandler.setDispatchObserverForTesting(captured::set);

        FeatureEventHandler.onToolUse(FeatureId.EXCAVATION, 3, 4, 5, "stone", "pickaxe");

        assertFalse(FeatureDispatchBus.hasContext());
        assertEquals(FeatureId.EXCAVATION, captured.get().feature());
        assertEquals(3, captured.get().blockX());
        assertEquals(4, captured.get().blockY());
        assertEquals(5, captured.get().blockZ());
        assertEquals("stone", captured.get().blockId());
        assertEquals("pickaxe", captured.get().toolId());
        assertTrue(captured.get().playerId() == 0L);
    }

    @Test
    void clearsBusWhenObserverFails() {
        FeatureEventHandler.setDispatchObserverForTesting(context -> {
            throw new IllegalStateException(context.feature().name());
        });

        assertThrows(IllegalStateException.class, () ->
            FeatureEventHandler.onToolUse(FeatureId.VEINATION, 1, 2, 3, "ore", "pickaxe"));

        assertFalse(FeatureDispatchBus.hasContext());
    }
}



