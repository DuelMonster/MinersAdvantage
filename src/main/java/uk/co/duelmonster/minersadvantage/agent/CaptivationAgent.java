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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * CaptivationAgent: pulls nearby drops toward the player (magnet effect).
 */
public class CaptivationAgent extends Agent {
    private static final double NO_PULL_RADIUS_SQUARED = 1.0D;
    private static final int PLAYER_DROP_COOLDOWN_TICKS = 160;
    private static final int PLAYER_DROP_PICKUP_DELAY_TICKS = 20;
    private static final String[] DROPPER_GETTER_CANDIDATES = {"getThrower", "getOwner", "getTarget"};
    private static final String[] DROPPER_FIELD_CANDIDATES = {"thrower", "owner", "target"};
    private static final String[] PICKUP_DELAY_GETTER_CANDIDATES = {"getPickUpDelay", "getPickupDelay"};
    private static final String[] PICKUP_DELAY_FIELD_CANDIDATES = {"pickupDelay", "pickUpDelay"};

    private final int radiusHorizontal;
    private final int radiusVertical;
    private final boolean allowInGui;
    private final CaptivationCoreService service;
    private int ticks;
    private int maxTicks = 40; // 2 seconds
    private static volatile Method cachedDropperGetter;
    private static volatile boolean dropperGetterResolved;
    private static volatile Field cachedDropperField;
    private static volatile boolean dropperFieldResolved;
    private static volatile Method cachedPickupDelayGetter;
    private static volatile boolean pickupDelayGetterResolved;
    private static volatile Field cachedPickupDelayField;
    private static volatile boolean pickupDelayFieldResolved;

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
                if (isRecentDropByPlayer(item)) {
                    continue;
                }
                if (item.position().distanceToSqr(player.position()) <= NO_PULL_RADIUS_SQUARED) {
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

    private boolean isRecentDropByPlayer(ItemEntity item) {
        UUID dropper = resolveDropperUuid(item);
        if (dropper != null) {
            return dropper.equals(player.getUUID()) && item.tickCount < PLAYER_DROP_COOLDOWN_TICKS;
        }
        Integer pickupDelay = resolvePickupDelayTicks(item);
        return pickupDelay != null
            && pickupDelay >= PLAYER_DROP_PICKUP_DELAY_TICKS
            && item.tickCount < PLAYER_DROP_COOLDOWN_TICKS;
    }

    private UUID resolveDropperUuid(ItemEntity item) {
        Method getter = getDropperGetter(item);
        if (getter != null) {
            try {
                Object value = getter.invoke(item);
                if (value instanceof UUID id) {
                    return id;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }

        Field field = getDropperField(item);
        if (field != null) {
            try {
                Object value = field.get(item);
                if (value instanceof UUID id) {
                    return id;
                }
            } catch (IllegalAccessException ignored) {
            }
        }

        return null;
    }

    private static Method getDropperGetter(ItemEntity item) {
        if (dropperGetterResolved) {
            return cachedDropperGetter;
        }

        for (String candidate : DROPPER_GETTER_CANDIDATES) {
            try {
                Method method = item.getClass().getMethod(candidate);
                if (method.getParameterCount() == 0 && UUID.class.isAssignableFrom(method.getReturnType())) {
                    cachedDropperGetter = method;
                    dropperGetterResolved = true;
                    return cachedDropperGetter;
                }
            } catch (NoSuchMethodException ignored) {
            }
        }

        dropperGetterResolved = true;
        return null;
    }

    private static Field getDropperField(ItemEntity item) {
        if (dropperFieldResolved) {
            return cachedDropperField;
        }

        for (String candidate : DROPPER_FIELD_CANDIDATES) {
            try {
                Field field = item.getClass().getDeclaredField(candidate);
                if (UUID.class.isAssignableFrom(field.getType())) {
                    field.setAccessible(true);
                    cachedDropperField = field;
                    dropperFieldResolved = true;
                    return cachedDropperField;
                }
            } catch (NoSuchFieldException ignored) {
            }
        }

        dropperFieldResolved = true;
        return null;
    }

    private Integer resolvePickupDelayTicks(ItemEntity item) {
        Method getter = getPickupDelayGetter(item);
        if (getter != null) {
            try {
                Object value = getter.invoke(item);
                if (value instanceof Number number) {
                    return number.intValue();
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }

        Field field = getPickupDelayField(item);
        if (field != null) {
            try {
                Object value = field.get(item);
                if (value instanceof Number number) {
                    return number.intValue();
                }
            } catch (IllegalAccessException ignored) {
            }
        }
        return null;
    }

    private static Method getPickupDelayGetter(ItemEntity item) {
        if (pickupDelayGetterResolved) {
            return cachedPickupDelayGetter;
        }

        for (String candidate : PICKUP_DELAY_GETTER_CANDIDATES) {
            try {
                Method method = item.getClass().getMethod(candidate);
                if (method.getParameterCount() == 0 && Number.class.isAssignableFrom(method.getReturnType())) {
                    cachedPickupDelayGetter = method;
                    pickupDelayGetterResolved = true;
                    return cachedPickupDelayGetter;
                }
            } catch (NoSuchMethodException ignored) {
            }
        }

        pickupDelayGetterResolved = true;
        return null;
    }

    private static Field getPickupDelayField(ItemEntity item) {
        if (pickupDelayFieldResolved) {
            return cachedPickupDelayField;
        }

        for (String candidate : PICKUP_DELAY_FIELD_CANDIDATES) {
            try {
                Field field = item.getClass().getDeclaredField(candidate);
                if (Number.class.isAssignableFrom(field.getType()) || field.getType() == int.class) {
                    field.setAccessible(true);
                    cachedPickupDelayField = field;
                    pickupDelayFieldResolved = true;
                    return cachedPickupDelayField;
                }
            } catch (NoSuchFieldException ignored) {
            }
        }

        pickupDelayFieldResolved = true;
        return null;
    }
}