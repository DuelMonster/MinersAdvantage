package uk.co.duelmonster.minersadvantage.common.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.services.processing.WorkerRuntimeService;

class AbortWorkersPacketFlowTest {
    @Test
    void abortPacketCancelsPlayerWorkersAndFlushesDrops() {
        MinersAdvantageCore core = new MinersAdvantageCore();
        WorkerRuntimeService runtime = core.workerRuntimeService();

        WorkerRuntimeService.WorkerHandle first = runtime.startWorker(101L, FeatureId.EXCAVATION, 2, 8);
        WorkerRuntimeService.WorkerHandle second = runtime.startWorker(101L, FeatureId.SHAFTANATION, 2, 8);
        WorkerRuntimeService.WorkerHandle other = runtime.startWorker(202L, FeatureId.CROPINATION, 2, 8);

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
    void registryExposesAbortPacketName() {
        assertEquals("AbortWorkersPacket", PacketRegistry.getPacketName(PacketRegistry.ABORT_WORKERS));
    }
}
