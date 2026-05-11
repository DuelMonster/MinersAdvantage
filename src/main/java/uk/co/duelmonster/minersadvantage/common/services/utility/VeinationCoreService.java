package uk.co.duelmonster.minersadvantage.common.services.utility;

import java.util.ArrayList;
import java.util.List;

/**
 * VeinationCoreService keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class VeinationCoreService {
    /**
     * VeinNode keeps this part of Miners Advantage running without turning server ticks into confetti.
     * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
     */
    public record VeinNode(int x, int y, int z) {}

    /**
     * sameVein exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public boolean sameVein(String blockId1, String blockId2) {
        return blockId1 != null && blockId1.equals(blockId2);
    }

    /**
     * estimatedBlocksInVein exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public int estimatedBlocksInVein(int maxVeinDistance, int foundCount) {
        return foundCount + (maxVeinDistance * 3);
    }

    /**
     * buildVeinNodes exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
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

