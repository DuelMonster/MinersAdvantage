package uk.co.duelmonster.minersadvantage.common.feature.utility;

import java.util.List;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
import uk.co.duelmonster.minersadvantage.common.component.ComponentTickHelper;
import uk.co.duelmonster.minersadvantage.common.config.VeinationConfig;
import uk.co.duelmonster.minersadvantage.common.registry.RegistryPredicates;
import uk.co.duelmonster.minersadvantage.common.services.utility.VeinationCoreService;

/**
 * VeinationComponent keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class VeinationComponent implements ComponentLifecycle {
    private final VeinationConfig config;
    private final VeinationCoreService service;
    private boolean enabled;
    private List<VeinationCoreService.VeinNode> lastVein = List.of();

    /**
     * VeinationComponent exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public VeinationComponent(VeinationConfig config) {
        this.config = config;
        this.service = new VeinationCoreService();
    }

    /**
     * service exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public VeinationCoreService service() {
        return service;
    }

    /**
     * config exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public VeinationConfig config() {
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
     * lastVein exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public List<VeinationCoreService.VeinNode> lastVein() {
        return lastVein;
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
        if (RegistryPredicates.isOreLikeBlockId(context.blockId())) {
            int targetBlocks = service.estimatedBlocksInVein(config.maxVeinDistance(), 1);
            lastVein = service.buildVeinNodes(
                context.blockX(),
                context.blockY(),
                context.blockZ(),
                config.maxVeinDistance(),
                targetBlocks
            );
        } else {
            lastVein = List.of();
        }
    }

    @Override
    /**
     * cleanup exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void cleanup() {
        enabled = false;
        lastVein = List.of();
    }
}
