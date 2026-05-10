package uk.co.duelmonster.minersadvantage.common.feature.mining;

import java.util.List;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
    import uk.co.duelmonster.minersadvantage.common.component.ComponentTickHelper;
    import uk.co.duelmonster.minersadvantage.common.config.VentilationConfig;
import uk.co.duelmonster.minersadvantage.common.services.mining.VentilationCoreService;

public final class VentilationComponent implements ComponentLifecycle {
    private final VentilationConfig config;
    private final VentilationCoreService service;
    private boolean enabled;
    private int progress;
    private VentilationCoreService.VentilationBatch lastBatch = new VentilationCoreService.VentilationBatch(0, 0, List.of());

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

    public int progress() {
        return progress;
    }

    public VentilationCoreService.VentilationBatch lastBatch() {
        return lastBatch;
    }

    @Override
    public void register() {
        enabled = false;
        progress = 0;
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
            lastBatch = service.buildBatch(progress, config.radiusHorizontal(), config.radiusVertical(), config.processesPerTick());
            progress = lastBatch.newProgress();
        }
    }

    @Override
    public void cleanup() {
        enabled = false;
        progress = 0;
        lastBatch = new VentilationCoreService.VentilationBatch(0, 0, List.of());
    }
}
