package uk.co.duelmonster.minersadvantage.common.event;

import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;
import uk.co.duelmonster.minersadvantage.common.network.FeatureDispatchPacket;
import uk.co.duelmonster.minersadvantage.common.network.packets.PacketCaptivate;
import uk.co.duelmonster.minersadvantage.common.orchestration.FeatureDispatchContext;
import uk.co.duelmonster.minersadvantage.testutil.TestRuntimeAssumptions;

/**
 * PacketProcessSupportTest keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class PacketProcessSupportTest {
    @AfterEach
    /**
     * r es et ob se rv er exists so this path stays predictable and easier to debug when things get weird.
     */
    void resetObserver() {
        FeatureEventHandler.resetDispatchObserverForTesting();
    }

    @Test
    /**
     * d is ab le d pa ck et di sp at ch st ay s lo ck ed out exists so this path stays predictable and easier to debug when things get weird.
     */
    void doesNotDispatchWhenCaptivationIsDisabled() {
        Assumptions.assumeTrue(TestRuntimeAssumptions.canInitializeSoundEvents());
        MinersAdvantageCore core = new MinersAdvantageCore();
        core.bootstrap();
        core.handleComponentTogglePacket(new ComponentTogglePacket(FeatureId.CAPTIVATION, false));

        AtomicReference<FeatureDispatchContext> captured = new AtomicReference<>();
        FeatureEventHandler.setDispatchObserverForTesting(captured::set);

        PacketCaptivate.process(null, new PacketCaptivate());

        assertNull(captured.get());
        core.shutdown();
    }

    @Test
    /**
     * direct feature dispatch respects disabled state exists so this path stays predictable and easier to debug when things get weird.
     */
    void doesNotDispatchCoreFeaturePacketWhenCaptivationIsDisabled() {
        Assumptions.assumeTrue(TestRuntimeAssumptions.canInitializeSoundEvents());
        MinersAdvantageCore core = new MinersAdvantageCore();
        core.bootstrap();
        core.handleComponentTogglePacket(new ComponentTogglePacket(FeatureId.CAPTIVATION, false));

        AtomicReference<FeatureDispatchContext> captured = new AtomicReference<>();
        FeatureEventHandler.setDispatchObserverForTesting(captured::set);

        core.handleFeatureDispatchPacket(new FeatureDispatchPacket(FeatureId.CAPTIVATION, 1, 2, 3, "minecraft:diamond", "minecraft:stick"));

        assertNull(captured.get());
        core.shutdown();
    }
}