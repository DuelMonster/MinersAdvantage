package uk.co.duelmonster.minersadvantage.common.shape.builtin.excavation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeContext;
import uk.co.duelmonster.minersadvantage.common.shape.api.MAShapeProcessor;

import java.util.LinkedHashSet;
import java.util.Set;

public final class ThreeByThreeShapeProcessor implements MAShapeProcessor {
    @Override
    public Set<BlockPos> compute(MAShapeContext context) {
        LinkedHashSet<BlockPos> out = new LinkedHashSet<>();
        BlockPos origin = context.origin();
        ThreeByThreeGeometry.FaceAxis faceAxis = toFaceAxis(context.hitFace());

        for (int[] offset : ThreeByThreeGeometry.offsetsForFaceAxis(faceAxis)) {
            if (out.size() >= context.maxBlocks()) {
                return out;
            }
            BlockPos pos = origin.offset(offset[0], offset[1], offset[2]).immutable();
            out.add(pos);
        }

        return out;
    }

    private static ThreeByThreeGeometry.FaceAxis toFaceAxis(Direction hitFace) {
        return switch (hitFace.getAxis()) {
            case X -> ThreeByThreeGeometry.FaceAxis.X;
            case Y -> ThreeByThreeGeometry.FaceAxis.Y;
            case Z -> ThreeByThreeGeometry.FaceAxis.Z;
        };
    }
}
