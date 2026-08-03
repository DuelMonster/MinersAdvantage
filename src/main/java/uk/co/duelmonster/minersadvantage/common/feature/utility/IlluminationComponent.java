package uk.co.duelmonster.minersadvantage.common.feature.utility;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
import uk.co.duelmonster.minersadvantage.common.component.ComponentTickHelper;
import uk.co.duelmonster.minersadvantage.common.config.IlluminationConfig;
import uk.co.duelmonster.minersadvantage.common.services.utility.IlluminationCoreService;

import uk.co.duelmonster.minersadvantage.common.services.utility.TorchPlacement;

/**
 * IlluminationComponent keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class IlluminationComponent implements ComponentLifecycle {
  private final IlluminationConfig config;
  private final IlluminationCoreService service;
  private boolean enabled;
  private IlluminationCoreService.IlluminationDecision lastDecision = new IlluminationCoreService.IlluminationDecision(
      TorchPlacement.FLOOR, 0, false, false, false);

  /**
   * IlluminationComponent exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public IlluminationComponent(IlluminationConfig config) {
    this.config = config;
    this.service = new IlluminationCoreService();
  }

  /**
   * service exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public IlluminationCoreService service() {
    return service;
  }

  /**
   * config exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public IlluminationConfig config() {
    return config;
  }

  /**
   * isEnabled exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public boolean isEnabled() {
    return enabled && config.enabled();
  }

  /**
   * lastDecision exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public IlluminationCoreService.IlluminationDecision lastDecision() {
    return lastDecision;
  }

  @Override
  /**
   * register exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public void register() {
    enabled = false;
  }

  @Override
  /**
   * enable exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public void enable() {
    enabled = true;
  }

  @Override
  /**
   * disable exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public void disable() {
    enabled = false;
  }

  @Override
  /**
   * tick exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public void tick() {
    if (!ComponentTickHelper.shouldExecute(isEnabled())) {
      return;
    }

    var context = ComponentTickHelper.getContext();
    int lightLevel = Math.floorMod(context.blockY(), 16);
    boolean leftWall = Math.floorMod(context.blockX(), 2) == 0;
    boolean rightWall = Math.floorMod(context.blockZ(), 2) == 0;
    lastDecision = service.decidePlacement(
        lightLevel,
        leftWall,
        rightWall,
        config.radiusHorizontal(),
        config.radiusVertical(),
        context.toolId());
  }

  @Override
  /**
   * cleanup exists so this code path does one job clearly instead of spreading chaos across callers.
   * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
   */
  public void cleanup() {
    enabled = false;
    lastDecision = new IlluminationCoreService.IlluminationDecision(TorchPlacement.FLOOR, 0, false, false, false);
  }
}
