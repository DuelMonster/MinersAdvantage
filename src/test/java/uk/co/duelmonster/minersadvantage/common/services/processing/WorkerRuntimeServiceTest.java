package uk.co.duelmonster.minersadvantage.common.services.processing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.services.core.PlayerStateService;

/**
 * WorkerRuntimeServiceTest keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class WorkerRuntimeServiceTest {
    @Test
    /**
     * Verify queued work completes and captured drops flush on worker completion.
     */
    void processesWorkAndCompletesWorkerWithDropFlush() {
        PlayerStateService playerStates = new PlayerStateService();
        WorkerRuntimeService service = new WorkerRuntimeService(playerStates);

        WorkerRuntimeService.WorkerHandle worker = service.startWorker(42L, FeatureId.EXCAVATION, 2);
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
        assertEquals(1, service.drainSpawnQueue().size());
    }

    @Test
    /**
     * Verify hunger guard pauses worker processing.
     */
    void pausesWorkerWhenHungerGuardIsActive() {
        PlayerStateService playerStates = new PlayerStateService();
        playerStates.updatePlayerState(7L, new PlayerStateService.PlayerState(7L, true, 0L, 0, false, false));
        WorkerRuntimeService service = new WorkerRuntimeService(playerStates);

        WorkerRuntimeService.WorkerHandle worker = service.startWorker(7L, FeatureId.SHAFTANATION, 3);
        AtomicInteger processed = new AtomicInteger();
        assertTrue(service.enqueueWork(worker.workerId(), processed::incrementAndGet));

        WorkerRuntimeService.WorkerTickResult tick = service.tick(false);

        assertEquals(0, tick.processedActions());
        assertEquals(1, tick.pausedWorkers());
        assertTrue(service.isWorkerActive(worker.workerId()));
        assertEquals(0, processed.get());
    }

    @Test
    /**
     * Verify aborting player workers flushes only that player's worker drops.
     */
    void abortAllForPlayerCancelsAndFlushesDrops() {
        PlayerStateService playerStates = new PlayerStateService();
        WorkerRuntimeService service = new WorkerRuntimeService(playerStates);

        WorkerRuntimeService.WorkerHandle first = service.startWorker(9L, FeatureId.VENTILATION, 1);
        WorkerRuntimeService.WorkerHandle second = service.startWorker(9L, FeatureId.VEINATION, 1);
        WorkerRuntimeService.WorkerHandle other = service.startWorker(10L, FeatureId.CROPINATION, 1);

        service.captureDrop(first.workerId(), "item", 1);
        service.captureDrop(second.workerId(), "xp", 5);
        service.captureDrop(other.workerId(), "item", 2);

        List<?> flushed = service.abortAllForPlayer(9L);

        assertEquals(2, flushed.size());
        assertEquals(1, service.activeWorkerCount());
        assertTrue(service.isWorkerActive(other.workerId()));
        assertFalse(service.isWorkerActive(first.workerId()));
        assertFalse(service.isWorkerActive(second.workerId()));
        assertEquals(2, service.drainSpawnQueue().size());
    }

    @Test
    /**
     * Verify live-drop interception honors gather-drops policy.
     */
    void interceptLiveDropHonorsGatherDropsPolicy() {
        PlayerStateService playerStates = new PlayerStateService();
        WorkerRuntimeService service = new WorkerRuntimeService(playerStates);

        WorkerRuntimeService.WorkerHandle gathered = service.startWorker(50L, FeatureId.EXCAVATION, 1);
        WorkerRuntimeService.WorkerHandle immediate = service.startWorker(51L, FeatureId.SHAFTANATION, 1);

        WorkerRuntimeService.DropInterceptionResult gatheredResult =
            service.interceptLiveDrop(gathered.workerId(), "item", 2, true);
        WorkerRuntimeService.DropInterceptionResult immediateResult =
            service.interceptLiveDrop(immediate.workerId(), "xp", 5, false);

        assertTrue(gatheredResult.capturedForGather());
        assertFalse(gatheredResult.spawnNow());
        assertFalse(immediateResult.capturedForGather());
        assertTrue(immediateResult.spawnNow());

        assertEquals(1, service.drainSpawnQueue().size());

        service.enqueueWork(gathered.workerId(), () -> { });
        service.tick(false);
        assertEquals(1, service.drainSpawnQueue().size());
    }

    @Test
    /**
     * Verify player-level interception routes to active worker.
     */
    void interceptLiveDropForPlayerUsesActiveWorker() {
        PlayerStateService playerStates = new PlayerStateService();
        WorkerRuntimeService service = new WorkerRuntimeService(playerStates);

        WorkerRuntimeService.WorkerHandle worker = service.startWorker(60L, FeatureId.EXCAVATION, 1);
        WorkerRuntimeService.DropInterceptionResult result =
            service.interceptLiveDropForPlayer(60L, "item:minecraft:stone", 1, true);

        assertTrue(result.capturedForGather());
        assertFalse(result.spawnNow());

        service.enqueueWork(worker.workerId(), () -> { });
        service.tick(false);
        assertEquals(1, service.drainSpawnQueue().size());
    }
}
