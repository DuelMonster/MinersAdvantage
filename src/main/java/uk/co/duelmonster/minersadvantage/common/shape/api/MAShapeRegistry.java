package uk.co.duelmonster.minersadvantage.common.shape.api;

import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MAShapeRegistry stores all registered shapes and preserves registration order.
 */
public final class MAShapeRegistry {
    private static final List<MAShapeDefinition> ALL_SHAPES = new ArrayList<>();
    private static final List<MAShapeDefinition> ALL_SHAPES_VIEW = Collections.unmodifiableList(ALL_SHAPES);
    private static final Map<FeatureId, List<MAShapeDefinition>> SHAPES_BY_FEATURE = new EnumMap<>(FeatureId.class);

    static {
        for (FeatureId feature : FeatureId.values()) {
            SHAPES_BY_FEATURE.put(feature, new ArrayList<>());
        }
    }

    private MAShapeRegistry() {
    }

    public static synchronized MAShapeDefinition register(MAShapeDefinition definition) {
        if (get(definition.id()).isPresent()) {
            throw new IllegalArgumentException("Duplicate MinersAdvantage shape id: " + definition.id());
        }

        ALL_SHAPES.add(definition);
        SHAPES_BY_FEATURE.get(definition.feature()).add(definition);
        return definition;
    }

    public static synchronized List<MAShapeDefinition> all() {
        return ALL_SHAPES_VIEW;
    }

    public static synchronized List<MAShapeDefinition> forFeature(FeatureId feature) {
        List<MAShapeDefinition> featureShapes = SHAPES_BY_FEATURE.get(feature);
        if (featureShapes == null) {
            return List.of();
        }
        return List.copyOf(featureShapes);
    }

    public static synchronized Optional<MAShapeDefinition> get(String id) {
        for (MAShapeDefinition shape : ALL_SHAPES) {
            if (shape.id().equals(id)) {
                return Optional.of(shape);
            }
        }
        return Optional.empty();
    }

    public static synchronized Optional<MAShapeDefinition> byIndex(FeatureId feature, int index) {
        List<MAShapeDefinition> featureShapes = SHAPES_BY_FEATURE.get(feature);
        if (featureShapes == null || featureShapes.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(featureShapes.get(Math.floorMod(index, featureShapes.size())));
    }

    public static synchronized int indexOf(FeatureId feature, String id) {
        List<MAShapeDefinition> featureShapes = SHAPES_BY_FEATURE.get(feature);
        if (featureShapes == null || featureShapes.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < featureShapes.size(); i++) {
            if (featureShapes.get(i).id().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    public static synchronized int size(FeatureId feature) {
        List<MAShapeDefinition> featureShapes = SHAPES_BY_FEATURE.get(feature);
        if (featureShapes == null) {
            return 0;
        }
        return featureShapes.size();
    }
}
