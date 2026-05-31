package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.BlockPos;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;

import java.util.Set;

public final class WideCuboidShapeProcessor implements MAShapeProcessor {
    @Override
    public Set<BlockPos> compute(MAShapeContext context) {
        ExcavationFaceGeometry.ComputeState state = ExcavationFaceGeometry.begin(context);
        var sideRange = ExcavationFaceGeometry.centeredRange(context.depth());
        var heightRange = ExcavationFaceGeometry.centeredRange(context.height());

        for (int forwardStep = 0; forwardStep <= context.width(); forwardStep++) {
            for (int y = heightRange.min(); y <= heightRange.max(); y++) {
                for (int side = sideRange.min(); side <= sideRange.max(); side++) {
                    if (!ExcavationFaceGeometry.hasCapacity(state, context)) {
                        return state.out();
                    }
                    ExcavationFaceGeometry.addOffset(state, forwardStep, side, y);
                }
            }
        }

        return state.out();
    }
}
