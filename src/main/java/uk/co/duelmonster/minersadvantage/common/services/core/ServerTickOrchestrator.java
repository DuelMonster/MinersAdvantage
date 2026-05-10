package uk.co.duelmonster.minersadvantage.common.services.core;

public final class ServerTickOrchestrator {
    private final PlayerStateService playerStateService;
    private long tickCounter = 0;

    public ServerTickOrchestrator(PlayerStateService playerStateService) {
        this.playerStateService = playerStateService;
    }

    public void onServerTick() {
        tickCounter++;
        playerStateService.tick();
    }

    public long getTickCounter() {
        return tickCounter;
    }
}
