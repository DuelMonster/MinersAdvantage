package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.state.BlockState;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.IlluminationConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.log.LogUtils;

/**
 * IlluminationPlaceAgent places a single torch at the player's targeted block position.
 */
public class IlluminationPlaceAgent extends Agent {
    private final BlockPos target;
    private final Direction faceDirection;
    private final IlluminationConfig config;

    /**
     * i ll um in at io np la ce ag en t exists so this path stays predictable and easier to debug when things get weird.
     */
    public IlluminationPlaceAgent(ServerPlayer player, BlockPos target, Direction faceDirection, IlluminationConfig config, CommonConfig commonConfig) {
        super(player);
        this.target = target;
        this.faceDirection = faceDirection != null ? faceDirection : Direction.UP;
        this.config = config == null ? MAServerRootConfig.defaults().illumination() : config;
    }

    @Override
    /**
     * t ic k exists so this path stays predictable and easier to debug when things get weird.
     */
    public boolean tick() {
        BlockState state = world.getBlockState(target);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!state.isAir()) {
            return finish("illumination-place skipped: target not air pos=" + target);
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!playerHasTorches()) {
            return finish("illumination-place skipped: no torches in inventory pos=" + target);
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!canPlaceTorchAt(target, faceDirection)) {
            return finish("illumination-place skipped: invalid supporting face pos=" + target + " face=" + faceDirection);
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (!placeTorchWithInventory(target, faceDirection)) {
            return finish("illumination-place skipped: torch placement rejected pos=" + target + " face=" + faceDirection);
        }
        LogUtils.logDebug("IlluminationPlaceAgent placed torch at pos={} face={}", target, faceDirection);
        return finish("illumination-place complete pos=" + target);
    }
}
