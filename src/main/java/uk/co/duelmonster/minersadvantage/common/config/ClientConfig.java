package uk.co.duelmonster.minersadvantage.common.config;

public record ClientConfig(
    boolean disableParticleEffects
) {
    public ClientConfig() {
        this(false);
    }
}
