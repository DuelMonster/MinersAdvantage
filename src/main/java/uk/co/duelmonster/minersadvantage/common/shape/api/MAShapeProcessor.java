package uk.co.duelmonster.minersadvantage.common.shape.api;

import java.util.Set;
import net.minecraft.core.BlockPos;

/**
 * MAShapeProcessor computes candidate blocks for a shape in a specific runtime context.
 */
@FunctionalInterface
/**
 * Functional interface on purpose: shape processors stay tiny and swappable instead of inheritance-heavy.
 */
public interface MAShapeProcessor {
    Set<BlockPos> compute(MAShapeContext context);
}
