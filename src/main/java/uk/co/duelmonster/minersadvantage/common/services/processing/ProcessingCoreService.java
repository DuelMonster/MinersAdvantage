package uk.co.duelmonster.minersadvantage.common.services.processing;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

/**
 * ProcessingCoreService keeps this part of MinersAdvantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class ProcessingCoreService<T> {
    private final Deque<T> queue = new ArrayDeque<>();
    private final int blocksPerTick;
    private final int blockLimit;

    /**
     * ProcessingCoreService exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public ProcessingCoreService(int blocksPerTick, int blockLimit) {
        this.blocksPerTick = blocksPerTick;
        this.blockLimit = blockLimit;
    }

    /**
     * offer exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean offer(T value) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (queue.size() >= blockLimit) {
            return false;
        }
        queue.add(value);
        return true;
    }

    /**
     * processTick exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public int processTick(Consumer<T> consumer) {
        int processed = 0;
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        while (processed < blocksPerTick && !queue.isEmpty()) {
            consumer.accept(queue.removeFirst());
            processed++;
        }
        return processed;
    }

    /**
     * clear exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public void clear() {
        queue.clear();
    }

    /**
     * size exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public int size() {
        return queue.size();
    }
}
