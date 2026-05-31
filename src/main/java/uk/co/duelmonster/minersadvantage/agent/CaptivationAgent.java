package uk.co.duelmonster.minersadvantage.agent;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ExperienceOrb;
import uk.co.duelmonster.minersadvantage.common.config.CaptivationConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.services.captivation.CaptivationCoreService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Captivation agent that plays vacuum cleaner for drops and XP around the player.
 * It runs for a short burst, respects GUI rules, and tries not to yoink freshly dropped player items.
 */
public class CaptivationAgent extends Agent {
    private static final double NO_PULL_RADIUS_SQUARED = 2.0D;
    private static final int PLAYER_DROP_COOLDOWN_TICKS = 160;
    private static final int PLAYER_DROP_PICKUP_DELAY_TICKS = 20;
    private static final String[] DROPPER_GETTER_CANDIDATES = {"getThrower", "getThrowerId", "getOwner", "getOwnerId", "getTarget"};
    private static final String[] DROPPER_FIELD_CANDIDATES = {"thrower", "throwerId", "owner", "ownerId", "target"};
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

    /**
     * Convenience constructor used by call sites that only provide one radius value.
     */
    public CaptivationAgent(ServerPlayer player, double radius) {
        this(player, new CaptivationConfig(true, true, (int) Math.ceil(radius), (int) Math.ceil(radius), false, false));
    }

    /**
     * Build captivation runtime state from config with defensive defaults.
     */
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

    /**
     * Per-tick magnet logic: pull valid item entities and XP toward the player until budget expires.
     */
    @Override
    /**
     * t ic k exists so this path stays predictable and easier to debug when things get weird.
     */
    public boolean tick() {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (player.isRemoved() || !player.isAlive()) {
            return finish("player unavailable");
        }

        // Respect GUI lock unless config explicitly allows magnet behavior while menus are open.
        if (!allowInGui && player.containerMenu != player.inventoryMenu) {
            ticks++;
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (ticks >= maxTicks) {
                return finish("max tick budget reached while gui open");
            }
            return false;
        }

        Level world = player.level();
        AABB box = player.getBoundingBox().inflate(radiusHorizontal, radiusVertical, radiusHorizontal);
        List<Entity> entities = world.getEntities(player, box, e -> e instanceof ItemEntity || e instanceof ExperienceOrb);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (Entity e : entities) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            if (e instanceof ItemEntity item) {
                String itemId = BuiltInRegistries.ITEM.getKey(item.getItem().getItem()).toString();
                // Core service decides if this item is allowed by blacklist/whitelist policy.
                if (!service.canCaptureItem(itemId, false)) {
                    continue;
                }
                // Freshly dropped player items get a cooldown window so we don't insta-snatch their loot.
                if (isRecentDropByPlayer(item)) {
                    continue;
                }
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (item.position().distanceToSqr(player.position()) <= NO_PULL_RADIUS_SQUARED) {
                    continue;
                }
                double dx = player.getX() - item.getX();
                double dy = player.getY() + 1.0 - item.getY();
                double dz = player.getZ() - item.getZ();
                double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (dist > 0.1) {
                    double speed = 0.3;
                    pullTowardPlayer(item, dx, dy, dz, dist, speed);
                }
            } else if (e instanceof ExperienceOrb orb) {
                // Close orbs can just touch immediately for smoother pickup feel.
                if (orb.position().distanceToSqr(player.position()) <= NO_PULL_RADIUS_SQUARED) {
                    orb.playerTouch(player);
                    continue;
                }
                double dx = player.getX() - orb.getX();
                double dy = player.getY() + 1.0 - orb.getY();
                double dz = player.getZ() - orb.getZ();
                double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (dist > 0.1) {
                    double speed = 0.3;
                    pullTowardPlayer(orb, dx, dy, dz, dist, speed);
                }
            }
        }
        ticks++;
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (ticks >= maxTicks) {
            return finish("max tick budget reached");
        }
        return false;
    }

    /**
     * Determine whether an item should be treated as a recent player drop and therefore ignored temporarily.
     */
    private boolean isRecentDropByPlayer(ItemEntity item) {
        UUID dropper = resolveDropperUuid(item);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (dropper != null) {
            return dropper.equals(player.getUUID()) && item.tickCount < PLAYER_DROP_COOLDOWN_TICKS;
        }
        Integer pickupDelay = resolvePickupDelayTicks(item);
        return pickupDelay != null
            && pickupDelay >= PLAYER_DROP_PICKUP_DELAY_TICKS
            && item.tickCount < PLAYER_DROP_COOLDOWN_TICKS;
    }

    /**
     * Resolve dropper UUID through getter-first, field-second reflection strategy for mapping compatibility.
     */
    private UUID resolveDropperUuid(ItemEntity item) {
        Method getter = getDropperGetter(item);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (getter != null) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            try {
                Object value = getter.invoke(item);
                UUID id = extractDropperUuid(value);
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (id != null) {
                    return id;
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }

        Field field = getDropperField(item);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (field != null) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            try {
                Object value = field.get(item);
                UUID id = extractDropperUuid(value);
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (id != null) {
                    return id;
                }
            } catch (IllegalAccessException ignored) {
            }
        }

        return null;
    }

    /**
     * Resolve and cache dropper getter method once, then reuse for future entity checks.
     */
    private static Method getDropperGetter(ItemEntity item) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (dropperGetterResolved) {
            return cachedDropperGetter;
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (String candidate : DROPPER_GETTER_CANDIDATES) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            try {
                Method method = item.getClass().getMethod(candidate);
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (method.getParameterCount() == 0 && canContainDropperUuid(method.getReturnType())) {
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

    /**
     * Resolve and cache dropper backing field once when no compatible getter exists.
     */
    private static Field getDropperField(ItemEntity item) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (dropperFieldResolved) {
            return cachedDropperField;
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (String candidate : DROPPER_FIELD_CANDIDATES) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            try {
                Field field = item.getClass().getDeclaredField(candidate);
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (canContainDropperUuid(field.getType())) {
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

    /**
     * Resolve item pickup delay via reflection so cooldown logic survives mapping/runtime differences.
     */
    private Integer resolvePickupDelayTicks(ItemEntity item) {
        Method getter = getPickupDelayGetter(item);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (getter != null) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            try {
                Object value = getter.invoke(item);
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (value instanceof Number number) {
                    return number.intValue();
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }

        Field field = getPickupDelayField(item);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (field != null) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            try {
                Object value = field.get(item);
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (value instanceof Number number) {
                    return number.intValue();
                }
            } catch (IllegalAccessException ignored) {
            }
        }
        return null;
    }

    /**
     * Resolve and cache pickup-delay getter method.
     */
    private static Method getPickupDelayGetter(ItemEntity item) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (pickupDelayGetterResolved) {
            return cachedPickupDelayGetter;
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (String candidate : PICKUP_DELAY_GETTER_CANDIDATES) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            try {
                Method method = item.getClass().getMethod(candidate);
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
                if (method.getParameterCount() == 0
                    && (Number.class.isAssignableFrom(method.getReturnType()) || method.getReturnType() == int.class)) {
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

    /**
     * Resolve and cache pickup-delay field when getter path is unavailable.
     */
    private static Field getPickupDelayField(ItemEntity item) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (pickupDelayFieldResolved) {
            return cachedPickupDelayField;
        }

        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        for (String candidate : PICKUP_DELAY_FIELD_CANDIDATES) {
            // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
            try {
                Field field = item.getClass().getDeclaredField(candidate);
                // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
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

    /**
     * Apply normalized velocity toward player center with configured pull speed.
     */
    private void pullTowardPlayer(Entity entity, double dx, double dy, double dz, double dist, double speed) {
        entity.setDeltaMovement(dx / dist * speed, dy / dist * speed, dz / dist * speed);
    }

    /**
     * Accept UUID or Entity return types when resolving dropper identity.
     */
    private static boolean canContainDropperUuid(Class<?> type) {
        return UUID.class.isAssignableFrom(type) || Entity.class.isAssignableFrom(type);
    }

    /**
     * Convert reflected dropper value into UUID when possible.
     */
    private static UUID extractDropperUuid(Object value) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (value instanceof UUID id) {
            return id;
        }
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (value instanceof Entity entity) {
            return entity.getUUID();
        }
        return null;
    }
}
