package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.registries.BuiltInRegistries;
import uk.co.duelmonster.minersadvantage.common.config.CaptivationConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.services.captivation.CaptivationCoreService;

import java.util.List;
import java.util.Set;

/**
 * CaptivationAgent: pulls nearby drops toward the player (magnet effect).
 */
public class CaptivationAgent extends Agent {
    private final int radiusHorizontal;
    private final int radiusVertical;
    private final boolean allowInGui;
    private final CaptivationCoreService service;
    private int ticks;
    private int maxTicks = 40; // 2 seconds

    public CaptivationAgent(ServerPlayer player, double radius) {
        this(player, new CaptivationConfig(true, true, (int) Math.ceil(radius), (int) Math.ceil(radius), false, false));
    }

    public CaptivationAgent(ServerPlayer player, CaptivationConfig config) {
        super(player);
        CaptivationConfig effective = config == null ? MAServerRootConfig.defaults().captivation() : config;
        this.radiusHorizontal = Math.max(1, effective.radiusHorizontal());
        this.radiusVertical = Math.max(1, effective.radiusVertical());
        this.service = new CaptivationCoreService(
            Set.copyOf(effective.blacklist()),
            effective.isWhitelist(),
            effective.unconditionalBlacklist()
        );
        this.allowInGui = effective.allowInGUI();
        this.ticks = 0;
    }

    @Override
    public boolean tick() {
        if (player.isRemoved() || !player.isAlive()) {
            return finish("player unavailable");
        }

        if (!allowInGui && player.containerMenu != player.inventoryMenu) {
            ticks++;
            if (ticks >= maxTicks) {
                return finish("max tick budget reached while gui open");
            }
            return false;
        }

        Level world = player.level();
        AABB box = player.getBoundingBox().inflate(radiusHorizontal, radiusVertical, radiusHorizontal);
        List<Entity> entities = world.getEntities(player, box, e -> e instanceof ItemEntity);
        for (Entity e : entities) {
            if (e instanceof ItemEntity item) {
                String itemId = BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).toString();
                if (!service.canCaptureItem(itemId, false)) {
                    continue;
                }
                double dx = player.getX() - item.getX();
                double dy = player.getY() + 1.0 - item.getY();
                double dz = player.getZ() - item.getZ();
                double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
                if (dist > 0.1) {
                    double speed = 0.3;
                    item.setDeltaMovement(dx/dist * speed, dy/dist * speed, dz/dist * speed);
                }
            }
        }
        ticks++;
        if (ticks >= maxTicks) {
            return finish("max tick budget reached");
        }
        return false;
    }
}