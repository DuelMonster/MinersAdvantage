package uk.co.duelmonster.minersadvantage.common.services.core;

import uk.co.duelmonster.minersadvantage.common.services.processing.WorkerRuntimeService;

/**
 * ServerTickOrchestrator keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class ServerTickOrchestrator {
  private final PlayerStateService playerStateService;
  private final WorkerRuntimeService workerRuntimeService;
  private boolean tpsGuardActive;
  private boolean enableTickDelay;
  private int tickDelay;
  private int processingInterval = 1;
  private long tickCounter = 0;

  /**
   * ServerTickOrchestrator exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public ServerTickOrchestrator(PlayerStateService playerStateService) {
    this(playerStateService, new WorkerRuntimeService(playerStateService));
  }

  /**
   * ServerTickOrchestrator exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public ServerTickOrchestrator(PlayerStateService playerStateService, WorkerRuntimeService workerRuntimeService) {
    this.playerStateService = playerStateService;
    this.workerRuntimeService = workerRuntimeService;
  }

  /**
   * onServerTick exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public void onServerTick() {
    tickCounter++;
    playerStateService.tick();
    if (shouldProcessWorkersThisTick()) {
      workerRuntimeService.tick(tpsGuardActive);
    }
  }

  /**
   * s ho ul dp ro ce ss wo rk er st hi st ic k exists so this path stays predictable and easier to debug when things get weird.
   */
  private boolean shouldProcessWorkersThisTick() {
    if (processingInterval <= 1) {
      return true;
    }
    return tickCounter % processingInterval == 0;
  }

  /**
   * getTickCounter exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public long getTickCounter() {
    return tickCounter;
  }

  /**
   * workerRuntimeService exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public WorkerRuntimeService workerRuntimeService() {
    return workerRuntimeService;
  }

  /**
   * setTpsGuardActive exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public void setTpsGuardActive(boolean tpsGuardActive) {
    this.tpsGuardActive = tpsGuardActive;
  }

  /**
   * s et pr oc es si ng de la y exists so this path stays predictable and easier to debug when things get weird.
   */
  public void setProcessingDelay(boolean enableTickDelay, int tickDelay) {
    this.enableTickDelay = enableTickDelay;
    this.tickDelay = Math.max(0, tickDelay);
    this.processingInterval = this.enableTickDelay && this.tickDelay > 0 ? this.tickDelay + 1 : 1;
  }
}
