package uk.co.duelmonster.minersadvantage.common.services.processing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.services.core.PlayerStateService;
import uk.co.duelmonster.minersadvantage.common.services.drop.DropCoreService;

public final class WorkerRuntimeService {
    public record WorkerHandle(UUID workerId, long playerId, FeatureId feature) {}

    public record WorkerTickResult(int processedActions, int completedWorkers, int pausedWorkers, int flushedDrops) {}

    private static final class ActiveWorker {
        private final WorkerHandle handle;
        private final ProcessingCoreService<Runnable> queue;
        private final DropCoreService drops;

        private ActiveWorker(WorkerHandle handle, int blocksPerTick, int blockLimit) {
            this.handle = handle;
            this.queue = new ProcessingCoreService<>(blocksPerTick, blockLimit);
            this.drops = new DropCoreService();
        }
    }

    private final Map<UUID, ActiveWorker> workers = new LinkedHashMap<>();
    private final PlayerStateService playerStateService;

    public WorkerRuntimeService(PlayerStateService playerStateService) {
        this.playerStateService = playerStateService;
    }

    public WorkerHandle startWorker(long playerId, FeatureId feature, int blocksPerTick, int blockLimit) {
        WorkerHandle handle = new WorkerHandle(UUID.randomUUID(), playerId, feature);
        workers.put(handle.workerId(), new ActiveWorker(handle, blocksPerTick, blockLimit));
        return handle;
    }

    public boolean enqueueWork(UUID workerId, Runnable action) {
        ActiveWorker worker = workers.get(workerId);
        if (worker == null) {
            return false;
        }
        return worker.queue.offer(action);
    }

    public void captureDrop(UUID workerId, String type, int amount) {
        ActiveWorker worker = workers.get(workerId);
        if (worker == null) {
            return;
        }
        worker.drops.capture(type, amount);
    }

    public List<DropCoreService.CapturedDrop> abortWorker(UUID workerId) {
        ActiveWorker worker = workers.remove(workerId);
        if (worker == null) {
            return List.of();
        }
        return worker.drops.flush();
    }

    public List<DropCoreService.CapturedDrop> abortAllForPlayer(long playerId) {
        List<UUID> toAbort = new ArrayList<>();
        for (ActiveWorker worker : workers.values()) {
            if (worker.handle.playerId() == playerId) {
                toAbort.add(worker.handle.workerId());
            }
        }

        List<DropCoreService.CapturedDrop> flushed = new ArrayList<>();
        for (UUID workerId : toAbort) {
            flushed.addAll(abortWorker(workerId));
        }
        return flushed;
    }

    public WorkerTickResult tick(boolean tpsGuardActive) {
        int processedActions = 0;
        int completedWorkers = 0;
        int pausedWorkers = 0;
        int flushedDrops = 0;
        List<UUID> completed = new ArrayList<>();

        for (ActiveWorker worker : workers.values()) {
            if (tpsGuardActive || playerStateService.getPlayerState(worker.handle.playerId()).hungerGuardActive()) {
                pausedWorkers++;
                continue;
            }

            processedActions += worker.queue.processTick(Runnable::run);

            if (worker.queue.size() == 0) {
                flushedDrops += worker.drops.flush().size();
                completed.add(worker.handle.workerId());
                completedWorkers++;
            }
        }

        for (UUID workerId : completed) {
            workers.remove(workerId);
        }

        return new WorkerTickResult(processedActions, completedWorkers, pausedWorkers, flushedDrops);
    }

    public int activeWorkerCount() {
        return workers.size();
    }

    public boolean isWorkerActive(UUID workerId) {
        return workers.containsKey(workerId);
    }
}
