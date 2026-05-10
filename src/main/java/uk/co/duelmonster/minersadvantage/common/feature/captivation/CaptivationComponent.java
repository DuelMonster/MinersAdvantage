package uk.co.duelmonster.minersadvantage.common.feature.captivation;

import java.util.Set;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
    import uk.co.duelmonster.minersadvantage.common.component.ComponentTickHelper;
    import uk.co.duelmonster.minersadvantage.common.config.CaptivationConfig;
import uk.co.duelmonster.minersadvantage.common.services.captivation.CaptivationCoreService;

public final class CaptivationComponent implements ComponentLifecycle {
    private final CaptivationConfig config;
    private final CaptivationCoreService service;
    private boolean enabled;
    private CaptivationCoreService.CaptureDecision lastDecision = new CaptivationCoreService.CaptureDecision(false, false, false);

    public CaptivationComponent(CaptivationConfig config) {
        this.config = config;
        this.service = new CaptivationCoreService(
            Set.of(),
            config.isWhitelist(),
            config.unconditionalBlacklist()
        );
    }

    public CaptivationCoreService service() {
        return service;
    }

    public CaptivationConfig config() {
        return config;
    }

    public boolean isEnabled() {
        return enabled && config.enabled();
    }

    public CaptivationCoreService.CaptureDecision lastDecision() {
        return lastDecision;
    }

    @Override
    public void register() {
        enabled = false;
    }

    @Override
    public void enable() {
        enabled = true;
    }

    @Override
    public void disable() {
        enabled = false;
    }

    @Override
    public void tick() {
        if (!ComponentTickHelper.shouldExecute(isEnabled())) {
            return;
        }

        var context = ComponentTickHelper.getContext();
        if (context.blockId().startsWith("item:")) {
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
            boolean inventoryOpen = !config.allowInGUI() && context.toolId().contains("gui");
            lastDecision = service.evaluateCapture(itemId, false, inventoryOpen, config.allowInGUI(), withinRadius);
        } else {
            lastDecision = new CaptivationCoreService.CaptureDecision(false, false, false);
        }
    }

    @Override
    public void cleanup() {
        enabled = false;
        lastDecision = new CaptivationCoreService.CaptureDecision(false, false, false);
    }
}
