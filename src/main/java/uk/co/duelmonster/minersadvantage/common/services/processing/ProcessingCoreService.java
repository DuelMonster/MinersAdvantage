package uk.co.duelmonster.minersadvantage.common.services.processing;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

public final class ProcessingCoreService<T> {
    private final Deque<T> queue = new ArrayDeque<>();
    private final int blocksPerTick;
    private final int blockLimit;

    public ProcessingCoreService(int blocksPerTick, int blockLimit) {
        this.blocksPerTick = blocksPerTick;
        this.blockLimit = blockLimit;
    }

    public boolean offer(T value) {
        if (queue.size() >= blockLimit) {
            return false;
        }
        queue.add(value);
        return true;
    }

    public int processTick(Consumer<T> consumer) {
        int processed = 0;
        while (processed < blocksPerTick && !queue.isEmpty()) {
            consumer.accept(queue.removeFirst());
            processed++;
        }
        return processed;
    }

    public void clear() {
        queue.clear();
    }

    public int size() {
        return queue.size();
    }
}
