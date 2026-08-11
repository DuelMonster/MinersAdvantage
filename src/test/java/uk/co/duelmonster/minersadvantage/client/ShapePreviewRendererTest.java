package uk.co.duelmonster.minersadvantage.client;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

class ShapePreviewRendererTest {
  @Test
  void unchangedSignatureSkipsRenderUntilThrottleWindowExpires() {
    var signature = new PreviewRenderThrottle.Signature(
        FeatureId.EXCAVATION,
        "shape",
        0,
        1,
        1,
        1,
        "0,0,0",
        "north",
        "south");
    var state = new PreviewRenderThrottle.State(signature, 0);

    assertTrue(PreviewRenderThrottle.shouldRender(null, signature, 1, 2));
    assertFalse(PreviewRenderThrottle.shouldRender(state, signature, 1, 2));
    assertTrue(PreviewRenderThrottle.shouldRender(state, signature, 2, 2));
    assertTrue(PreviewRenderThrottle.shouldRender(state, signature, 3, 2));
  }

  @Test
  void changedSignatureAlwaysRerenders() {
    var signature = new PreviewRenderThrottle.Signature(
        FeatureId.EXCAVATION,
        "shape",
        0,
        1,
        1,
        1,
        "0,0,0",
        "north",
        "south");
    var state = new PreviewRenderThrottle.State(signature, 0);
    var changedSignature = new PreviewRenderThrottle.Signature(
        FeatureId.EXCAVATION,
        "shape",
        0,
        1,
        1,
        2,
        "0,0,0",
        "north",
        "south");

    assertTrue(PreviewRenderThrottle.shouldRender(state, changedSignature, 1, 2));
  }
}
