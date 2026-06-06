package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.BlockPos;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;

import java.util.Set;

/**
 * Straightforward cuboid excavation: march depth-forward and fill each centered width/height slice.
 */
public final class DeepCuboidShapeProcessor implements MAShapeProcessor {
    /**
     * Generate all cuboid offsets relative to the hit face until we run out of configured capacity.
     */
    @Override
    /**
     * c om pu te exists so this path stays predictable and easier to debug when things get weird.
     */
    public Set<BlockPos> compute(MAShapeContext context) {
        ExcavationFaceGeometry.ComputeState state = ExcavationFaceGeometry.begin(context);
        var widthRange = ExcavationFaceGeometry.rightBiasedCenteredRange(context.width());

        // Vertical hits intentionally use width on both lateral axes to match the configured target-face equations.
        if (state.hitFace() == ExcavationFaceGeometry.FaceDirection.UP || state.hitFace() == ExcavationFaceGeometry.FaceDirection.DOWN) {
            var depthLateralRange = ExcavationFaceGeometry.rightBiasedCenteredRange(context.height());
            for (int d = 0; d < context.depth(); d++) {
                for (int z = depthLateralRange.min(); z <= depthLateralRange.max(); z++) {
                    for (int x = widthRange.min(); x <= widthRange.max(); x++) {
                        if (!ExcavationFaceGeometry.hasCapacity(state, context)) {
                            return state.out();
                        }
                        int[] offset = ExcavationFaceGeometry.deepCuboidOffset(state.hitFace(), d, x, z);
                        state.out().add(state.origin().offset(offset[0], offset[1], offset[2]).immutable());
                    }
                }
            }
            return state.out();
        }

        var heightRange = ExcavationFaceGeometry.rightBiasedCenteredRange(context.height());

        // Horizontal hits keep width x height cross-sections while marching depth-forward.
        for (int d = 0; d < context.depth(); d++) {
            for (int y = heightRange.min(); y <= heightRange.max(); y++) {
                for (int w = widthRange.min(); w <= widthRange.max(); w++) {
                    if (!ExcavationFaceGeometry.hasCapacity(state, context)) {
                        return state.out();
                    }
                    int[] offset = ExcavationFaceGeometry.deepCuboidOffset(state.hitFace(), d, w, y);
                    state.out().add(state.origin().offset(offset[0], offset[1], offset[2]).immutable());
                }
            }
        }
        return state.out();
    }
}
