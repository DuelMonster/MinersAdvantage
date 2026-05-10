package uk.co.duelmonster.minersadvantage.common.network;

import java.util.HashMap;
import java.util.Map;

public final class PacketRegistry {
    private static final Map<Integer, String> PACKET_TYPES = new HashMap<>();

    public static final int FEATURE_DISPATCH = 0;
    public static final int PLAYER_STATE_SYNC = 1;
    public static final int COMPONENT_TOGGLE = 2;
    public static final int HARVEST_COMPLETE = 3;

    static {
        PACKET_TYPES.put(FEATURE_DISPATCH, "FeatureDispatchPacket");
        PACKET_TYPES.put(PLAYER_STATE_SYNC, "PlayerStateSyncPacket");
        PACKET_TYPES.put(COMPONENT_TOGGLE, "ComponentTogglePacket");
        PACKET_TYPES.put(HARVEST_COMPLETE, "HarvestCompletePacket");
    }

    public static String getPacketName(int id) {
        return PACKET_TYPES.getOrDefault(id, "UnknownPacket");
    }
}
