package uk.co.duelmonster.minersadvantage.common.network.packets;

import java.util.UUID;
import uk.co.duelmonster.minersadvantage.common.JsonHelper;
import uk.co.duelmonster.minersadvantage.common.SyncType;
import uk.co.duelmonster.minersadvantage.common.Variables;
import uk.co.duelmonster.minersadvantage.common.config.MAConfig_Base;
import uk.co.duelmonster.minersadvantage.common.config.SyncedClientConfig;
import uk.co.duelmonster.minersadvantage.common.network.packetids.PacketId;

/**
 * PacketSynchronization is the teammate that keeps this part of the mod understandable and stable.
 * It exists so behavior stays explicit instead of becoming mystery spaghetti at 2 AM.
 */
public class PacketSynchronization implements IMAPacket {
    public final UUID uuid;
    public final SyncType syncType;
    public final String payload;

    /**
     * PacketSynchronization exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public PacketSynchronization(UUID uuid, SyncType syncType, String payload) {
        this.uuid = uuid;
        this.syncType = syncType;
        this.payload = payload == null ? "" : payload;
    }

    @Override
    /**
     * getPacketId exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public PacketId getPacketId() {
        return PacketId.SYNCHRONIZATION;
    }

    /**
     * process exists to keep this step focused, predictable, and debuggable.
     * In short: one clear job here beats ten confusing side-effects elsewhere.
     */
    public static void process(UUID senderUuid, PacketSynchronization pkt, boolean playToServer) {
        if (pkt.syncType == SyncType.Variables) {
            if (playToServer && senderUuid != null) {
                Variables.set(senderUuid, pkt.payload);
            } else {
                Variables.set(pkt.payload);
            }
            return;
        }

        if (pkt.syncType == SyncType.ClientConfig) {
            SyncedClientConfig config = JsonHelper.fromJson(pkt.payload, SyncedClientConfig.class);
            if (config == null) {
                return;
            }
            if (playToServer && senderUuid != null) {
                MAConfig_Base.setPlayerConfig(senderUuid, config);
            } else {
                MAConfig_Base.setGlobalConfig(config);
            }
        }
    }
}


