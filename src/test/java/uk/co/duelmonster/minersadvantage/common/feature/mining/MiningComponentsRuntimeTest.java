package uk.co.duelmonster.minersadvantage.common.feature.mining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.config.ExcavationConfig;
import uk.co.duelmonster.minersadvantage.common.config.ShaftanationConfig;
import uk.co.duelmonster.minersadvantage.common.config.VentilationConfig;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.orchestration.FeatureDispatchBus;
import uk.co.duelmonster.minersadvantage.common.orchestration.FeatureDispatchContext;

/**
 * MiningComponentsRuntimeTest keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class MiningComponentsRuntimeTest {
    @AfterEach
    /**
     * Clear shared dispatch context after each test.
     */
    void clearDispatchContext() {
        FeatureDispatchBus.clearContext();
    }

    @Test
    /**
     * Verify excavation plan respects configured height.
     */
    void excavationUsesConfiguredHeight() {
        ExcavationComponent component = new ExcavationComponent(new ExcavationConfig(true, 1, 5, 1, 5));
        component.register();
        component.enable();
        FeatureDispatchBus.setContext(new FeatureDispatchContext(FeatureId.EXCAVATION, 10, 64, 10, "minecraft:stone", "shovel", 1L));

        component.tick();

        assertEquals(5, component.lastPlan().size());
        assertTrue(component.lastPlan().stream().anyMatch(target -> target.y() < 64));
    }

    @Test
    /**
     * Verify shaftanation progress advances with valid stone context.
     */
    void shaftanationAdvancesDepthWhenStoneContextPresent() {
        ShaftanationComponent component = new ShaftanationComponent(new ShaftanationConfig(true, 9, 3));
        component.register();
        component.enable();
        FeatureDispatchBus.setContext(new FeatureDispatchContext(FeatureId.SHAFTANATION, 0, 40, 0, "minecraft:stone", "pickaxe", 2L));

        component.tick();
        component.tick();

        assertEquals(6, component.progressDepth());
        assertFalse(component.lastBatch().steps().isEmpty());
    }

    @Test
    /**
     * Verify ventilation progress is limited to intended cave-depth contexts.
     */
    void ventilationOnlyProgressesInCaveDepths() {
        VentilationComponent component = new VentilationComponent(new VentilationConfig(true, 2, 4, 1, 4));
        component.register();
        component.enable();

        FeatureDispatchBus.setContext(new FeatureDispatchContext(FeatureId.VENTILATION, 0, 40, 0, "minecraft:stone", "pickaxe", 3L));
        component.tick();
        assertTrue(component.progress() > 0);

        int progressed = component.progress();
        FeatureDispatchBus.setContext(new FeatureDispatchContext(FeatureId.VENTILATION, 0, 62, 0, "minecraft:stone", "pickaxe", 3L));
        component.tick();

        assertEquals(progressed, component.progress());
    }
}
