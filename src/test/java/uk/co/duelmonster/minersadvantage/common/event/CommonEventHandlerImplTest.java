package uk.co.duelmonster.minersadvantage.common.event;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.orchestration.FeatureDispatchContext;

/**
 * CommonEventHandlerImplTest keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class CommonEventHandlerImplTest {
    @AfterEach
    void clearObserver() {
        FeatureEventHandler.resetDispatchObserverForTesting();
    }

    @Test
    void routesPickaxeStoneToShaftanation() {
        CommonEventHandlerImpl handler = new CommonEventHandlerImpl(new MinersAdvantageCore());
        AtomicReference<FeatureDispatchContext> captured = new AtomicReference<>();
        FeatureEventHandler.setDispatchObserverForTesting(captured::set);

        handler.onPickaxeUse(1, 2, 3, "minecraft:stone");

        assertEquals(FeatureId.SHAFTANATION, captured.get().feature());
    }

    @Test
    void routesHoeCropToCropination() {
        CommonEventHandlerImpl handler = new CommonEventHandlerImpl(new MinersAdvantageCore());
        AtomicReference<FeatureDispatchContext> captured = new AtomicReference<>();
        FeatureEventHandler.setDispatchObserverForTesting(captured::set);

        handler.onHoeUse(1, 2, 3, "minecraft:wheat_crop");

        assertEquals(FeatureId.CROPINATION, captured.get().feature());
    }
}

