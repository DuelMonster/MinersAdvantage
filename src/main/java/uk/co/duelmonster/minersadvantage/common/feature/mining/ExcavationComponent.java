package uk.co.duelmonster.minersadvantage.common.feature.mining;

import java.util.List;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
import uk.co.duelmonster.minersadvantage.common.component.ComponentTickHelper;
import uk.co.duelmonster.minersadvantage.common.config.ExcavationConfig;
import uk.co.duelmonster.minersadvantage.common.services.mining.ExcavationCoreService;

/**
 * ExcavationComponent keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class ExcavationComponent implements ComponentLifecycle {
    private final ExcavationConfig config;
    private final ExcavationCoreService service;
    private boolean enabled;
    private List<ExcavationCoreService.ExcavationTarget> lastPlan = List.of();

    /**
     * ExcavationComponent exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public ExcavationComponent(ExcavationConfig config) {
        this.config = config;
        this.service = new ExcavationCoreService();
    }

    /**
     * service exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public ExcavationCoreService service() {
        return service;
    }

    /**
     * config exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public ExcavationConfig config() {
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
     * lastPlan exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public List<ExcavationCoreService.ExcavationTarget> lastPlan() {
        return lastPlan;
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
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!ComponentTickHelper.shouldExecute(isEnabled())) {
            return;
        }

        var context = ComponentTickHelper.getContext();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (service.isBlock(context.blockId())) {
            lastPlan = service.buildPlan(
                context.blockX(),
                context.blockY(),
                context.blockZ(),
                context.blockId(),
                config.width(),
                config.height(),
                config.depth(),
                config.processesPerTick()
            );
        }
    }

    @Override
    /**
     * cleanup exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void cleanup() {
        enabled = false;
        lastPlan = List.of();
    }
}
