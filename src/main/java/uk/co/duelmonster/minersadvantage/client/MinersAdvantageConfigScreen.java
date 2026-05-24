package uk.co.duelmonster.minersadvantage.client;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import java.lang.reflect.Method;
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
        ConfigCategory.Builder clientCategory = ConfigCategory.createBuilder()
            .name(Component.literal("Client"));
        ConfigCategory.Builder generalCategory = ConfigCategory.createBuilder()
            .name(Component.literal("General"));
        ConfigCategory.Builder featuresCategory = ConfigCategory.createBuilder()
            .name(Component.literal("Features"));

        if (!gameplayEditable) {
            generalCategory.option(authorityNoticeOption());
            featuresCategory.option(authorityNoticeOption());
        }

        clientCategory
            .option(Option.<Boolean>createBuilder()
                .name(Component.literal("Disable Particle Effects"))
                .description(OptionDescription.of(Component.literal("Disable Miners Advantage particle effects on this client only.")))
                .binding(currentClientConfig.client().disableParticleEffects(), () -> mutable.disableParticleEffects, value -> mutable.disableParticleEffects = value)
                .controller(TickBoxControllerBuilder::create)
                .build());

        generalCategory
            .option(Option.<Boolean>createBuilder()
                .name(Component.literal("TPS Guard"))
                .description(OptionDescription.of(authorityAwareDescription("Reduces heavy processing when TPS drops.", gameplayEditable)))
                .binding(currentServerConfig.common().tpsGuard(), () -> mutable.tpsGuard, value -> mutable.tpsGuard = value)
                .available(gameplayEditable)
                .controller(TickBoxControllerBuilder::create)
                .build())
            .option(Option.<Boolean>createBuilder()
                .name(Component.literal("Gather Drops"))
                .description(OptionDescription.of(authorityAwareDescription("Allow automation to pull nearby drops.", gameplayEditable)))
                .binding(currentServerConfig.common().gatherDrops(), () -> mutable.gatherDrops, value -> mutable.gatherDrops = value)
                .available(gameplayEditable)
                .controller(TickBoxControllerBuilder::create)
                .build())
            .option(Option.<Boolean>createBuilder()
                .name(Component.literal("Auto Illuminate"))
                .description(OptionDescription.of(authorityAwareDescription("Permit automatic torch placement behavior.", gameplayEditable)))
                .binding(currentServerConfig.common().autoIlluminate(), () -> mutable.autoIlluminate, value -> mutable.autoIlluminate = value)
                .available(gameplayEditable)
                .controller(TickBoxControllerBuilder::create)
                .build())
            .option(Option.<Boolean>createBuilder()
                .name(Component.literal("Mine Veins"))
                .description(OptionDescription.of(authorityAwareDescription("Permit connected ore mining logic.", gameplayEditable)))
                .binding(currentServerConfig.common().mineVeins(), () -> mutable.mineVeins, value -> mutable.mineVeins = value)
                .available(gameplayEditable)
                .controller(TickBoxControllerBuilder::create)
                .build())
            .option(Option.<Integer>createBuilder()
                .name(Component.literal("Blocks Per Tick"))
                .description(OptionDescription.of(authorityAwareDescription("Maximum blocks processed each server tick.", gameplayEditable)))
                .binding(currentServerConfig.common().blocksPerTick(), () -> mutable.blocksPerTick, value -> mutable.blocksPerTick = value)
                .available(gameplayEditable)
                .controller(option -> IntegerSliderControllerBuilder.create(option).range(1, 8).step(1))
                .build())
            .option(Option.<Integer>createBuilder()
                .name(Component.literal("Block Limit"))
                .description(OptionDescription.of(authorityAwareDescription("Hard cap for an operation size.", gameplayEditable)))
                .binding(currentServerConfig.common().blockLimit(), () -> mutable.blockLimit, value -> mutable.blockLimit = value)
                .available(gameplayEditable)
                .controller(option -> IntegerSliderControllerBuilder.create(option).range(1, 256).step(1))
                .build());

        featuresCategory
            .option(featureToggle("Captivation", () -> mutable.captivationEnabled, value -> mutable.captivationEnabled = value, currentServerConfig.captivation().enabled(), gameplayEditable))
            .option(featureToggle("Cropination", () -> mutable.cropinationEnabled, value -> mutable.cropinationEnabled = value, currentServerConfig.cropination().enabled(), gameplayEditable))
            .option(featureToggle("Cultivation", () -> mutable.cultivationEnabled, value -> mutable.cultivationEnabled = value, currentServerConfig.cultivation().enabled(), gameplayEditable))
            .option(featureToggle("Excavation", () -> mutable.excavationEnabled, value -> mutable.excavationEnabled = value, currentServerConfig.excavation().enabled(), gameplayEditable))
            .option(featureToggle("Pathanation", () -> mutable.pathanationEnabled, value -> mutable.pathanationEnabled = value, currentServerConfig.pathanation().enabled(), gameplayEditable))
            .option(featureToggle("Illumination", () -> mutable.illuminationEnabled, value -> mutable.illuminationEnabled = value, currentServerConfig.illumination().enabled(), gameplayEditable))
            .option(featureToggle("Lumbination", () -> mutable.lumbinationEnabled, value -> mutable.lumbinationEnabled = value, currentServerConfig.lumbination().enabled(), gameplayEditable))
            .option(featureToggle("Shaftanation", () -> mutable.shaftanationEnabled, value -> mutable.shaftanationEnabled = value, currentServerConfig.shaftanation().enabled(), gameplayEditable))
            .option(featureToggle("Substitution", () -> mutable.substitutionEnabled, value -> mutable.substitutionEnabled = value, currentServerConfig.substitution().enabled(), gameplayEditable))
            .option(featureToggle("Veination", () -> mutable.veinationEnabled, value -> mutable.veinationEnabled = value, currentServerConfig.veination().enabled(), gameplayEditable))
            .option(featureToggle("Ventilation", () -> mutable.ventilationEnabled, value -> mutable.ventilationEnabled = value, currentServerConfig.ventilation().enabled(), gameplayEditable));

        return YetAnotherConfigLib.createBuilder()
            .title(Component.literal("Miners Advantage"))
            .category(clientCategory.build())
            .category(generalCategory.build())
            .category(featuresCategory.build())
            .save(() -> {
                currentClientConfig = mutable.toClientRootConfig(currentClientConfig);
                MAConfig_Base.setClientRootConfig(currentClientConfig);
                if (gameplayEditable) {
                    currentServerConfig = mutable.toServerRootConfig(currentServerConfig);
                    MAConfig_Base.setServerRootConfig(currentServerConfig);
                } else {
                    currentServerConfig = MAConfig_Base.getServerRootConfig();
                }
                sendClientSync(currentClientConfig);
            })
            .build()
            .generateScreen(parent);
    }

    private static Option<Boolean> featureToggle(
        String label,
        java.util.function.Supplier<Boolean> getter,
        java.util.function.Consumer<Boolean> setter,
        boolean defaultValue,
        boolean available
    ) {
        return Option.<Boolean>createBuilder()
            .name(Component.literal(label))
            .description(OptionDescription.of(authorityAwareDescription("Enable or disable " + label + ".", available)))
            .binding(defaultValue, getter, setter)
            .available(available)
            .controller(TickBoxControllerBuilder::create)
            .build();
    }

    private static Option<Boolean> authorityNoticeOption() {
        return Option.<Boolean>createBuilder()
            .name(Component.literal("Gameplay Authority"))
            .description(OptionDescription.of(Component.literal("Gameplay settings are controlled by the server while connected to remote multiplayer.")))
            .binding(true, () -> true, value -> { })
            .available(false)
            .controller(TickBoxControllerBuilder::create)
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
