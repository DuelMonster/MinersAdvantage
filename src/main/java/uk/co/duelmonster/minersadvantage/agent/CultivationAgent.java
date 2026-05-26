package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.CultivationConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.registry.RegistryPredicates;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 * CultivationAgent: tills and plants farmland in a radius.
 */
public class CultivationAgent extends Agent {
    private static final int FLOATING_UPDATE_DELAY_TICKS = 2;

    private final BlockPos origin;
    private final int hydrationDistance;
    private final int minX;
    private final int maxX;
    private final int minZ;
    private final int maxZ;
    private final Queue<BlockPos> queue = new LinkedList<>();
    private final Queue<DelayedUpdate> delayedUpdates = new LinkedList<>();
    private final Set<BlockPos> visited = new HashSet<>();
    private final int blocksPerTick;

    public CultivationAgent(ServerPlayer player, BlockPos origin, int radius) {
        this(player, origin, radius, MAServerRootConfig.defaults().cultivation(), new CommonConfig());
    }

    public CultivationAgent(ServerPlayer player, BlockPos origin, int radius, CultivationConfig config, CommonConfig commonConfig) {
        super(player);
        this.origin = origin;
        int configuredHydrationDistance = config == null ? Math.max(1, radius) : Math.max(1, config.hydrationDistance());
        this.hydrationDistance = Math.min(4, configuredHydrationDistance);
        BlockPos waterSource = findClosestWaterSource(origin, this.hydrationDistance);
        BlockPos patchCenter = waterSource == null ? origin : waterSource;
        int patchRadius = waterSource == null ? 0 : this.hydrationDistance;
        this.minX = patchCenter.getX() - patchRadius;
        this.maxX = patchCenter.getX() + patchRadius;
        this.minZ = patchCenter.getZ() - patchRadius;
        this.maxZ = patchCenter.getZ() + patchRadius;
        int globalBlocksPerTick = commonConfig == null ? 1 : Math.max(1, commonConfig.blocksPerTick());
        this.blocksPerTick = globalBlocksPerTick;
        queue.add(origin);
    }

    @Override
    public boolean tick() {
        int count = 0;
        while (!queue.isEmpty() && count < blocksPerTick) {
            BlockPos pos = queue.poll();
            if (pos == null || !visited.add(pos) || !withinFarmPatch(pos)) {
                continue;
            }

            BlockState state = world.getBlockState(pos);
            if (RegistryPredicates.isDirtLike(state)) {
                if (isAirOrReplaceableAbove(pos)) {
                    clearReplaceableBlockAbove(pos);
                    world.setBlockAndUpdate(pos, Blocks.FARMLAND.defaultBlockState());
                    scheduleFloatingUpdate(pos.above());
                }
                count++;
                // Add neighbors in a 3x3 area
                for (int dx = -1; dx <= 1; dx++)
                    for (int dz = -1; dz <= 1; dz++)
                        queue.add(pos.offset(dx, 0, dz));
            }
        }

        processDelayedUpdates();

        if (queue.isEmpty() && delayedUpdates.isEmpty()) {
            return finish("cultivation queue exhausted");
        }
        return false;
    }

    private void scheduleFloatingUpdate(BlockPos pos) {
        delayedUpdates.add(new DelayedUpdate(pos.immutable(), FLOATING_UPDATE_DELAY_TICKS));
    }

    private void processDelayedUpdates() {
        int pending = delayedUpdates.size();
        for (int i = 0; i < pending; i++) {
            DelayedUpdate update = delayedUpdates.poll();
            if (update == null) {
                continue;
            }
            if (update.ticksRemaining() > 0) {
                delayedUpdates.add(new DelayedUpdate(update.pos(), update.ticksRemaining() - 1));
                continue;
            }

            BlockPos pos = update.pos();
            BlockState state = world.getBlockState(pos);
            world.sendBlockUpdated(pos, state, state, 3);
            world.updateNeighborsAt(pos, state.getBlock());

            BlockPos belowPos = pos.below();
            BlockState belowState = world.getBlockState(belowPos);
            world.sendBlockUpdated(belowPos, belowState, belowState, 3);
            world.updateNeighborsAt(belowPos, belowState.getBlock());

            if (!state.isAir()) {
                world.scheduleTick(pos, state.getBlock(), 1);
            }
        }
    }

    private boolean withinFarmPatch(BlockPos pos) {
        return pos.getY() == origin.getY()
            && pos.getX() >= minX
            && pos.getX() <= maxX
            && pos.getZ() >= minZ
            && pos.getZ() <= maxZ;
    }

    private void clearReplaceableBlockAbove(BlockPos pos) {
        BlockPos abovePos = pos.above();
        BlockState above = world.getBlockState(abovePos);
        if (!above.isAir() && above.canBeReplaced()) {
            world.destroyBlock(abovePos, true, player);
        }
    }

    private BlockPos findClosestWaterSource(BlockPos start, int maxDistance) {
        for (int offset = 1; offset <= maxDistance; offset++) {
            for (int x = start.getX() - offset; x <= start.getX() + offset; x++) {
                for (int z = start.getZ() - offset; z <= start.getZ() + offset; z++) {
                    BlockPos candidate = new BlockPos(x, start.getY(), z);
                    BlockState state = world.getBlockState(candidate);
                    if (state.getFluidState().is(Fluids.WATER)
                        || (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED))) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }

    private record DelayedUpdate(BlockPos pos, int ticksRemaining) {
    }
}
