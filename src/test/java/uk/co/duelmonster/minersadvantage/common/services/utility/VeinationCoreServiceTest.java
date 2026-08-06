package uk.co.duelmonster.minersadvantage.common.services.utility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * VeinationCoreServiceTest keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class VeinationCoreServiceTest {
  @Test
  /**
   * b ui ld sc on ne ct ed ve in no de li st wi th in li mi ts exists so this path stays predictable and easier to debug when things get weird.
   */
  void buildsConnectedVeinNodeListWithinLimits() {
    VeinationCoreService service = new VeinationCoreService();
    List<VeinationCoreService.VeinNode> nodes = service.buildVeinNodes(0, 40, 0, 2, 5);

    assertEquals(5, nodes.size());
    assertEquals(0, nodes.get(0).x());
    assertTrue(nodes.stream().allMatch(node -> Math.abs(node.x()) <= 2));
  }

  @Test
  /**
   * sameVein and cursor-empty checks exist so throughput/parity helpers stay locked to current semantics.
   */
  void normalizesOreFamiliesAndKeepsEstimateBounded() {
    VeinationCoreService service = new VeinationCoreService();

    assertTrue(service.sameVein("minecraft:deepslate_diamond_ore", "minecraft:diamond_ore"));
    assertFalse(service.sameVein("minecraft:diamond_ore", "minecraft:gold_ore"));
    assertEquals(11, service.estimatedBlocksInVein(2, 5));
  }
}
