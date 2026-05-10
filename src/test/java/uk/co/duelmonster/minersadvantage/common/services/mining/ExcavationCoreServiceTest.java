package uk.co.duelmonster.minersadvantage.common.services.mining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ExcavationCoreServiceTest {
    @Test
    void identifiesValidBlocks() {
        ExcavationCoreService service = new ExcavationCoreService();
        assertTrue(service.isBlock("dirt"));
        assertFalse(service.isBlock("air"));
        assertFalse(service.isBlock(null));
    }

    @Test
    void detectsOres() {
        ExcavationCoreService service = new ExcavationCoreService();
        assertTrue(service.isOre("iron_ore"));
        assertFalse(service.isOre("dirt"));
    }

    @Test
    void estimatesExcavationVolume() {
        ExcavationCoreService service = new ExcavationCoreService();
        assertEquals(27, service.estimatedTurnsToExcavate(1, 1));
    }

    @Test
    void buildsOreVeinPlanWithVeinOperations() {
        ExcavationCoreService service = new ExcavationCoreService();
        List<ExcavationCoreService.ExcavationTarget> plan =
            service.buildPlan(10, 64, 10, "minecraft:iron_ore", 2, 2, 4);

        assertEquals(4, plan.size());
        assertTrue(plan.stream().allMatch(target -> target.operation().equals("vein")));
    }

    @Test
    void buildsAreaPlanWithinRequestedVerticalRadius() {
        ExcavationCoreService service = new ExcavationCoreService();
        List<ExcavationCoreService.ExcavationTarget> plan =
            service.buildPlan(0, 50, 0, "minecraft:stone", 1, 0, 5);

        assertEquals(5, plan.size());
        assertTrue(plan.stream().allMatch(target -> target.y() == 50));
        assertTrue(plan.stream().allMatch(target -> target.operation().equals("excavate")));
    }
}
