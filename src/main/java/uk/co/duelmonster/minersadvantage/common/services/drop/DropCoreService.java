package uk.co.duelmonster.minersadvantage.common.services.drop;

import java.util.ArrayList;
import java.util.List;

public final class DropCoreService {
    public record CapturedDrop(String type, int amount) {}

    private final List<CapturedDrop> capturedDrops = new ArrayList<>();

    public void capture(String type, int amount) {
        capturedDrops.add(new CapturedDrop(type, amount));
    }

    public List<CapturedDrop> flush() {
        List<CapturedDrop> snapshot = List.copyOf(capturedDrops);
        capturedDrops.clear();
        return snapshot;
    }

    public int count() {
        return capturedDrops.size();
    }
}
