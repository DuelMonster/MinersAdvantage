package uk.co.duelmonster.minersadvantage.common.component;

/**
 * ComponentLifecycle keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public interface ComponentLifecycle {
    void register();

    void enable();

    void disable();

    void tick();

    void cleanup();

    boolean isEnabled();
}

