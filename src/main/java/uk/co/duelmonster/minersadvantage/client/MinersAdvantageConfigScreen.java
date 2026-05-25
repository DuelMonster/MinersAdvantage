package uk.co.duelmonster.minersadvantage.client;

import java.lang.reflect.Method;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import uk.co.duelmonster.minersadvantage.common.config.ClientConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAClientRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAConfig_Base;
import uk.co.duelmonster.minersadvantage.common.network.PlayerStateSyncPacket;

/**
 * MinersAdvantageConfigScreen keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class MinersAdvantageConfigScreen {
    private static MAClientRootConfig currentClientConfig = MAConfig_Base.getClientRootConfig();
    private static MAServerRootConfig currentServerConfig = MAConfig_Base.getServerRootConfig();

    /**
     * MinersAdvantageConfigScreen exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private MinersAdvantageConfigScreen() {
    }

    /**
     * create exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public static Screen create(Screen parent) {
        MutableConfig mutable = new MutableConfig(currentClientConfig, currentServerConfig);
        boolean gameplayEditable = isGameplayEditable();
        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.literal("Miners Advantage"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory clientCategory = builder.getOrCreateCategory(Component.literal("Client"));
        ConfigCategory generalCategory = builder.getOrCreateCategory(Component.literal("General"));
        ConfigCategory featuresCategory = builder.getOrCreateCategory(Component.literal("Features"));

        if (!gameplayEditable) {
            generalCategory.addEntry(authorityNoticeEntry(entryBuilder));
            featuresCategory.addEntry(authorityNoticeEntry(entryBuilder));
        }

        clientCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.literal("Disable Particle Effects"),
                mutable.disableParticleEffects
            )
            .setDefaultValue(currentClientConfig.client().disableParticleEffects())
            .setTooltip(Component.literal("Disable Miners Advantage particle effects on this client only."))
            .setSaveConsumer(value -> mutable.disableParticleEffects = value)
            .build());

        addGameplayBoolean(generalCategory, entryBuilder, "TPS Guard", "Reduces heavy processing when TPS drops.", mutable.tpsGuard, currentServerConfig.common().tpsGuard(), gameplayEditable, value -> mutable.tpsGuard = value);
        addGameplayBoolean(generalCategory, entryBuilder, "Gather Drops", "Allow automation to pull nearby drops.", mutable.gatherDrops, currentServerConfig.common().gatherDrops(), gameplayEditable, value -> mutable.gatherDrops = value);
        addGameplayBoolean(generalCategory, entryBuilder, "Auto Illuminate", "Permit automatic torch placement behavior.", mutable.autoIlluminate, currentServerConfig.common().autoIlluminate(), gameplayEditable, value -> mutable.autoIlluminate = value);
        addGameplayBoolean(generalCategory, entryBuilder, "Mine Veins", "Permit connected ore mining logic.", mutable.mineVeins, currentServerConfig.common().mineVeins(), gameplayEditable, value -> mutable.mineVeins = value);
        addGameplayInt(generalCategory, entryBuilder, "Blocks Per Tick", "Maximum blocks processed each server tick.", mutable.blocksPerTick, currentServerConfig.common().blocksPerTick(), 1, 8, gameplayEditable, value -> mutable.blocksPerTick = value);
        addGameplayInt(generalCategory, entryBuilder, "Block Limit", "Hard cap for an operation size.", mutable.blockLimit, currentServerConfig.common().blockLimit(), 1, 256, gameplayEditable, value -> mutable.blockLimit = value);

        addFeatureToggle(featuresCategory, entryBuilder, "Captivation", mutable.captivationEnabled, currentServerConfig.captivation().enabled(), gameplayEditable, value -> mutable.captivationEnabled = value);
        addFeatureToggle(featuresCategory, entryBuilder, "Cropination", mutable.cropinationEnabled, currentServerConfig.cropination().enabled(), gameplayEditable, value -> mutable.cropinationEnabled = value);
        addFeatureToggle(featuresCategory, entryBuilder, "Cultivation", mutable.cultivationEnabled, currentServerConfig.cultivation().enabled(), gameplayEditable, value -> mutable.cultivationEnabled = value);
        addFeatureToggle(featuresCategory, entryBuilder, "Excavation", mutable.excavationEnabled, currentServerConfig.excavation().enabled(), gameplayEditable, value -> mutable.excavationEnabled = value);
        addFeatureToggle(featuresCategory, entryBuilder, "Pathanation", mutable.pathanationEnabled, currentServerConfig.pathanation().enabled(), gameplayEditable, value -> mutable.pathanationEnabled = value);
        addFeatureToggle(featuresCategory, entryBuilder, "Illumination", mutable.illuminationEnabled, currentServerConfig.illumination().enabled(), gameplayEditable, value -> mutable.illuminationEnabled = value);
        addFeatureToggle(featuresCategory, entryBuilder, "Lumbination", mutable.lumbinationEnabled, currentServerConfig.lumbination().enabled(), gameplayEditable, value -> mutable.lumbinationEnabled = value);
        addFeatureToggle(featuresCategory, entryBuilder, "Shaftanation", mutable.shaftanationEnabled, currentServerConfig.shaftanation().enabled(), gameplayEditable, value -> mutable.shaftanationEnabled = value);
        addFeatureToggle(featuresCategory, entryBuilder, "Substitution", mutable.substitutionEnabled, currentServerConfig.substitution().enabled(), gameplayEditable, value -> mutable.substitutionEnabled = value);
        addFeatureToggle(featuresCategory, entryBuilder, "Veination", mutable.veinationEnabled, currentServerConfig.veination().enabled(), gameplayEditable, value -> mutable.veinationEnabled = value);
        addFeatureToggle(featuresCategory, entryBuilder, "Ventilation", mutable.ventilationEnabled, currentServerConfig.ventilation().enabled(), gameplayEditable, value -> mutable.ventilationEnabled = value);

        builder.setSavingRunnable(() -> {
                currentClientConfig = mutable.toClientRootConfig(currentClientConfig);
                MAConfig_Base.setClientRootConfig(currentClientConfig);
                if (gameplayEditable) {
                    currentServerConfig = mutable.toServerRootConfig(currentServerConfig);
                    MAConfig_Base.setServerRootConfig(currentServerConfig);
                } else {
                    currentServerConfig = MAConfig_Base.getServerRootConfig();
                }
                sendClientSync(currentClientConfig);
            });

        return builder.build();
    }

    private static void addFeatureToggle(
        ConfigCategory category,
        ConfigEntryBuilder entryBuilder,
        String label,
        boolean currentValue,
        boolean defaultValue,
        boolean editable,
        java.util.function.Consumer<Boolean> consumer
    ) {
        if (!editable) {
            category.addEntry(entryBuilder.startTextDescription(
                    Component.literal(label + ": " + (currentValue ? "Enabled" : "Disabled"))
                )
                .build());
            return;
        }

        category.addEntry(entryBuilder.startBooleanToggle(Component.literal(label), currentValue)
            .setDefaultValue(defaultValue)
            .setTooltip(authorityAwareDescription("Enable or disable " + label + ".", editable))
            .setSaveConsumer(consumer)
            .build());
    }

    private static void addGameplayBoolean(
        ConfigCategory category,
        ConfigEntryBuilder entryBuilder,
        String label,
        String description,
        boolean currentValue,
        boolean defaultValue,
        boolean editable,
        java.util.function.Consumer<Boolean> consumer
    ) {
        if (!editable) {
            category.addEntry(entryBuilder.startTextDescription(
                    Component.literal(label + ": " + (currentValue ? "Enabled" : "Disabled"))
                )
                .build());
            return;
        }

        category.addEntry(entryBuilder.startBooleanToggle(Component.literal(label), currentValue)
            .setDefaultValue(defaultValue)
            .setTooltip(authorityAwareDescription(description, editable))
            .setSaveConsumer(consumer)
            .build());
    }

    private static void addGameplayInt(
        ConfigCategory category,
        ConfigEntryBuilder entryBuilder,
        String label,
        String description,
        int currentValue,
        int defaultValue,
        int min,
        int max,
        boolean editable,
        java.util.function.Consumer<Integer> consumer
    ) {
        if (!editable) {
            category.addEntry(entryBuilder.startTextDescription(
                    Component.literal(label + ": " + currentValue)
                )
                .build());
            return;
        }

        category.addEntry(entryBuilder.startIntField(Component.literal(label), currentValue)
            .setDefaultValue(defaultValue)
            .setMin(min)
            .setMax(max)
            .setTooltip(authorityAwareDescription(description, editable))
            .setSaveConsumer(consumer)
            .build());
    }

    private static me.shedaniel.clothconfig2.api.AbstractConfigListEntry<?> authorityNoticeEntry(ConfigEntryBuilder entryBuilder) {
        return entryBuilder.startTextDescription(Component.literal("Gameplay settings are controlled by the server while connected to remote multiplayer."))
            .build();
    }

    private static Component authorityAwareDescription(String baseDescription, boolean gameplayEditable) {
        if (gameplayEditable) {
            return Component.literal(baseDescription);
        }
        return Component.literal(baseDescription + " Controlled by the server in multiplayer.");
    }

    private static boolean isGameplayEditable() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.getConnection() == null) {
            return true;
        }
        return minecraft.hasSingleplayerServer();
    }

    /**
     * sendClientSync exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private static void sendClientSync(MAClientRootConfig updatedConfig) {
        Minecraft minecraft = Minecraft.getInstance();
        long playerId = minecraft.player == null ? 0L : minecraft.player.getUUID().getLeastSignificantBits();
        PlayerStateSyncPacket packet = new PlayerStateSyncPacket(
            playerId,
            updatedConfig,
            MAServerRootConfig.defaults()
        );

        try {
            Class<?> fabricNetworking = Class.forName("net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking");
            Method sendMethod = fabricNetworking.getMethod("send", Object.class);
            sendMethod.invoke(null, packet);
            return;
        } catch (ReflectiveOperationException ignored) {
            // Why this exists: Try NeoForge path. (future-you will thank present-you).
        }

        try {
            Class<?> neoDistributor = Class.forName("net.neoforged.neoforge.client.network.ClientPacketDistributor");
            Method sendToServer = neoDistributor.getMethod("sendToServer", Object.class);
            sendToServer.invoke(null, packet);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Unable to send config sync packet", exception);
        }
    }

    /**
     * MutableConfig keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    private static final class MutableConfig {
        private boolean disableParticleEffects;

        private boolean tpsGuard;
        private boolean gatherDrops;
        private boolean autoIlluminate;
        private boolean mineVeins;
        private int blocksPerTick;
        private int blockLimit;

        private boolean captivationEnabled;
        private boolean cropinationEnabled;
        private boolean cultivationEnabled;
        private boolean excavationEnabled;
        private boolean pathanationEnabled;
        private boolean illuminationEnabled;
        private boolean lumbinationEnabled;
        private boolean shaftanationEnabled;
        private boolean substitutionEnabled;
        private boolean veinationEnabled;
        private boolean ventilationEnabled;

        /**
         * MutableConfig exists so this code path does one job clearly instead of spreading chaos across callers.
         * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
         */
        private MutableConfig(MAClientRootConfig clientConfig, MAServerRootConfig serverConfig) {
            this.disableParticleEffects = clientConfig.client().disableParticleEffects();

            MAServerRootConfig config = serverConfig;
            this.tpsGuard = config.common().tpsGuard();
            this.gatherDrops = config.common().gatherDrops();
            this.autoIlluminate = config.common().autoIlluminate();
            this.mineVeins = config.common().mineVeins();
            this.blocksPerTick = config.common().blocksPerTick();
            this.blockLimit = config.common().blockLimit();

            this.captivationEnabled = config.captivation().enabled();
            this.cropinationEnabled = config.cropination().enabled();
            this.cultivationEnabled = config.cultivation().enabled();
            this.excavationEnabled = config.excavation().enabled();
            this.pathanationEnabled = config.pathanation().enabled();
            this.illuminationEnabled = config.illumination().enabled();
            this.lumbinationEnabled = config.lumbination().enabled();
            this.shaftanationEnabled = config.shaftanation().enabled();
            this.substitutionEnabled = config.substitution().enabled();
            this.veinationEnabled = config.veination().enabled();
            this.ventilationEnabled = config.ventilation().enabled();
        }

        private MAClientRootConfig toClientRootConfig(MAClientRootConfig baseline) {
            return new MAClientRootConfig(
                new ClientConfig(disableParticleEffects)
            );
        }

        /**
         * toServerRootConfig exists so this code path does one job clearly instead of spreading chaos across callers.
         * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
         */
        private MAServerRootConfig toServerRootConfig(MAServerRootConfig baseline) {
            CommonConfig updatedCommon = new CommonConfig(
                tpsGuard,
                gatherDrops,
                autoIlluminate,
                mineVeins,
                blocksPerTick,
                baseline.common().enableTickDelay(),
                baseline.common().tickDelay(),
                baseline.common().blockRadius(),
                blockLimit
            );

            return new MAServerRootConfig(
                updatedCommon,
                new uk.co.duelmonster.minersadvantage.common.config.CaptivationConfig(
                    captivationEnabled,
                    baseline.captivation().allowInGUI(),
                    baseline.captivation().radiusHorizontal(),
                    baseline.captivation().radiusVertical(),
                    baseline.captivation().isWhitelist(),
                    baseline.captivation().unconditionalBlacklist(),
                    baseline.captivation().blacklist()
                ),
                new uk.co.duelmonster.minersadvantage.common.config.CropinationConfig(
                    cropinationEnabled,
                    baseline.cropination().harvestSeeds()
                ),
                new uk.co.duelmonster.minersadvantage.common.config.CultivationConfig(
                    cultivationEnabled,
                    baseline.cultivation().hydrationDistance()
                ),
                new uk.co.duelmonster.minersadvantage.common.config.ExcavationConfig(
                    excavationEnabled,
                    baseline.excavation().radiusHorizontal(),
                    baseline.excavation().radiusVertical(),
                    baseline.excavation().processesPerTick(),
                    baseline.excavation().toggleMode(),
                    baseline.excavation().ignoreBlockVariants(),
                    baseline.excavation().isBlockWhitelist(),
                    baseline.excavation().blockBlacklist()
                ),
                new uk.co.duelmonster.minersadvantage.common.config.PathanationConfig(
                    pathanationEnabled,
                    baseline.pathanation().targetBlockRange(),
                    baseline.pathanation().pathWidth()
                ),
                new uk.co.duelmonster.minersadvantage.common.config.IlluminationConfig(
                    illuminationEnabled,
                    baseline.illumination().radiusHorizontal(),
                    baseline.illumination().radiusVertical(),
                    baseline.illumination().lowestLightLevel(),
                    baseline.illumination().useBlockLight()
                ),
                new uk.co.duelmonster.minersadvantage.common.config.LumbinationConfig(
                    lumbinationEnabled,
                    baseline.lumbination().maxTrunkRange(),
                    baseline.lumbination().maxLeafRange(),
                    baseline.lumbination().processesPerTick(),
                    baseline.lumbination().chopTreeBelow(),
                    baseline.lumbination().destroyLeaves(),
                    baseline.lumbination().leavesAffectDurability(),
                    baseline.lumbination().replantSaplings(),
                    baseline.lumbination().useShearsOnLeaves(),
                    baseline.lumbination().logs(),
                    baseline.lumbination().leaves(),
                    baseline.lumbination().axes()
                ),
                new uk.co.duelmonster.minersadvantage.common.config.ShaftanationConfig(
                    shaftanationEnabled,
                    baseline.shaftanation().maxDepth(),
                    baseline.shaftanation().processesPerTick(),
                    baseline.shaftanation().shaftWidth(),
                    baseline.shaftanation().shaftHeight(),
                    baseline.shaftanation().torchPlacement()
                ),
                new uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig(
                    substitutionEnabled,
                    baseline.substitution().allowMending(),
                    baseline.substitution().prioritizeSilkTouch(),
                    baseline.substitution().switchBack(),
                    baseline.substitution().favourFortune(),
                    baseline.substitution().ignoreIfValidTool(),
                    baseline.substitution().ignorePassiveMobs(),
                    baseline.substitution().blacklist()
                ),
                new uk.co.duelmonster.minersadvantage.common.config.VeinationConfig(
                    veinationEnabled,
                    baseline.veination().maxVeinDistance(),
                    baseline.veination().ores(),
                    baseline.veination().oreHarvestWithoutSneak(),
                    baseline.veination().dropOresAtFirstBrokenBlock(),
                    baseline.veination().increaseHarvestingTimePerOre(),
                    baseline.veination().increasedHarvestingTimePerOreModifier(),
                    baseline.veination().pickaxeBlacklist()
                ),
                new uk.co.duelmonster.minersadvantage.common.config.VentilationConfig(
                    ventilationEnabled,
                    baseline.ventilation().radiusHorizontal(),
                    baseline.ventilation().radiusVertical(),
                    baseline.ventilation().processesPerTick(),
                    baseline.ventilation().placeLadders()
                )
            );
        }
    }
}
