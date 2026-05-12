package uk.co.duelmonster.minersadvantage.common.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import uk.co.duelmonster.minersadvantage.common.network.packets.IMAPacket;

/**
 * Legacy compatibility network facade that routes packet names and payloads.
 */
public final class NetworkHandler {
    private static final int FALLBACK_PACKET_ID = -1;

    private final Map<Integer, Consumer<Object>> serverHandlers = new ConcurrentHashMap<>();
    private final Map<Long, Consumer<Object>> playerHandlers = new ConcurrentHashMap<>();

    public void registerPackets() {
        serverHandlers.clear();
    }

    public void registerServerHandler(int packetId, Consumer<Object> handler) {
        if (handler == null) {
            serverHandlers.remove(packetId);
            return;
        }
        serverHandlers.put(packetId, handler);
    }

    public void registerPlayerHandler(long playerId, Consumer<Object> handler) {
        if (handler == null) {
            playerHandlers.remove(playerId);
            return;
        }
        playerHandlers.put(playerId, handler);
    }

    public void sendToServer(Object msg) {
        Consumer<Object> handler = serverHandlers.get(resolvePacketId(msg));
        if (handler != null) {
            handler.accept(msg);
            return;
        }
        Consumer<Object> fallback = serverHandlers.get(FALLBACK_PACKET_ID);
        if (fallback != null) {
            fallback.accept(msg);
        }
    }

    public void sendTo(long playerId, Object msg) {
        Consumer<Object> handler = playerHandlers.get(playerId);
        if (handler != null) {
            handler.accept(msg);
        }
    }

    private int resolvePacketId(Object msg) {
        if (msg instanceof IMAPacket packet && packet.getPacketId() != null) {
            return packet.getPacketId().ordinal();
        }
        return FALLBACK_PACKET_ID;
    }
}
