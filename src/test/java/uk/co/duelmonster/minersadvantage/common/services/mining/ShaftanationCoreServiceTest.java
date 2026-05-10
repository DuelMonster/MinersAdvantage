package uk.co.duelmonster.minersadvantage.common.services.mining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ShaftanationCoreServiceTest {
    @Test
    void buildsDepthBatchWithTorchCadence() {
        ShaftanationCoreService service = new ShaftanationCoreService();
        ShaftanationCoreService.ShaftBatch batch = service.buildBatch(3, 10, 4);

        assertEquals(7, batch.newDepth());
        assertEquals(4, batch.steps().size());
        assertEquals(1, batch.torchPlacements());
        assertTrue(batch.steps().stream().anyMatch(ShaftanationCoreService.ShaftStep::placeTorch));
    }
}
