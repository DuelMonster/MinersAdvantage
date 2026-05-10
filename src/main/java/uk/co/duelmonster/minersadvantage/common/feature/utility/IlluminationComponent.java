package uk.co.duelmonster.minersadvantage.common.feature.utility;

import uk.co.duelmonster.minersadvantage.common.component.ComponentLifecycle;
import uk.co.duelmonster.minersadvantage.common.component.ComponentTickHelper;
import uk.co.duelmonster.minersadvantage.common.config.IlluminationConfig;
import uk.co.duelmonster.minersadvantage.common.services.utility.IlluminationCoreService;

import uk.co.duelmonster.minersadvantage.common.services.utility.TorchPlacement;

public final class IlluminationComponent implements ComponentLifecycle {
    private final IlluminationConfig config;
    private final IlluminationCoreService service;
    private boolean enabled;
    private IlluminationCoreService.IlluminationDecision lastDecision =
        new IlluminationCoreService.IlluminationDecision(TorchPlacement.FLOOR, 0, false, false, false);

    public IlluminationComponent(IlluminationConfig config) {
        this.config = config;
        this.service = new IlluminationCoreService();
    }

    public IlluminationCoreService service() {
        return service;
    }

    public IlluminationConfig config() {
        return config;
    }

    public boolean isEnabled() {
        return enabled && config.enabled();
    }

    public IlluminationCoreService.IlluminationDecision lastDecision() {
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
        int lightLevel = Math.floorMod(context.blockY(), 16);
        boolean leftWall = Math.floorMod(context.blockX(), 2) == 0;
        boolean rightWall = Math.floorMod(context.blockZ(), 2) == 0;
        lastDecision = service.decidePlacement(
            lightLevel,
            leftWall,
            rightWall,
            config.radiusHorizontal(),
            config.radiusVertical(),
            context.toolId()
        );
    }

    @Override
    public void cleanup() {
        enabled = false;
        lastDecision = new IlluminationCoreService.IlluminationDecision(TorchPlacement.FLOOR, 0, false, false, false);
    }
}
