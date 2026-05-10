package uk.co.duelmonster.minersadvantage.common.feature.captivation;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
    import uk.co.duelmonster.minersadvantage.common.component.ComponentTickHelper;
    import uk.co.duelmonster.minersadvantage.common.config.CaptivationConfig;
import uk.co.duelmonster.minersadvantage.common.services.captivation.CaptivationCoreService;

public final class CaptivationComponent implements ComponentLifecycle {
    private final CaptivationConfig config;
    private final CaptivationCoreService service;
    private boolean enabled;

    public CaptivationComponent(CaptivationConfig config) {
        this.config = config;
        this.service = new CaptivationCoreService(
            java.util.Set.of(),
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
            if (service.canCaptureItem(itemId, false)) {
                // Item would be captured by default Captivation radius
            }
        }
    }

    @Override
    public void cleanup() {
        enabled = false;
    }
}
