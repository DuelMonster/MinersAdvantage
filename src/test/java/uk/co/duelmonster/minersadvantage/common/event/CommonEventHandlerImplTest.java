package uk.co.duelmonster.minersadvantage.common.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.MinersAdvantageCore;
import uk.co.duelmonster.minersadvantage.common.feature.FeatureId;
import uk.co.duelmonster.minersadvantage.common.network.ComponentTogglePacket;
import uk.co.duelmonster.minersadvantage.common.orchestration.FeatureDispatchContext;
import uk.co.duelmonster.minersadvantage.testutil.TestRuntimeAssumptions;

/**
 * CommonEventHandlerImplTest keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class CommonEventHandlerImplTest {
  @AfterEach
  /**
   * c le ar ob se rv er exists so this path stays predictable and easier to debug when things get weird.
   */
  void clearObserver() {
    FeatureEventHandler.resetDispatchObserverForTesting();
  }

  @Test
  /**
   * r ou te sp ic ka xe st on et os ha ft an at io n exists so this path stays predictable and easier to debug when things get weird.
   */
  void routesPickaxeStoneToShaftanation() {
    Assumptions.assumeTrue(TestRuntimeAssumptions.canInitializeSoundEvents());
    MinersAdvantageCore core = new MinersAdvantageCore();
    core.bootstrap();
    CommonEventHandlerImpl handler = new CommonEventHandlerImpl(core);
    AtomicReference<FeatureDispatchContext> captured = new AtomicReference<>();
    FeatureEventHandler.setDispatchObserverForTesting(captured::set);

    handler.onPickaxeUse(1, 2, 3, "minecraft:stone");

    assertEquals(FeatureId.SHAFTANATION, captured.get().feature());
    core.shutdown();
  }

  @Test
  /**
   * r ou te sh oe cr op to cr op in at io n exists so this path stays predictable and easier to debug when things get weird.
   */
  void routesHoeCropToCropination() {
    Assumptions.assumeTrue(TestRuntimeAssumptions.canInitializeSoundEvents());
    MinersAdvantageCore core = new MinersAdvantageCore();
    core.bootstrap();
    CommonEventHandlerImpl handler = new CommonEventHandlerImpl(core);
    AtomicReference<FeatureDispatchContext> captured = new AtomicReference<>();
    FeatureEventHandler.setDispatchObserverForTesting(captured::set);

    handler.onHoeUse(1, 2, 3, "minecraft:wheat_crop");

    assertEquals(FeatureId.CROPINATION, captured.get().feature());
    core.shutdown();
  }

  @Test
  void doesNotDispatchPickupWhenCaptivationIsDisabled() {
    Assumptions.assumeTrue(TestRuntimeAssumptions.canInitializeSoundEvents());
    MinersAdvantageCore core = new MinersAdvantageCore();
    core.bootstrap();
    core.components().get(FeatureId.CAPTIVATION).disable();

    CommonEventHandlerImpl handler = new CommonEventHandlerImpl(core);
    AtomicReference<FeatureDispatchContext> captured = new AtomicReference<>();
    FeatureEventHandler.setDispatchObserverForTesting(captured::set);

    handler.onItemPickup("minecraft:diamond", false);

    assertNull(captured.get());
    core.shutdown();
  }

  @Test
  void doesNotDispatchPickaxeWhenShaftanationIsDisabled() {
    Assumptions.assumeTrue(TestRuntimeAssumptions.canInitializeSoundEvents());
    MinersAdvantageCore core = new MinersAdvantageCore();
    core.bootstrap();
    core.components().get(FeatureId.SHAFTANATION).disable();

    CommonEventHandlerImpl handler = new CommonEventHandlerImpl(core);
    AtomicReference<FeatureDispatchContext> captured = new AtomicReference<>();
    FeatureEventHandler.setDispatchObserverForTesting(captured::set);

    handler.onPickaxeUse(1, 2, 3, "minecraft:stone");

    assertNull(captured.get());
    core.shutdown();
  }

  @Test
  void itemPickupDispatchRetainsPerPlayerOwnership() {
    Assumptions.assumeTrue(TestRuntimeAssumptions.canInitializeSoundEvents());
    MinersAdvantageCore core = new MinersAdvantageCore();
    core.bootstrap();
    core.handleComponentTogglePacket(7L, new ComponentTogglePacket(FeatureId.CAPTIVATION, false), false);

    CommonEventHandlerImpl handler = new CommonEventHandlerImpl(core);
    AtomicReference<FeatureDispatchContext> captured = new AtomicReference<>();
    FeatureEventHandler.setDispatchObserverForTesting(captured::set);

    handler.onItemPickup(7L, "minecraft:diamond", false);
    assertNull(captured.get());

    handler.onItemPickup(8L, "minecraft:diamond", false);
    assertEquals(8L, captured.get().playerId());
    core.shutdown();
  }
}
