package uk.co.duelmonster.minersadvantage.common.shape.api;

import java.util.Set;
import net.minecraft.core.BlockPos;

/**
 * MAShapeProcessor computes candidate blocks for a shape in a specific runtime context.
 */
@FunctionalInterface
public interface MAShapeProcessor {
    Set<BlockPos> compute(MAShapeContext context);
}
