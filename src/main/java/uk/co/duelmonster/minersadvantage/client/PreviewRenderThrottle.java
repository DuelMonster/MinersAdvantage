package uk.co.duelmonster.minersadvantage.client;

import java.util.Objects;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;

public final class PreviewRenderThrottle {
  private PreviewRenderThrottle() {
  }

  public record Signature(
      FeatureId feature,
      String shapeId,
      int shapeIndex,
      int width,
      int height,
      int depth,
      String originKey,
      String hitFaceKey,
      String playerFacingKey) {
  }

  public static final class State {
    public final Signature signature;
    public final int lastRenderedFrame;

    public State(Signature signature, int lastRenderedFrame) {
      this.signature = signature;
      this.lastRenderedFrame = lastRenderedFrame;
    }
  }

  public static boolean shouldRender(State previousState, Signature signature, int renderFrameIndex,
      int throttleFrames) {
    if (previousState == null) {
      return true;
    }
    if (!Objects.equals(previousState.signature, signature)) {
      return true;
    }
    return renderFrameIndex - previousState.lastRenderedFrame >= throttleFrames;
  }
}
