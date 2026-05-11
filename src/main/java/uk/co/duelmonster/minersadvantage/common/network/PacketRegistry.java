package uk.co.duelmonster.minersadvantage.common.network;

import java.util.HashMap;
import java.util.Map;

/**
 * PacketRegistry keeps this part of Miners Advantage running without turning server ticks into confetti.
 * It's here to make the behavior obvious, reliable, and slightly less mysterious at 2 AM.
 */
public final class PacketRegistry {
    private static final Map<Integer, String> PACKET_TYPES = new HashMap<>();

    public static final int FEATURE_DISPATCH = 0;
    public static final int PLAYER_STATE_SYNC = 1;
    public static final int COMPONENT_TOGGLE = 2;
    public static final int ABORT_WORKERS = 4;
    public static final int SUPREME_VANTAGE = 5;

    static {
        PACKET_TYPES.put(FEATURE_DISPATCH, "FeatureDispatchPacket");
        PACKET_TYPES.put(PLAYER_STATE_SYNC, "PlayerStateSyncPacket");
        PACKET_TYPES.put(COMPONENT_TOGGLE, "ComponentTogglePacket");
        PACKET_TYPES.put(ABORT_WORKERS, "AbortWorkersPacket");
        PACKET_TYPES.put(SUPREME_VANTAGE, "SupremeVantagePacket");
    }

    /**
     * getPacketName exists so this code path does one job clearly instead of spreading chaos across callers.
     * Think of it as a guardrail for correctness, minus the dramatic cliff scene.
     */
    public static String getPacketName(int id) {
        return PACKET_TYPES.getOrDefault(id, "UnknownPacket");
    }
}

