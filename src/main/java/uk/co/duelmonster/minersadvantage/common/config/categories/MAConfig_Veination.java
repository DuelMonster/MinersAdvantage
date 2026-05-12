package uk.co.duelmonster.minersadvantage.common.config.categories;

import java.util.List;
import uk.co.duelmonster.minersadvantage.common.config.VeinationConfig;

public final class MAConfig_Veination extends MAConfig_BaseCategory {
    private int maxVeinDistance;
    private List<String> ores;

    public MAConfig_Veination(VeinationConfig config) {
        this.enabled = config.enabled();
        this.maxVeinDistance = config.maxVeinDistance();
        this.ores = config.ores();
    }

    public int maxVeinDistance() { return maxVeinDistance; }
    public List<String> ores() { return ores; }
}
