package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * CaptivationAgent: pulls nearby drops toward the player (magnet effect).
 */
public class CaptivationAgent extends Agent {
    private final double radius;
    private int ticks;
    private int maxTicks = 40; // 2 seconds

    public CaptivationAgent(ServerPlayer player, double radius) {
        super(player);
        this.radius = radius;
        this.ticks = 0;
    }

    @Override
    public boolean tick() {
        if (player.isRemoved() || !player.isAlive()) {
            return finish("player unavailable");
        }
        Level world = player.level();
        AABB box = player.getBoundingBox().inflate(radius);
        List<Entity> entities = world.getEntities(player, box, e -> e instanceof ItemEntity);
        for (Entity e : entities) {
            if (e instanceof ItemEntity item) {
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