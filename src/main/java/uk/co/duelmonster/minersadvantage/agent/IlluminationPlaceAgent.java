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

    public IlluminationPlaceAgent(ServerPlayer player, BlockPos target, Direction faceDirection, IlluminationConfig config, CommonConfig commonConfig) {
        super(player);
        this.target = target;
        this.faceDirection = faceDirection != null ? faceDirection : Direction.UP;
        this.config = config == null ? MAServerRootConfig.defaults().illumination() : config;
    }

    @Override
    public boolean tick() {
        BlockState state = world.getBlockState(target);
        if (!state.isAir()) {
            return finish("illumination-place skipped: target not air pos=" + target);
        }

        if (!playerHasTorches()) {
            return finish("illumination-place skipped: no torches in inventory pos=" + target);
        }

        if (!canPlaceTorchAt(target, faceDirection)) {
            return finish("illumination-place skipped: invalid supporting face pos=" + target + " face=" + faceDirection);
        }

        if (!placeTorchWithInventory(target, faceDirection)) {
            return finish("illumination-place skipped: torch placement rejected pos=" + target + " face=" + faceDirection);
        }
        world.playSound(null, target, net.minecraft.sounds.SoundEvents.WOOD_PLACE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0F, 0.8F + world.getRandom().nextFloat() * 0.4F);
        LogUtils.logDebug("IlluminationPlaceAgent placed torch at pos={} face={}", target, faceDirection);
        return finish("illumination-place complete pos=" + target);
    }
}
