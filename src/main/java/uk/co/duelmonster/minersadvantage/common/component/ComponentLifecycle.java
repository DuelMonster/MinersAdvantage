package uk.co.duelmonster.minersadvantage.common.component;

public interface ComponentLifecycle {
    void register();

    void enable();

    void disable();

    void tick();

    void cleanup();

    boolean isEnabled();
}
