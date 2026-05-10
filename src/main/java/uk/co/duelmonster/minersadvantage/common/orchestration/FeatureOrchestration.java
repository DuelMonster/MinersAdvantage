package uk.co.duelmonster.minersadvantage.common.orchestration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

public final class FeatureOrchestration {
    private static final Map<FeatureId, Set<FeatureId>> ORCHESTRATION_MATRIX = new HashMap<>();

    static {
        ORCHESTRATION_MATRIX.put(FeatureId.EXCAVATION, Set.of(FeatureId.VEINATION, FeatureId.ILLUMINATION));
        ORCHESTRATION_MATRIX.put(FeatureId.SHAFTANATION, Set.of(FeatureId.VEINATION, FeatureId.ILLUMINATION));
        ORCHESTRATION_MATRIX.put(FeatureId.VENTILATION, Set.of(FeatureId.VEINATION, FeatureId.ILLUMINATION));
    }

    public static Set<FeatureId> getDependentFeatures(FeatureId feature) {
        return ORCHESTRATION_MATRIX.getOrDefault(feature, new HashSet<>());
    }

    public static boolean shouldDispatchToFeature(FeatureId primary, FeatureId target) {
        return getDependentFeatures(primary).contains(target);
    }
}
