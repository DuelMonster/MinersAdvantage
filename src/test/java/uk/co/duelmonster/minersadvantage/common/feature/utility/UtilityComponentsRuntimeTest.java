package uk.co.duelmonster.minersadvantage.common.feature.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.config.IlluminationConfig;
import uk.co.duelmonster.minersadvantage.common.config.PathanationConfig;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig;
import uk.co.duelmonster.minersadvantage.common.config.VeinationConfig;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.orchestration.FeatureDispatchBus;
import uk.co.duelmonster.minersadvantage.common.orchestration.FeatureDispatchContext;

class UtilityComponentsRuntimeTest {
    @AfterEach
    void clearDispatchContext() {
        FeatureDispatchBus.clearContext();
    }

    @Test
    void illuminationProducesDecisionFromContext() {
        IlluminationComponent component = new IlluminationComponent(new IlluminationConfig(true, 2, 1));
        component.register();
        component.enable();
        FeatureDispatchBus.setContext(new FeatureDispatchContext(FeatureId.ILLUMINATION, 10, 34, 11, "minecraft:stone", "pickaxe", 1L));

        component.tick();

        assertNotNull(component.lastDecision());
        assertTrue(component.lastDecision().placeNow());
    }

    @Test
    void pathanationBuildsPathWithinConfiguredRange() {
        PathanationComponent component = new PathanationComponent(new PathanationConfig(true, 4));
        component.register();
        component.enable();
        FeatureDispatchBus.setContext(new FeatureDispatchContext(FeatureId.PATHANATION, 5, 64, 5, "minecraft:dirt", "shovel", 2L));

        component.tick();

        assertEquals(4, component.lastPath().size());
    }

    @Test
    void veinationOnlyBuildsPlanForOreBlocks() {
        VeinationComponent component = new VeinationComponent(new VeinationConfig(true, 3));
        component.register();
        component.enable();

        FeatureDispatchBus.setContext(new FeatureDispatchContext(FeatureId.VEINATION, 0, 40, 0, "minecraft:diamond_ore", "pickaxe", 3L));
        component.tick();
        assertFalse(component.lastVein().isEmpty());

        FeatureDispatchBus.setContext(new FeatureDispatchContext(FeatureId.VEINATION, 0, 40, 0, "minecraft:stone", "pickaxe", 3L));
        component.tick();
        assertTrue(component.lastVein().isEmpty());
    }

    @Test
    void substitutionChoosesSilkToolInOreContextWhenConfigured() {
        SubstitutionComponent component = new SubstitutionComponent(new SubstitutionConfig(true, false, true));
        component.register();
        component.enable();
        FeatureDispatchBus.setContext(new FeatureDispatchContext(FeatureId.SUBSTITUTION, 1, 30, 1, "minecraft:iron_ore", "pickaxe", 4L));

        component.tick();

        assertEquals("silk_pick", component.lastSelectedToolId());
    }
}
