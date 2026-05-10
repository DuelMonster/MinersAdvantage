package uk.co.duelmonster.minersadvantage.common.services.mining;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class VentilationCoreServiceTest {
    @Test
    void buildsVentilationBatchWithLadderCadence() {
        VentilationCoreService service = new VentilationCoreService();
        VentilationCoreService.VentilationBatch batch = service.buildBatch(1, 2, 1, 4);

        assertEquals(5, batch.newProgress());
        assertEquals(4, batch.steps().size());
        assertEquals(1, batch.ladderPlacements());
        assertTrue(batch.steps().stream().anyMatch(VentilationCoreService.VentilationStep::placeLadder));
    }
}
