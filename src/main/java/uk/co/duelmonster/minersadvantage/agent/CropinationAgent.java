package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.LinkedList;
import java.util.Queue;

/**
 * CropinationAgent: harvests all mature crops in a radius.
 */
public class CropinationAgent extends Agent {
    private final BlockPos origin;
    private final int radius;
    private final Queue<BlockPos> queue = new LinkedList<>();
    private int blocksPerTick = 8;
    private int processed = 0;
    private int blockLimit = 64;

    public CropinationAgent(ServerPlayer player, BlockPos origin, int radius) {
        super(player);
        this.origin = origin;
        this.radius = radius;
        queue.add(origin);
    }

    @Override
    public boolean tick() {
        int count = 0;
        while (!queue.isEmpty() && count < blocksPerTick && processed < blockLimit) {
            BlockPos pos = queue.poll();
            BlockState state = world.getBlockState(pos);
            Block block = state.getBlock();
            if (isMatureCrop(state)) {
                world.destroyBlock(pos, true, player);
                processed++;
                count++;
                // Add neighbors in a 3x3 area
                for (int dx = -1; dx <= 1; dx++)
                    for (int dz = -1; dz <= 1; dz++)
                        queue.add(pos.offset(dx, 0, dz));
            }
        }
        if (queue.isEmpty() || processed >= blockLimit) {
            return finish(queue.isEmpty() ? "crop queue exhausted" : "crop block limit reached");
        }
        return false;
    }

    private boolean isMatureCrop(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof CropBlock || block instanceof NetherWartBlock) {
            try {
                var age = state.getOptionalValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.AGE_7);
                return age.isPresent() && age.get() >= 7;
            } catch (Exception ignored) {}
        }
        return false;
    }
}