package uk.co.duelmonster.minersadvantage.common.services.processing;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.services.core.PlayerStateService;
import uk.co.duelmonster.minersadvantage.common.services.drop.DropCoreService;

/**
 * WorkerRuntimeService keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class WorkerRuntimeService {
    /**
     * WorkerHandle keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record WorkerHandle(UUID workerId, long playerId, FeatureId feature) {}
    /**
     * AbortResult keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record AbortResult(int cancelledWorkers, int flushedDrops) {}
    /**
     * DropSpawn keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record DropSpawn(long playerId, String type, int amount) {}
    /**
     * DropInterceptionResult keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record DropInterceptionResult(boolean capturedForGather, boolean spawnNow) {}

    /**
     * WorkerTickResult keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record WorkerTickResult(int processedActions, int completedWorkers, int pausedWorkers, int flushedDrops) {}

    /**
     * ActiveWorker keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    private static final class ActiveWorker {
        private final WorkerHandle handle;
        private final ProcessingCoreService<Runnable> queue;
        private final DropCoreService drops;

        /**
         * ActiveWorker exists so this code path does one job clearly instead of spreading chaos across callers.
         * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
         */
        private ActiveWorker(WorkerHandle handle, int blocksPerTick, int blockLimit) {
            this.handle = handle;
            this.queue = new ProcessingCoreService<>(blocksPerTick, blockLimit);
            this.drops = new DropCoreService();
        }
    }

    private final Map<UUID, ActiveWorker> workers = new LinkedHashMap<>();
    private final List<DropSpawn> pendingSpawns = new ArrayList<>();
    private final PlayerStateService playerStateService;

    public WorkerRuntimeService(PlayerStateService playerStateService) {
        this.playerStateService = playerStateService;
    }

    /**
     * startWorker exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public WorkerHandle startWorker(long playerId, FeatureId feature, int blocksPerTick, int blockLimit) {
        WorkerHandle handle = new WorkerHandle(UUID.randomUUID(), playerId, feature);
        workers.put(handle.workerId(), new ActiveWorker(handle, blocksPerTick, blockLimit));
        return handle;
    }

    /**
     * enqueueWork exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean enqueueWork(UUID workerId, Runnable action) {
        ActiveWorker worker = workers.get(workerId);
        if (worker == null) {
            return false;
        }
        return worker.queue.offer(action);
    }

    /**
     * captureDrop exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void captureDrop(UUID workerId, String type, int amount) {
        ActiveWorker worker = workers.get(workerId);
        if (worker == null) {
            return;
        }
        worker.drops.capture(type, amount);
    }

    /**
     * interceptLiveDrop exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public DropInterceptionResult interceptLiveDrop(UUID workerId, String type, int amount, boolean gatherDrops) {
        ActiveWorker worker = workers.get(workerId);
        if (worker == null) {
            return new DropInterceptionResult(false, true);
        }

        if (gatherDrops) {
            worker.drops.capture(type, amount);
            return new DropInterceptionResult(true, false);
        }

        pendingSpawns.add(new DropSpawn(worker.handle.playerId(), type, amount));
        return new DropInterceptionResult(false, true);
    }

    /**
     * interceptLiveDropForPlayer exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public DropInterceptionResult interceptLiveDropForPlayer(long playerId, String type, int amount, boolean gatherDrops) {
        ActiveWorker worker = findFirstWorkerForPlayer(playerId);
        if (worker == null) {
            return new DropInterceptionResult(false, true);
        }

        if (gatherDrops) {
            worker.drops.capture(type, amount);
            return new DropInterceptionResult(true, false);
        }

        pendingSpawns.add(new DropSpawn(playerId, type, amount));
        return new DropInterceptionResult(false, true);
    }

    /**
     * abortWorker exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public List<DropCoreService.CapturedDrop> abortWorker(UUID workerId) {
        ActiveWorker worker = workers.remove(workerId);
        if (worker == null) {
            return List.of();
        }
        List<DropCoreService.CapturedDrop> flushed = worker.drops.flush();
        queueSpawnDrops(worker.handle.playerId(), flushed);
        return flushed;
    }

    /**
     * abortAllForPlayer exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
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

    /**
     * abortAllForPlayerWithStats exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public AbortResult abortAllForPlayerWithStats(long playerId) {
        int cancelledWorkers = 0;
        for (ActiveWorker worker : workers.values()) {
            if (worker.handle.playerId() == playerId) {
                cancelledWorkers++;
            }
        }
        List<DropCoreService.CapturedDrop> flushed = abortAllForPlayer(playerId);
        return new AbortResult(cancelledWorkers, flushed.size());
    }

    /**
     * tick exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
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
                List<DropCoreService.CapturedDrop> flushed = worker.drops.flush();
                flushedDrops += flushed.size();
                queueSpawnDrops(worker.handle.playerId(), flushed);
                completed.add(worker.handle.workerId());
                completedWorkers++;
            }
        }

        for (UUID workerId : completed) {
            workers.remove(workerId);
        }

        return new WorkerTickResult(processedActions, completedWorkers, pausedWorkers, flushedDrops);
    }

    /**
     * activeWorkerCount exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public int activeWorkerCount() {
        return workers.size();
    }

    /**
     * isWorkerActive exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean isWorkerActive(UUID workerId) {
        return workers.containsKey(workerId);
    }

    /**
     * drainSpawnQueue exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public List<DropSpawn> drainSpawnQueue() {
        List<DropSpawn> snapshot = List.copyOf(pendingSpawns);
        pendingSpawns.clear();
        return snapshot;
    }

    /**
     * queueSpawnDrops exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private void queueSpawnDrops(long playerId, List<DropCoreService.CapturedDrop> flushed) {
        for (DropCoreService.CapturedDrop drop : flushed) {
            pendingSpawns.add(new DropSpawn(playerId, drop.type(), drop.amount()));
        }
    }

    /**
     * findFirstWorkerForPlayer exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private ActiveWorker findFirstWorkerForPlayer(long playerId) {
        for (ActiveWorker worker : workers.values()) {
            if (worker.handle.playerId() == playerId) {
                return worker;
            }
        }
        return null;
    }
}

