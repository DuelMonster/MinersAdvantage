package uk.co.duelmonster.minersadvantage.common.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import uk.co.duelmonster.minersadvantage.common.config.SubstitutionConfig;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.common.services.policy.PolicyCoreService;

/**
 * PolicyCoreServiceTest keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
class PolicyCoreServiceTest {
  @Test
  /**
   * v al id at es ra ng es exists so this path stays predictable and easier to debug when things get weird.
   */
  void validatesRanges() {
    PolicyCoreService service = new PolicyCoreService();
    assertEquals(5, service.clampRange(9, 1, 5));
  }

  @Test
  /**
   * a pp li es se rv er au th or it at iv ec on fi gt oe ff ec ti ve co nf ig exists so this path stays predictable and easier to debug when things get weird.
   */
  void appliesServerAuthoritativeConfigToEffectiveConfig() {
    PolicyCoreService service = new PolicyCoreService();
    SyncedClientConfig client = SyncedClientConfig.defaults();
    SyncedClientConfig server = ConfigTestFixtures.withSubstitution(
        client,
        new SubstitutionConfig(false, true, false, false, false, false, false, client.substitution().blacklist()));

    SyncedClientConfig effective = service.applyServerAuthoritative(client, server);

    assertFalse(effective.substitution().enabled());
    assertTrue(effective.substitution().allowMending());
    assertFalse(effective.substitution().ignoreIfValidTool());
  }

  @Test
  /**
   * e nf or ce ss er ve rc om mo np ol ic yv al ue s exists so this path stays predictable and easier to debug when things get weird.
   */
  void enforcesServerCommonPolicyValues() {
    PolicyCoreService service = new PolicyCoreService();
    SyncedClientConfig client = SyncedClientConfig.defaults();
    SyncedClientConfig server = ConfigTestFixtures.withCommon(
        client,
        new uk.co.duelmonster.minersadvantage.common.config.CommonConfig(false, true, false, false, 8, 6, false, 0, 7));

    SyncedClientConfig effective = service.applyServerAuthoritative(client, server);

    assertFalse(effective.common().mineVeins());
    assertFalse(effective.common().autoIlluminate());
    assertEquals(8, effective.common().ticksPerBlock());
    assertEquals(6, effective.common().maxBlocksPerTick());
    assertEquals(7, effective.common().blockRadius());
  }

  @Test
  /**
   * u se ss er ve rg am ep la yc on fi ge ve nw it ho ut en fo rc em en tf la gs exists so this path stays predictable and easier to debug when things get weird.
   */
  void usesServerGameplayConfigEvenWithoutEnforcementFlags() {
    PolicyCoreService service = new PolicyCoreService();
    SyncedClientConfig client = SyncedClientConfig.defaults();
    SyncedClientConfig server = ConfigTestFixtures.withSubstitution(
        ConfigTestFixtures.withCommon(
            client,
            new uk.co.duelmonster.minersadvantage.common.config.CommonConfig(true, false, true, true, 10, 2, true, 5,
                3)),
        new SubstitutionConfig(false, true, false, true, true, true, true, client.substitution().blacklist()));

    SyncedClientConfig effective = service.applyServerAuthoritative(client, server);

    assertFalse(effective.substitution().enabled());
    assertTrue(effective.substitution().allowMending());
  }
}
