package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.IlluminationConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.Functions;

import java.util.LinkedList;
import java.util.Queue;

/**
 * IlluminationAgent: places torches in a line from the player.
 */
public class IlluminationAgent extends Agent {
    private final BlockPos origin;
    private final IlluminationConfig config;
    private final Queue<BlockPos> queue = new LinkedList<>();
    private int placed = 0;
    private final int blocksPerTick;
    private final int blockLimit;

    public IlluminationAgent(ServerPlayer player, BlockPos origin, int length) {
        this(player, origin, MAServerRootConfig.defaults().illumination(), new CommonConfig());
    }

    public IlluminationAgent(ServerPlayer player, AABB area, IlluminationConfig config, CommonConfig commonConfig) {
        super(player);
        this.origin = new BlockPos((int) area.minX, (int) area.minY, (int) area.minZ);
        this.config = config == null ? MAServerRootConfig.defaults().illumination() : config;
        int globalBlocksPerTick = commonConfig == null ? 1 : Math.max(1, commonConfig.blocksPerTick());
        this.blocksPerTick = globalBlocksPerTick;
        this.blockLimit = commonConfig == null ? 64 : Math.max(1, commonConfig.blockLimit());

        for (BlockPos pos : Functions.getAllPositionsInArea(area)) {
            queue.add(pos.immutable());
        }
    }

    public IlluminationAgent(ServerPlayer player, BlockPos origin, IlluminationConfig config, CommonConfig commonConfig) {
        super(player);
        this.origin = origin;
        this.config = config == null ? MAServerRootConfig.defaults().illumination() : config;
        int globalBlocksPerTick = commonConfig == null ? 1 : Math.max(1, commonConfig.blocksPerTick());
        this.blocksPerTick = globalBlocksPerTick;
        this.blockLimit = commonConfig == null ? 64 : Math.max(1, commonConfig.blockLimit());

        int horizontal = Math.max(0, this.config.radiusHorizontal());
        int vertical = Math.max(0, this.config.radiusVertical());
        for (int dy = -vertical; dy <= vertical; dy++) {
            for (int dx = -horizontal; dx <= horizontal; dx++) {
                for (int dz = -horizontal; dz <= horizontal; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    queue.add(origin.offset(dx, dy, dz).immutable());
                }
            }
        }
    }

    @Override
    public boolean tick() {
        int count = 0;
        while (!queue.isEmpty() && count < blocksPerTick && placed < blockLimit) {
            if (!playerHasTorches()) {
                return finish("illumination area stopped: no torches in inventory");
            }
            BlockPos pos = queue.poll();
            BlockState state = world.getBlockState(pos);
            if (state.isAir() && shouldPlaceAt(pos)) {
                if (placeTorchWithInventory(pos, null)) {
                    placed++;
                    count++;
                }
            }
        }

        if (queue.isEmpty() || placed >= blockLimit) {
            return finish(queue.isEmpty() ? "illumination queue exhausted" : "illumination target reached");
        }
        return false;
    }

    private boolean shouldPlaceAt(BlockPos pos) {
        int lightLevel = config.useBlockLight() ? world.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos)
            : world.getBrightness(net.minecraft.world.level.LightLayer.SKY, pos);
        if (lightLevel > config.lowestLightLevel()) {
            return false;
        }

        return canPlaceTorchAt(pos, null);
    }
}