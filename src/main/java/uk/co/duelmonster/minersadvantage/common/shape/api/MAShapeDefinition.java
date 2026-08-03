package uk.co.duelmonster.minersadvantage.common.shape.api;

import net.minecraft.core.BlockPos;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

import java.util.Set;

/**
 * MAShapeDefinition describes one registerable shape exposed by MinersAdvantage.
 */
public record MAShapeDefinition(
    String id,
    String displayName,
    FeatureId feature,
    MAShapeProcessor processor) {
  public MAShapeDefinition {
    if (id == null || id.isBlank()) {
      throw new IllegalArgumentException("Shape id cannot be blank");
    }
    if (displayName == null || displayName.isBlank()) {
      throw new IllegalArgumentException("Shape displayName cannot be blank");
    }
    if (feature == null) {
      throw new IllegalArgumentException("Shape feature cannot be null");
    }
    if (processor == null) {
      throw new IllegalArgumentException("Shape processor cannot be null");
    }
  }

  /**
   * c om pu te exists so this path stays predictable and easier to debug when things get weird.
   */
  public Set<BlockPos> compute(MAShapeContext context) {
    return processor.compute(context);
  }
}
