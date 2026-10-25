package uk.co.duelmonster.minersadvantage.common.services.utility;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class VeinationRuntimeServiceTest {
  @Test
  void anchorRequiresMatchingDropOwner() {
    UUID owner = UUID.randomUUID();

    assertTrue(VeinationRuntimeService.hasMatchingDropOwner(owner, owner));
    assertFalse(VeinationRuntimeService.hasMatchingDropOwner(owner, UUID.randomUUID()));
  }

  @Test
  void unattributedDropCannotMatchNearbyPlayer() {
    assertFalse(VeinationRuntimeService.hasMatchingDropOwner(null, UUID.randomUUID()));
    assertFalse(VeinationRuntimeService.hasMatchingDropOwner(UUID.randomUUID(), null));
  }
}
