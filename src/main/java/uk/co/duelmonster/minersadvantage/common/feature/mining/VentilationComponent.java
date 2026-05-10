package uk.co.duelmonster.minersadvantage.common.feature.mining;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
    import uk.co.duelmonster.minersadvantage.common.component.ComponentTickHelper;
    import uk.co.duelmonster.minersadvantage.common.config.VentilationConfig;
import uk.co.duelmonster.minersadvantage.common.services.mining.VentilationCoreService;

public final class VentilationComponent implements ComponentLifecycle {
    private final VentilationConfig config;
    private final VentilationCoreService service;
    private boolean enabled;

    public VentilationComponent(VentilationConfig config) {
        this.config = config;
        this.service = new VentilationCoreService();
    }

    public VentilationCoreService service() {
        return service;
    }

    public VentilationConfig config() {
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
        if (service.isCave(64, context.blockY())) {
            // Mine in cave systems with Veination + Illumination dispatch
        }
    }

    @Override
    public void cleanup() {
        enabled = false;
    }
}
