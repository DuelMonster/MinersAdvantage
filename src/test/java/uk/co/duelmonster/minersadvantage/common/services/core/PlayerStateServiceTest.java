package uk.co.duelmonster.minersadvantage.common.services.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class PlayerStateServiceTest {
    @Test
    void storesAndRetrievesPlayerState() {
        PlayerStateService service = new PlayerStateService();
        PlayerStateService.PlayerState state = new PlayerStateService.PlayerState(1L, true, 100L, 5);
        service.updatePlayerState(1L, state);
        
        PlayerStateService.PlayerState retrieved = service.getPlayerState(1L);
        assertNotNull(retrieved);
        assertEquals(1L, retrieved.playerId());
        assertEquals(true, retrieved.hungerGuardActive());
    }

    @Test
    void returnsDefaultStateForUnknownPlayer() {
        PlayerStateService service = new PlayerStateService();
        PlayerStateService.PlayerState state = service.getPlayerState(999L);
        assertNotNull(state);
        assertEquals(999L, state.playerId());
        assertEquals(false, state.hungerGuardActive());
    }
}
