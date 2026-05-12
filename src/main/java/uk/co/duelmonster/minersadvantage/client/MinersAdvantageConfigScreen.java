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
import uk.co.duelmonster.minersadvantage.common.config.CommonConfig;
import uk.co.duelmonster.minersadvantage.common.config.ServerOverridesConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.common.network.PlayerStateSyncPacket;

/**
 * MinersAdvantageConfigScreen keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class MinersAdvantageConfigScreen {
    private static SyncedClientConfig currentConfig = SyncedClientConfig.defaults();

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
        MutableConfig mutable = new MutableConfig(currentConfig);

        return YetAnotherConfigLib.createBuilder()
            .title(Component.literal("Miners Advantage"))
            .category(ConfigCategory.createBuilder()
                .name(Component.literal("General"))
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("TPS Guard"))
                    .description(OptionDescription.of(Component.literal("Reduces heavy processing when TPS drops.")))
                    .binding(currentConfig.common().tpsGuard(), () -> mutable.tpsGuard, value -> mutable.tpsGuard = value)
                    .controller(TickBoxControllerBuilder::create)
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("Gather Drops"))
                    .description(OptionDescription.of(Component.literal("Allow automation to pull nearby drops.")))
                    .binding(currentConfig.common().gatherDrops(), () -> mutable.gatherDrops, value -> mutable.gatherDrops = value)
                    .controller(TickBoxControllerBuilder::create)
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("Auto Illuminate"))
                    .description(OptionDescription.of(Component.literal("Permit automatic torch placement behavior.")))
                    .binding(currentConfig.common().autoIlluminate(), () -> mutable.autoIlluminate, value -> mutable.autoIlluminate = value)
                    .controller(TickBoxControllerBuilder::create)
                    .build())
                .option(Option.<Boolean>createBuilder()
                    .name(Component.literal("Mine Veins"))
                    .description(OptionDescription.of(Component.literal("Permit connected ore mining logic.")))
                    .binding(currentConfig.common().mineVeins(), () -> mutable.mineVeins, value -> mutable.mineVeins = value)
                    .controller(TickBoxControllerBuilder::create)
                    .build())
                .option(Option.<Integer>createBuilder()
                    .name(Component.literal("Blocks Per Tick"))
                    .description(OptionDescription.of(Component.literal("Maximum blocks processed each server tick.")))
                    .binding(currentConfig.common().blocksPerTick(), () -> mutable.blocksPerTick, value -> mutable.blocksPerTick = value)
                    .controller(option -> IntegerSliderControllerBuilder.create(option).range(1, 8).step(1))
                    .build())
                .option(Option.<Integer>createBuilder()
                    .name(Component.literal("Block Limit"))
                    .description(OptionDescription.of(Component.literal("Hard cap for an operation size.")))
                    .binding(currentConfig.common().blockLimit(), () -> mutable.blockLimit, value -> mutable.blockLimit = value)
                    .controller(option -> IntegerSliderControllerBuilder.create(option).range(1, 256).step(1))
                    .build())
                .build())
            .category(ConfigCategory.createBuilder()
                .name(Component.literal("Features"))
                .option(featureToggle("Captivation", () -> mutable.captivationEnabled, value -> mutable.captivationEnabled = value, currentConfig.captivation().enabled()))
                .option(featureToggle("Cropination", () -> mutable.cropinationEnabled, value -> mutable.cropinationEnabled = value, currentConfig.cropination().enabled()))
                .option(featureToggle("Cultivation", () -> mutable.cultivationEnabled, value -> mutable.cultivationEnabled = value, currentConfig.cultivation().enabled()))
                .option(featureToggle("Excavation", () -> mutable.excavationEnabled, value -> mutable.excavationEnabled = value, currentConfig.excavation().enabled()))
                .option(featureToggle("Pathanation", () -> mutable.pathanationEnabled, value -> mutable.pathanationEnabled = value, currentConfig.pathanation().enabled()))
                .option(featureToggle("Illumination", () -> mutable.illuminationEnabled, value -> mutable.illuminationEnabled = value, currentConfig.illumination().enabled()))
                .option(featureToggle("Lumbination", () -> mutable.lumbinationEnabled, value -> mutable.lumbinationEnabled = value, currentConfig.lumbination().enabled()))
                .option(featureToggle("Shaftanation", () -> mutable.shaftanationEnabled, value -> mutable.shaftanationEnabled = value, currentConfig.shaftanation().enabled()))
                .option(featureToggle("Substitution", () -> mutable.substitutionEnabled, value -> mutable.substitutionEnabled = value, currentConfig.substitution().enabled()))
                .option(featureToggle("Veination", () -> mutable.veinationEnabled, value -> mutable.veinationEnabled = value, currentConfig.veination().enabled()))
                .option(featureToggle("Ventilation", () -> mutable.ventilationEnabled, value -> mutable.ventilationEnabled = value, currentConfig.ventilation().enabled()))
                .build())
            .save(() -> {
                currentConfig = mutable.toSyncedClientConfig(currentConfig);
                sendClientSync(currentConfig);
            })
            .build()
            .generateScreen(parent);
    }

    private static Option<Boolean> featureToggle(
        String label,
        java.util.function.Supplier<Boolean> getter,
        java.util.function.Consumer<Boolean> setter,
        boolean defaultValue
    ) {
        return Option.<Boolean>createBuilder()
            .name(Component.literal(label))
            .description(OptionDescription.of(Component.literal("Enable or disable " + label + ".")))
            .binding(defaultValue, getter, setter)
            .controller(TickBoxControllerBuilder::create)
            .build();
    }

    /**
     * sendClientSync exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    private static void sendClientSync(SyncedClientConfig updatedConfig) {
        Minecraft minecraft = Minecraft.getInstance();
        long playerId = minecraft.player == null ? 0L : minecraft.player.getUUID().getLeastSignificantBits();
        PlayerStateSyncPacket packet = new PlayerStateSyncPacket(
            playerId,
            updatedConfig,
            SyncedClientConfig.defaults(),
            new ServerOverridesConfig()
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
        private MutableConfig(SyncedClientConfig config) {
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

        /**
         * toSyncedClientConfig exists so this code path does one job clearly instead of spreading chaos across callers.
         * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
         */
        private SyncedClientConfig toSyncedClientConfig(SyncedClientConfig baseline) {
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

            return new SyncedClientConfig(
                baseline.client(),
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
                    baseline.veination().ores()
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
