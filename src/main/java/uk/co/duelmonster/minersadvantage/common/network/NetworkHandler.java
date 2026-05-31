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

    /**
     * registerPackets exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public void registerPackets() {
        serverHandlers.clear();
    }

    /**
     * registerServerHandler exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public void registerServerHandler(int packetId, Consumer<Object> handler) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (handler == null) {
            serverHandlers.remove(packetId);
            return;
        }
        serverHandlers.put(packetId, handler);
    }

    /**
     * registerPlayerHandler exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public void registerPlayerHandler(long playerId, Consumer<Object> handler) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (handler == null) {
            playerHandlers.remove(playerId);
            return;
        }
        playerHandlers.put(playerId, handler);
    }

    /**
     * sendToServer exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public void sendToServer(Object msg) {
        Consumer<Object> handler = serverHandlers.get(resolvePacketId(msg));
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (handler != null) {
            handler.accept(msg);
            return;
        }
        Consumer<Object> fallback = serverHandlers.get(FALLBACK_PACKET_ID);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (fallback != null) {
            fallback.accept(msg);
        }
    }

    /**
     * sendTo exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public void sendTo(long playerId, Object msg) {
        Consumer<Object> handler = playerHandlers.get(playerId);
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (handler != null) {
            handler.accept(msg);
        }
    }

    /**
     * resolvePacketId exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    private int resolvePacketId(Object msg) {
        // Why this branch exists: make the flow explicit so future debugging is less guesswork and fewer surprises.
        if (msg instanceof IMAPacket packet && packet.getPacketId() != null) {
            return packet.getPacketId().ordinal();
        }
        return FALLBACK_PACKET_ID;
    }
}
