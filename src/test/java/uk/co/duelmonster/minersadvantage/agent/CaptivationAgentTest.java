package uk.co.duelmonster.minersadvantage.agent;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class CaptivationAgentTest {
  @Test
  void isRecentDropByPlayer_shouldReturnTrue_whenDropperMatchesAndCooldownIsActive() {
    UUID playerId = UUID.randomUUID();

    assertTrue(CaptivationAgent.isRecentDropByPlayer(playerId, playerId, 42));
  }

  @Test
  void isRecentDropByPlayer_shouldReturnFalse_whenDropperDoesNotMatchOrMissing() {
    UUID playerId = UUID.randomUUID();

    assertFalse(CaptivationAgent.isRecentDropByPlayer(UUID.randomUUID(), playerId, 42));
    assertFalse(CaptivationAgent.isRecentDropByPlayer(null, playerId, 42));
  }

  @Test
  void isRecentDropByPlayer_shouldReturnFalse_whenCooldownWindowHasExpired() {
    UUID playerId = UUID.randomUUID();

    assertFalse(CaptivationAgent.isRecentDropByPlayer(playerId, playerId, 160));
    assertFalse(CaptivationAgent.isRecentDropByPlayer(playerId, playerId, 999));
  }

  @Test
  void shouldSkipItemInTick_shouldSkip_whenItemCannotBeCaptured() {
    UUID playerId = UUID.randomUUID();

    assertTrue(CaptivationAgent.shouldSkipItemInTick(false, null, playerId, 0));
  }

  @Test
  void shouldSkipItemInTick_shouldSkip_whenRecentDropBySamePlayer() {
    UUID playerId = UUID.randomUUID();

    assertTrue(CaptivationAgent.shouldSkipItemInTick(true, playerId, playerId, 30));
  }

  @Test
  void shouldSkipItemInTick_shouldNotSkip_whenDropWasNotByCurrentPlayer() {
    UUID playerId = UUID.randomUUID();

    assertFalse(CaptivationAgent.shouldSkipItemInTick(true, UUID.randomUUID(), playerId, 30));
    assertFalse(CaptivationAgent.shouldSkipItemInTick(true, null, playerId, 30));
  }
}
