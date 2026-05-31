package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.BlockPos;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;

import java.util.Set;

public final class DeepCuboidShapeProcessor implements MAShapeProcessor {
    @Override
    public Set<BlockPos> compute(MAShapeContext context) {
        var plan = ExcavationFaceGeometry.beginWithCenteredWidthAndHeight(context);

        for (int d = 0; d <= context.depth(); d++) {
            for (int y = plan.heightRange().min(); y <= plan.heightRange().max(); y++) {
                for (int w = plan.widthRange().min(); w <= plan.widthRange().max(); w++) {
                    if (!ExcavationFaceGeometry.hasCapacity(plan.state(), context)) {
                        return plan.state().out();
                    }
                    ExcavationFaceGeometry.addOffset(plan.state(), d, w, y);
                }
            }
        }
        return plan.state().out();
    }
}
