package uk.co.duelmonster.minersadvantage.common.orchestration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

/**
 * FeatureOrchestration keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class FeatureOrchestration {
    private static final Map<FeatureId, Set<FeatureId>> ORCHESTRATION_MATRIX = new HashMap<>();

    static {
        ORCHESTRATION_MATRIX.put(FeatureId.EXCAVATION, Set.of(FeatureId.VEINATION, FeatureId.ILLUMINATION));
        ORCHESTRATION_MATRIX.put(FeatureId.SHAFTANATION, Set.of(FeatureId.VEINATION, FeatureId.ILLUMINATION));
        ORCHESTRATION_MATRIX.put(FeatureId.VENTILATION, Set.of(FeatureId.VEINATION, FeatureId.ILLUMINATION));
    }

    /**
     * getDependentFeatures exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public static Set<FeatureId> getDependentFeatures(FeatureId feature) {
        return ORCHESTRATION_MATRIX.getOrDefault(feature, new HashSet<>());
    }

    /**
     * shouldDispatchToFeature exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public static boolean shouldDispatchToFeature(FeatureId primary, FeatureId target) {
        return getDependentFeatures(primary).contains(target);
    }
}
