package uk.co.duelmonster.minersadvantage.agent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LumbinationAgentTest {
  @Test
  void isHarvestableLogId_shouldRejectStrippedAndWoodForms() {
    assertTrue(LumbinationAgent.isHarvestableLogId("minecraft:oak_log"));
    assertTrue(LumbinationAgent.isHarvestableLogId("minecraft:crimson_stem"));
    assertTrue(LumbinationAgent.isHarvestableLogId("minecraft:warped_hyphae"));
    assertFalse(LumbinationAgent.isHarvestableLogId("minecraft:stripped_oak_log"));
    assertFalse(LumbinationAgent.isHarvestableLogId("minecraft:oak_wood"));
    assertFalse(LumbinationAgent.isHarvestableLogId("minecraft:stripped_oak_wood"));
  }

  @Test
  void isBlockedLogId_shouldRejectStrippedAndWoodForms() {
    assertTrue(LumbinationAgent.isBlockedLogId("minecraft:stripped_oak_log"));
    assertTrue(LumbinationAgent.isBlockedLogId("minecraft:oak_wood"));
    assertTrue(LumbinationAgent.isBlockedLogId("minecraft:stripped_oak_wood"));
    assertFalse(LumbinationAgent.isBlockedLogId("minecraft:oak_log"));
  }

  @Test
  void hasAvailableSaplings_shouldCountHarvestedDropsAlongsideInventory() {
    assertTrue(LumbinationAgent.hasAvailableSaplings(0, 4, 4));
    assertTrue(LumbinationAgent.hasAvailableSaplings(2, 2, 4));
    assertFalse(LumbinationAgent.hasAvailableSaplings(1, 2, 4));
  }

  @Test
  void isMangroveRootsId_shouldRecognizeMangroveRootsOnly() {
    assertTrue(LumbinationAgent.isMangroveRootsId("minecraft:mangrove_roots"));
    assertFalse(LumbinationAgent.isMangroveRootsId("minecraft:oak_leaves"));
    assertFalse(LumbinationAgent.isMangroveRootsId(null));
  }
}
