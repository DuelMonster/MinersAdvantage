package uk.co.duelmonster.minersadvantage.common.services.drop;

import java.util.ArrayList;
import java.util.List;

/**
 * DropCoreService keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class DropCoreService {
    /**
     * CapturedDrop keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record CapturedDrop(String type, int amount) {}

    private final List<CapturedDrop> capturedDrops = new ArrayList<>();

    /**
     * capture exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void capture(String type, int amount) {
        capturedDrops.add(new CapturedDrop(type, amount));
    }

    /**
     * flush exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public List<CapturedDrop> flush() {
        List<CapturedDrop> snapshot = List.copyOf(capturedDrops);
        capturedDrops.clear();
        return snapshot;
    }

    /**
     * count exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public int count() {
        return capturedDrops.size();
    }
}
