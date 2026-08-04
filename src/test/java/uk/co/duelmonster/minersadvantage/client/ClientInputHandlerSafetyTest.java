//? if fabric {
package uk.co.duelmonster.minersadvantage.client;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.config.MAClientRootConfig;
import uk.co.duelmonster.minersadvantage.common.config.MAServerRootConfig;

/**
 * Guardrails for startup safety in client input handling.
 */
class ClientInputHandlerSafetyTest {
  @Test
  void doesNotReadRuntimeConfigInStaticFieldInitializers() throws ReflectiveOperationException {
    Field clientSnapshotField = ClientInputHandler.class.getDeclaredField("lastSyncedClientConfig");
    Field serverSnapshotField = ClientInputHandler.class.getDeclaredField("lastSyncedServerConfig");
    clientSnapshotField.setAccessible(true);
    serverSnapshotField.setAccessible(true);

    MAClientRootConfig clientSnapshot = (MAClientRootConfig) clientSnapshotField.get(null);
    MAServerRootConfig serverSnapshot = (MAServerRootConfig) serverSnapshotField.get(null);

    assertTrue(clientSnapshot == null, "Client snapshot must remain lazy-initialized");
    assertTrue(serverSnapshot == null, "Server snapshot must remain lazy-initialized");
  }
}
//?} else {
/*
// Fabric-only regression test.
*/
//?}
