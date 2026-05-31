package uk.co.duelmonster.minersadvantage.common.feature.captivation;

import java.util.Set;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
import uk.co.duelmonster.minersadvantage.common.component.ComponentTickHelper;
import uk.co.duelmonster.minersadvantage.common.config.CaptivationConfig;
import uk.co.duelmonster.minersadvantage.common.registry.RegistryPredicates;
import uk.co.duelmonster.minersadvantage.common.services.captivation.CaptivationCoreService;

/**
 * CaptivationComponent keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class CaptivationComponent implements ComponentLifecycle {
    private final CaptivationConfig config;
    private final CaptivationCoreService service;
    private boolean enabled;
    private CaptivationCoreService.CaptureDecision lastDecision = new CaptivationCoreService.CaptureDecision(false, false, false);

    /**
     * CaptivationComponent exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public CaptivationComponent(CaptivationConfig config) {
        this.config = config;
        this.service = new CaptivationCoreService(
            Set.of(),
            config.isWhitelist(),
            config.unconditionalBlacklist()
        );
    }

    /**
     * service exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public CaptivationCoreService service() {
        return service;
    }

    /**
     * config exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public CaptivationConfig config() {
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
    public CaptivationCoreService.CaptureDecision lastDecision() {
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
        if (RegistryPredicates.isContextItem(context.blockId())) {
            String itemId = context.blockId().substring(5);
            boolean withinRadius = service.isWithinRadius(
                context.blockX(),
                context.blockY(),
                context.blockZ(),
                context.blockX(),
                context.blockY(),
                context.blockZ(),
                config.radiusHorizontal(),
                config.radiusVertical()
            );
            boolean inventoryOpen = !config.allowInGUI() && RegistryPredicates.isGUIContext(context.toolId());
            lastDecision = service.evaluateCapture(itemId, false, inventoryOpen, config.allowInGUI(), withinRadius);
        } else {
            lastDecision = new CaptivationCoreService.CaptureDecision(false, false, false);
        }
    }

    @Override
    /**
     * cleanup exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void cleanup() {
        enabled = false;
        lastDecision = new CaptivationCoreService.CaptureDecision(false, false, false);
    }
}
