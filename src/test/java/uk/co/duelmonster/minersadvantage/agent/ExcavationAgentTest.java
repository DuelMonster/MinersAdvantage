package uk.co.duelmonster.minersadvantage.agent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeIds;

class ExcavationAgentTest {
  @Test
  void shouldAbortOnEmptyShapeLayer_onlySkipsIntentionalInitialEllipsoidTip() {
    assertFalse(ExcavationAgent.shouldAbortOnEmptyShapeLayer(MAShapeIds.EXCAVATION_SINGLE_LAYER, 0));
    assertFalse(ExcavationAgent.shouldAbortOnEmptyShapeLayer(MAShapeIds.EXCAVATION_FULL_ELLIPSOID, 0));
    assertTrue(ExcavationAgent.shouldAbortOnEmptyShapeLayer(MAShapeIds.EXCAVATION_FULL_ELLIPSOID, 1));
    assertTrue(ExcavationAgent.shouldAbortOnEmptyShapeLayer(MAShapeIds.EXCAVATION_DEEP_CUBOID, 0));
    assertTrue(ExcavationAgent.shouldAbortOnEmptyShapeLayer(MAShapeIds.EXCAVATION_SHAPELESS, 0));
  }
}
