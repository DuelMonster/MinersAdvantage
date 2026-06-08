package uk.co.duelmonster.minersadvantage.common.services.mining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.testutil.TestRuntimeAssumptions;

/**
 * ExcavationCoreServiceTest keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class ExcavationCoreServiceTest {
    private static void assumeBlockRegistries() {
        Assumptions.assumeTrue(TestRuntimeAssumptions.canInitializeBlockRegistries());
    }

    @Test
    /**
     * Verify basic block validity checks.
     */
    void identifiesValidBlocks() {
        ExcavationCoreService service = new ExcavationCoreService();
        assertTrue(service.isBlock("dirt"));
        assertFalse(service.isBlock("air"));
        assertFalse(service.isBlock(null));
    }

    @Test
    /**
     * Verify ore detection helper behavior.
     */
    void detectsOres() {
        assumeBlockRegistries();
        ExcavationCoreService service = new ExcavationCoreService();
        assertTrue(service.isOre("iron_ore"));
        assertFalse(service.isOre("dirt"));
    }

    @Test
    /**
     * Verify volume estimate calculation.
     */
    void estimatesExcavationVolume() {
        ExcavationCoreService service = new ExcavationCoreService();
        assertEquals(27, service.estimatedTurnsToExcavate(3, 3, 3));
    }

    @Test
    /**
     * Verify ore context produces vein operation plan.
     */
    void buildsOreVeinPlanWithVeinOperations() {
        assumeBlockRegistries();
        ExcavationCoreService service = new ExcavationCoreService();
        List<ExcavationCoreService.ExcavationTarget> plan =
            service.buildPlan(10, 64, 10, "minecraft:iron_ore", 5, 5, 3, 4);

        assertEquals(4, plan.size());
        assertTrue(plan.stream().allMatch(target -> target.operation().equals("vein")));
    }

    @Test
    /**
     * Verify area context plan stays within requested vertical layer.
     */
    void buildsAreaPlanWithinRequestedHeight() {
        assumeBlockRegistries();
        ExcavationCoreService service = new ExcavationCoreService();
        List<ExcavationCoreService.ExcavationTarget> plan =
            service.buildPlan(0, 50, 0, "minecraft:stone", 3, 1, 3, 5);

        assertEquals(5, plan.size());
        assertTrue(plan.stream().allMatch(target -> target.y() == 50));
        assertTrue(plan.stream().allMatch(target -> target.operation().equals("excavate")));
    }
}
