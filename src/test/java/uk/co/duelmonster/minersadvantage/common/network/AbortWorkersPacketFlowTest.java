package uk.co.duelmonster.minersadvantage.common.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.services.processing.WorkerRuntimeService;
import uk.co.duelmonster.minersadvantage.testutil.TestRuntimeAssumptions;

/**
 * AbortWorkersPacketFlowTest keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class AbortWorkersPacketFlowTest {
    @Test
    /**
     * a bo rt pa ck et ca nc el sp la ye rw or ke rs an df lu sh es dr op s exists so this path stays predictable and easier to debug when things get weird.
     */
    void abortPacketCancelsPlayerWorkersAndFlushesDrops() {
        Assumptions.assumeTrue(TestRuntimeAssumptions.canInitializeSoundEvents());
        MinersAdvantageCore core = new MinersAdvantageCore();
        WorkerRuntimeService runtime = core.workerRuntimeService();

        WorkerRuntimeService.WorkerHandle first = runtime.startWorker(101L, FeatureId.EXCAVATION, 2);
        WorkerRuntimeService.WorkerHandle second = runtime.startWorker(101L, FeatureId.SHAFTANATION, 2);
        WorkerRuntimeService.WorkerHandle other = runtime.startWorker(202L, FeatureId.CROPINATION, 2);

        runtime.captureDrop(first.workerId(), "item", 2);
        runtime.captureDrop(second.workerId(), "xp", 4);
        runtime.captureDrop(other.workerId(), "item", 1);

        WorkerRuntimeService.AbortResult result = core.handleAbortPacket(new AbortWorkersPacket(101L, "test"));

        assertEquals(2, result.cancelledWorkers());
        assertEquals(2, result.flushedDrops());
        assertEquals(1, runtime.activeWorkerCount());
        assertTrue(runtime.isWorkerActive(other.workerId()));
        assertEquals(2, runtime.drainSpawnQueue().size());
    }

    @Test
    /**
     * r eg is tr ye xp os es ab or tp ac ke tn am e exists so this path stays predictable and easier to debug when things get weird.
     */
    void registryExposesAbortPacketName() {
        assertEquals("AbortWorkersPacket", PacketRegistry.getPacketName(PacketRegistry.ABORT_WORKERS));
    }
}
