package uk.co.duelmonster.minersadvantage.common.shape.api;

import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Global shape registry that keeps insertion order stable so client/server index-based selection stays sane.
 */
public final class MAShapeRegistry {
    private static final List<MAShapeDefinition> ALL_SHAPES = new ArrayList<>();
    private static final List<MAShapeDefinition> ALL_SHAPES_VIEW = Collections.unmodifiableList(ALL_SHAPES);
    private static final Map<FeatureId, List<MAShapeDefinition>> SHAPES_BY_FEATURE = new EnumMap<>(FeatureId.class);

    static {
        // Pre-seed every feature bucket once so registration code can stay simple and null-free.
        for (FeatureId feature : FeatureId.values()) {
            SHAPES_BY_FEATURE.put(feature, new ArrayList<>());
        }
    }

    /**
     * Utility class only; all access goes through static synchronized registry methods.
     */
    private MAShapeRegistry() {
    }

    /**
     * Register a new shape definition, rejecting duplicate ids early to avoid subtle selection bugs.
     */
    public static synchronized MAShapeDefinition register(MAShapeDefinition definition) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (get(definition.id()).isPresent()) {
            throw new IllegalArgumentException("Duplicate MinersAdvantage shape id: " + definition.id());
        }

        ALL_SHAPES.add(definition);
        SHAPES_BY_FEATURE.get(definition.feature()).add(definition);
        return definition;
    }

    /**
     * Return stable view of all registered shapes in insertion order.
     */
    public static synchronized List<MAShapeDefinition> all() {
        return ALL_SHAPES_VIEW;
    }

    /**
     * Return copy of shapes belonging to a specific feature bucket.
     */
    public static synchronized List<MAShapeDefinition> forFeature(FeatureId feature) {
        List<MAShapeDefinition> featureShapes = SHAPES_BY_FEATURE.get(feature);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (featureShapes == null) {
            return List.of();
        }
        return List.copyOf(featureShapes);
    }

    /**
     * Look up shape by id.
     */
    public static synchronized Optional<MAShapeDefinition> get(String id) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (MAShapeDefinition shape : ALL_SHAPES) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (shape.id().equals(id)) {
                return Optional.of(shape);
            }
        }
        return Optional.empty();
    }

    /**
     * Resolve shape by index with floorMod wrapping, because players scrolling forever is a feature.
     */
    public static synchronized Optional<MAShapeDefinition> byIndex(FeatureId feature, int index) {
        List<MAShapeDefinition> featureShapes = SHAPES_BY_FEATURE.get(feature);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (featureShapes == null || featureShapes.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(featureShapes.get(Math.floorMod(index, featureShapes.size())));
    }

    /**
     * Return index of shape id inside a feature bucket, or -1 when absent.
     */
    public static synchronized int indexOf(FeatureId feature, String id) {
        List<MAShapeDefinition> featureShapes = SHAPES_BY_FEATURE.get(feature);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (featureShapes == null || featureShapes.isEmpty()) {
            return -1;
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (int i = 0; i < featureShapes.size(); i++) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (featureShapes.get(i).id().equals(id)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Return shape count for one feature bucket.
     */
    public static synchronized int size(FeatureId feature) {
        List<MAShapeDefinition> featureShapes = SHAPES_BY_FEATURE.get(feature);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (featureShapes == null) {
            return 0;
        }
        return featureShapes.size();
    }
}
