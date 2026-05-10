package uk.co.duelmonster.minersadvantage.common.services.core;

import uk.co.duelmonster.minersadvantage.common.services.processing.WorkerRuntimeService;

public final class ServerTickOrchestrator {
    private final PlayerStateService playerStateService;
    private final WorkerRuntimeService workerRuntimeService;
    private boolean tpsGuardActive;
    private long tickCounter = 0;

    public ServerTickOrchestrator(PlayerStateService playerStateService) {
        this(playerStateService, new WorkerRuntimeService(playerStateService));
    }

    public ServerTickOrchestrator(PlayerStateService playerStateService, WorkerRuntimeService workerRuntimeService) {
        this.playerStateService = playerStateService;
        this.workerRuntimeService = workerRuntimeService;
    }

    public void onServerTick() {
        tickCounter++;
        playerStateService.tick();
        workerRuntimeService.tick(tpsGuardActive);
    }

    public long getTickCounter() {
        return tickCounter;
    }

    public WorkerRuntimeService workerRuntimeService() {
        return workerRuntimeService;
    }

    public void setTpsGuardActive(boolean tpsGuardActive) {
        this.tpsGuardActive = tpsGuardActive;
    }
}
