package uk.co.duelmonster.minersadvantage.common.services.utility;

import java.util.ArrayList;
import java.util.List;

public final class VeinationCoreService {
    public record VeinNode(int x, int y, int z) {}

    public boolean sameVein(String blockId1, String blockId2) {
        return blockId1 != null && blockId1.equals(blockId2);
    }

    public int estimatedBlocksInVein(int maxVeinDistance, int foundCount) {
        return foundCount + (maxVeinDistance * 3);
    }

    public List<VeinNode> buildVeinNodes(int originX, int originY, int originZ, int maxVeinDistance, int maxNodes) {
        List<VeinNode> nodes = new ArrayList<>();
        if (maxVeinDistance < 0 || maxNodes <= 0) {
            return nodes;
        }

        nodes.add(new VeinNode(originX, originY, originZ));
        for (int distance = 1; distance <= maxVeinDistance && nodes.size() < maxNodes; distance++) {
            nodes.add(new VeinNode(originX + distance, originY, originZ));
            if (nodes.size() >= maxNodes) {
                break;
            }
            nodes.add(new VeinNode(originX - distance, originY, originZ));
            if (nodes.size() >= maxNodes) {
                break;
            }
            nodes.add(new VeinNode(originX, originY + distance, originZ));
            if (nodes.size() >= maxNodes) {
                break;
            }
            nodes.add(new VeinNode(originX, originY - distance, originZ));
            if (nodes.size() >= maxNodes) {
                break;
            }
            nodes.add(new VeinNode(originX, originY, originZ + distance));
            if (nodes.size() >= maxNodes) {
                break;
            }
            nodes.add(new VeinNode(originX, originY, originZ - distance));
        }
        return nodes;
    }
}
