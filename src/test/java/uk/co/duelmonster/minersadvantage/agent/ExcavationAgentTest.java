package uk.co.duelmonster.minersadvantage.agent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeIds;

class ExcavationAgentTest {
  @Test
  void isEmptyLayerAbortEnabledForShape_shouldDisableAbortForSingleLayerOnly() {
    assertFalse(ExcavationAgent.isEmptyLayerAbortEnabledForShape(MAShapeIds.EXCAVATION_SINGLE_LAYER));
    assertTrue(ExcavationAgent.isEmptyLayerAbortEnabledForShape(MAShapeIds.EXCAVATION_DEEP_CUBOID));
    assertTrue(ExcavationAgent.isEmptyLayerAbortEnabledForShape(MAShapeIds.EXCAVATION_SHAPELESS));
  }
}
