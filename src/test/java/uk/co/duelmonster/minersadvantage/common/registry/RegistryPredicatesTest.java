package uk.co.duelmonster.minersadvantage.common.registry;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * RegistryPredicatesTest keeps defensive registry lookups predictable while preserving gameplay semantics.
 */
class RegistryPredicatesTest {
  @Test
  /**
   * Ore-like id helper should preserve expected allow/deny behavior.
   */
  void oreLikeIdHelperPreservesExpectedSemantics() {
    assertTrue(RegistryPredicates.isOreLikeBlockId("minecraft:deepslate_gold_ore"));
    assertFalse(RegistryPredicates.isOreLikeBlockId("minecraft:dirt"));
  }

  @Test
  /**
   * Log/leaf/sapling heuristics should remain stable for known ids.
   */
  void logLeafSaplingIdHelpersPreserveExpectedSemantics() {
    assertTrue(RegistryPredicates.isLogLikeBlockId("minecraft:oak_log"));
    assertTrue(RegistryPredicates.isLeafLikeBlockId("minecraft:oak_leaves"));
    assertTrue(RegistryPredicates.isSaplingBlock("minecraft:oak_sapling"));
    assertFalse(RegistryPredicates.isLogLikeBlockId("minecraft:stone"));
  }
}
