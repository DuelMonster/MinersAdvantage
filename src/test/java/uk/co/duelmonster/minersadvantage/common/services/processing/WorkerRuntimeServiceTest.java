package uk.co.duelmonster.minersadvantage.common.services.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.services.core.PlayerStateService;

class WorkerRuntimeServiceTest {
    @Test
    void processesWorkAndCompletesWorkerWithDropFlush() {
        PlayerStateService playerStates = new PlayerStateService();
        WorkerRuntimeService service = new WorkerRuntimeService(playerStates);

        WorkerRuntimeService.WorkerHandle worker = service.startWorker(42L, FeatureId.EXCAVATION, 2, 4);
        AtomicInteger processed = new AtomicInteger();

        assertTrue(service.enqueueWork(worker.workerId(), processed::incrementAndGet));
        assertTrue(service.enqueueWork(worker.workerId(), processed::incrementAndGet));
        service.captureDrop(worker.workerId(), "item", 3);

        WorkerRuntimeService.WorkerTickResult tick = service.tick(false);

        assertEquals(2, tick.processedActions());
        assertEquals(1, tick.completedWorkers());
        assertEquals(1, tick.flushedDrops());
        assertEquals(2, processed.get());
        assertFalse(service.isWorkerActive(worker.workerId()));
    }

    @Test
    void pausesWorkerWhenHungerGuardIsActive() {
        PlayerStateService playerStates = new PlayerStateService();
        playerStates.updatePlayerState(7L, new PlayerStateService.PlayerState(7L, true, 0L, 0));
        WorkerRuntimeService service = new WorkerRuntimeService(playerStates);

        WorkerRuntimeService.WorkerHandle worker = service.startWorker(7L, FeatureId.SHAFTANATION, 3, 8);
        AtomicInteger processed = new AtomicInteger();
        assertTrue(service.enqueueWork(worker.workerId(), processed::incrementAndGet));

        WorkerRuntimeService.WorkerTickResult tick = service.tick(false);

        assertEquals(0, tick.processedActions());
        assertEquals(1, tick.pausedWorkers());
        assertTrue(service.isWorkerActive(worker.workerId()));
        assertEquals(0, processed.get());
    }

    @Test
    void abortAllForPlayerCancelsAndFlushesDrops() {
        PlayerStateService playerStates = new PlayerStateService();
        WorkerRuntimeService service = new WorkerRuntimeService(playerStates);

        WorkerRuntimeService.WorkerHandle first = service.startWorker(9L, FeatureId.VENTILATION, 1, 4);
        WorkerRuntimeService.WorkerHandle second = service.startWorker(9L, FeatureId.VEINATION, 1, 4);
        WorkerRuntimeService.WorkerHandle other = service.startWorker(10L, FeatureId.CROPINATION, 1, 4);

        service.captureDrop(first.workerId(), "item", 1);
        service.captureDrop(second.workerId(), "xp", 5);
        service.captureDrop(other.workerId(), "item", 2);

        List<?> flushed = service.abortAllForPlayer(9L);

        assertEquals(2, flushed.size());
        assertEquals(1, service.activeWorkerCount());
        assertTrue(service.isWorkerActive(other.workerId()));
        assertFalse(service.isWorkerActive(first.workerId()));
        assertFalse(service.isWorkerActive(second.workerId()));
    }
}
