package uk.co.duelmonster.minersadvantage.common.services.policy;

public final class PolicyCoreService {
    public int clampRange(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    public boolean featureEnabled(boolean clientEnabled, boolean serverOverrideEnabled) {
        return clientEnabled && serverOverrideEnabled;
    }
}
