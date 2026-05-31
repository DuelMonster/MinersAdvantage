package uk.co.duelmonster.minersadvantage.common.feature.farming;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.config.CaptivationConfig;
import uk.co.duelmonster.minersadvantage.common.config.CropinationConfig;
import uk.co.duelmonster.minersadvantage.common.config.CultivationConfig;
import uk.co.duelmonster.minersadvantage.common.config.LumbinationConfig;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.feature.captivation.CaptivationComponent;
import uk.co.duelmonster.minersadvantage.common.feature.harvest.LumbinationComponent;
import uk.co.duelmonster.minersadvantage.common.orchestration.FeatureDispatchBus;
import uk.co.duelmonster.minersadvantage.common.orchestration.FeatureDispatchContext;

/**
 * FarmingHarvestComponentsRuntimeTest keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class FarmingHarvestComponentsRuntimeTest {
    @AfterEach
    /**
     * Clear shared dispatch context after each test.
     */
    void clearDispatchContext() {
        FeatureDispatchBus.clearContext();
    }

    @Test
    /**
     * Verify cropination harvests and replants mature crops.
     */
    void cropinationProducesHarvestActionForMatureCrop() {
        CropinationComponent component = new CropinationComponent(new CropinationConfig(true, true));
        component.register();
        component.enable();
        FeatureDispatchBus.setContext(new FeatureDispatchContext(FeatureId.CROPINATION, 1, 64, 1, "minecraft:wheat_crop_mature", "hoe", 1L));

        component.tick();

        assertTrue(component.lastAction().shouldHarvest());
        assertTrue(component.lastAction().shouldReplant());
    }

    @Test
    /**
     * Verify cultivation creates soil plan using configured radius.
     */
    void cultivationBuildsPlanForSoilTargets() {
        CultivationComponent component = new CultivationComponent(new CultivationConfig(true, 3));
        component.register();
        component.enable();
        FeatureDispatchBus.setContext(new FeatureDispatchContext(FeatureId.CULTIVATION, 5, 64, 5, "minecraft:dirt", "hoe", 2L));

        component.tick();

        assertEquals(3, component.lastPlan().size());
    }

    @Test
    /**
     * Verify lumbination plan includes logs and sapling replant intent.
     */
    void lumbinationBuildsTreePlanWhenLogTriggered() {
        LumbinationComponent component = new LumbinationComponent(new LumbinationConfig(true, 5, 4, 4));
        component.register();
        component.enable();
        FeatureDispatchBus.setContext(new FeatureDispatchContext(FeatureId.LUMBINATION, 0, 70, 0, "minecraft:oak_log", "axe", 3L));

        component.tick();

        assertTrue(component.lastPlan().logsToHarvest() > 0);
        assertTrue(component.lastPlan().replantSapling());
    }

    @Test
    /**
     * Verify captivation decision allows item capture in valid context.
     */
    void captivationEvaluatesItemCaptureDecision() {
        CaptivationComponent component = new CaptivationComponent(new CaptivationConfig(true, false, 8, 4, false, false));
        component.register();
        component.enable();
        FeatureDispatchBus.setContext(new FeatureDispatchContext(FeatureId.CAPTIVATION, 4, 64, 4, "item:minecraft:diamond", "hand", 4L));

        component.tick();

        assertTrue(component.lastDecision().canCapture());
        assertFalse(component.lastDecision().blockedByGui());
    }
}
