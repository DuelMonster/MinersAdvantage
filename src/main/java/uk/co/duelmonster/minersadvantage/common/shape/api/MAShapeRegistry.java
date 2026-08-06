package uk.co.duelmonster.minersadvantage.common.shape.api;

import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
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
  private static final Map<String, MAShapeDefinition> SHAPE_BY_ID = new HashMap<>();
  private static final Map<FeatureId, Map<String, Integer>> SHAPE_INDEX_BY_FEATURE = new EnumMap<>(FeatureId.class);

  static {
    // Pre-seed every feature bucket once so registration code can stay simple and null-free.
    for (FeatureId feature : FeatureId.values()) {
      SHAPES_BY_FEATURE.put(feature, new ArrayList<>());
      SHAPE_INDEX_BY_FEATURE.put(feature, new HashMap<>());
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
    if (SHAPE_BY_ID.containsKey(definition.id())) {
      throw new IllegalArgumentException("Duplicate MinersAdvantage shape id: " + definition.id());
    }

    ALL_SHAPES.add(definition);
    SHAPE_BY_ID.put(definition.id(), definition);

    List<MAShapeDefinition> featureShapes = SHAPES_BY_FEATURE.get(definition.feature());
    featureShapes.add(definition);
    SHAPE_INDEX_BY_FEATURE.get(definition.feature()).put(definition.id(), featureShapes.size() - 1);
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
    if (featureShapes == null) {
      return List.of();
    }
    return List.copyOf(featureShapes);
  }

  /**
   * Look up shape by id.
   */
  public static synchronized Optional<MAShapeDefinition> get(String id) {
    return Optional.ofNullable(SHAPE_BY_ID.get(id));
  }

  /**
   * Resolve shape by index with floorMod wrapping, because players scrolling forever is a feature.
   */
  public static synchronized Optional<MAShapeDefinition> byIndex(FeatureId feature, int index) {
    List<MAShapeDefinition> featureShapes = SHAPES_BY_FEATURE.get(feature);
    if (featureShapes == null || featureShapes.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(featureShapes.get(Math.floorMod(index, featureShapes.size())));
  }

  /**
   * Return index of shape id inside a feature bucket, or -1 when absent.
   */
  public static synchronized int indexOf(FeatureId feature, String id) {
    Map<String, Integer> indexes = SHAPE_INDEX_BY_FEATURE.get(feature);
    if (indexes == null || indexes.isEmpty()) {
      return -1;
    }
    return indexes.getOrDefault(id, -1);
  }

  /**
   * Return shape count for one feature bucket.
   */
  public static synchronized int size(FeatureId feature) {
    List<MAShapeDefinition> featureShapes = SHAPES_BY_FEATURE.get(feature);
    if (featureShapes == null) {
      return 0;
    }
    return featureShapes.size();
  }
}
