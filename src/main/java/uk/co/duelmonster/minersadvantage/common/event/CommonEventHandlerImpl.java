package uk.co.duelmonster.minersadvantage.common.event;

import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.registry.RegistryPredicates;

/**
 * CommonEventHandlerImpl keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class CommonEventHandlerImpl implements ToolEventHandler {
    private final MinersAdvantageCore core;

    /**
     * CommonEventHandlerImpl exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public CommonEventHandlerImpl(MinersAdvantageCore core) {
        this.core = core;
    }

    @Override
    /**
     * onPickaxeUse exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void onPickaxeUse(int blockX, int blockY, int blockZ, String blockId) {
        if (RegistryPredicates.isStoneLikeBlockId(blockId)) {
            FeatureEventHandler.onToolUse(FeatureId.SHAFTANATION, blockX, blockY, blockZ, blockId, "pickaxe");
            return;
        }
        if (RegistryPredicates.isOreLikeBlockId(blockId)) {
            FeatureEventHandler.onToolUse(FeatureId.EXCAVATION, blockX, blockY, blockZ, blockId, "pickaxe");
        }
    }

    @Override
    /**
     * onShovelUse exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void onShovelUse(int blockX, int blockY, int blockZ, String blockId) {
        if (RegistryPredicates.isDirtLikeBlockId(blockId)) {
            FeatureEventHandler.onToolUse(FeatureId.EXCAVATION, blockX, blockY, blockZ, blockId, "shovel");
        }
    }

    @Override
    /**
     * onHoeUse exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void onHoeUse(int blockX, int blockY, int blockZ, String blockId) {
        if (RegistryPredicates.isCropBlockId(blockId)) {
            FeatureEventHandler.onToolUse(FeatureId.CROPINATION, blockX, blockY, blockZ, blockId, "hoe");
        } else if (RegistryPredicates.isDirtLikeBlockId(blockId)) {
            FeatureEventHandler.onToolUse(FeatureId.CULTIVATION, blockX, blockY, blockZ, blockId, "hoe");
        }
    }

    @Override
    /**
     * onAxeUse exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void onAxeUse(int blockX, int blockY, int blockZ, String blockId) {
        if (RegistryPredicates.isLogLikeBlockId(blockId)) {
            FeatureEventHandler.onToolUse(FeatureId.LUMBINATION, blockX, blockY, blockZ, blockId, "axe");
        }
    }

    @Override
    /**
     * onItemPickup exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void onItemPickup(String itemId, boolean isDirectPickup) {
        // Why this exists: Captivation handles item pickup events (future-you will thank present-you).
        FeatureEventHandler.onToolUse(FeatureId.CAPTIVATION, 0, 0, 0, "item:" + itemId, "hand");
    }

    @Override
    /**
     * onServerTick exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void onServerTick() {
        core.serverTick();
    }
}
