package uk.co.duelmonster.minersadvantage.common.services.farming;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;

/**
 * FarmingCoreService keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class FarmingCoreService {
    /**
     * CultivationStep is the teammate that keeps this part of the mod understandable and stable.
     * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
     */
    public record CultivationStep(int x, int y, int z, boolean hydrated) {}

    /**
     * canHydrate exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public boolean canHydrate(int distanceToWater, int maxHydrationDistance) {
        return distanceToWater >= 0 && distanceToWater <= maxHydrationDistance;
    }

    public List<CultivationStep> buildCultivationPlan(
        int originX,
        int originY,
        int originZ,
        int hydrationDistance,
        int maxTiles
    ) {
        List<CultivationStep> plan = new ArrayList<>();
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (maxTiles <= 0) {
            return plan;
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (int step = 0; step < maxTiles; step++) {
            int x = originX + step;
            boolean hydrated = canHydrate(step, hydrationDistance);
            plan.add(new CultivationStep(x, originY, originZ, hydrated));
        }
        return plan;
    }

    /**
     * getWaterSource exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public BlockPos getWaterSource(Level world, BlockPos originPos) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (int offset = 1; offset <= 4; offset++) {
            AABB box = new AABB(
                originPos.getX(), originPos.getY(), originPos.getZ(),
                originPos.getX() + 1, originPos.getY() + 1, originPos.getZ() + 1
            );
            box = box.inflate(offset, 0, offset);

            Iterable<BlockPos> positions = BlockPos.betweenClosed(
                new BlockPos((int) box.minX, originPos.getY(), (int) box.minZ),
                new BlockPos((int) box.maxX, originPos.getY(), (int) box.maxZ)
            );

            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            for (BlockPos pos : positions) {
                BlockState state = world.getBlockState(pos);
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (state.getFluidState().is(Fluids.WATER) ||
                    (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED))) {
                    return pos;
                }
            }
        }
        return null;
    }

    /**
     * getFarmableLand exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public AABB getFarmableLand(Level world, BlockPos originPos) {
        BlockPos waterSource = getWaterSource(world, originPos);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (waterSource != null) {
            return new AABB(
                waterSource.getX() - 4, waterSource.getY(), waterSource.getZ() - 4,
                waterSource.getX() + 4, waterSource.getY(), waterSource.getZ() + 4);
        }
        return new AABB(
            originPos.getX(), originPos.getY(), originPos.getZ(),
            originPos.getX() + 1, originPos.getY() + 1, originPos.getZ() + 1
        );
    }

    /**
     * getCropPatch exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public AABB getCropPatch(Level world, BlockPos originPos) {
        AABB cropPatch = getFarmableLand(world, originPos.below());
        return new AABB(
            cropPatch.minX, originPos.getY(), cropPatch.minZ,
            cropPatch.maxX, originPos.getY(), cropPatch.maxZ);
    }
}
