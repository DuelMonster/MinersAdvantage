package uk.co.duelmonster.minersadvantage.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import uk.co.duelmonster.minersadvantage.common.Constants;
import uk.co.duelmonster.minersadvantage.network.packets.PacketAbortAgents;
import uk.co.duelmonster.minersadvantage.network.packets.PacketCaptivate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketCropinate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketCultivate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketExcavate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketIlluminate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketLumbinate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketPathanate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketShaftanate;
import uk.co.duelmonster.minersadvantage.network.packets.PacketSubstituteTool;
import uk.co.duelmonster.minersadvantage.network.packets.PacketSupremeVantage;
import uk.co.duelmonster.minersadvantage.network.packets.PacketSynchronization;
import uk.co.duelmonster.minersadvantage.network.packets.PacketVeinate;

public class NetworkHandler {

  private final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
      .named(Constants.CHANNEL_ID)
      .clientAcceptedVersions(Constants.PROTOCOL_VERSION::equals)
      .serverAcceptedVersions(Constants.PROTOCOL_VERSION::equals)
      .networkProtocolVersion(() -> Constants.PROTOCOL_VERSION)
      .simpleChannel();

  public void registerPackets() {
    int id = 0;

    CHANNEL.registerMessage(id++,
        PacketAbortAgents.class,
        PacketAbortAgents::encode,
        PacketAbortAgents::decode,
        PacketAbortAgents::handle);

    CHANNEL.registerMessage(id++,
        PacketSynchronization.class,
        PacketSynchronization::encode,
        PacketSynchronization::decode,
        PacketSynchronization::handle);

    CHANNEL.registerMessage(id++,
        PacketSupremeVantage.class,
        PacketSupremeVantage::encode,
        PacketSupremeVantage::decode,
        PacketSupremeVantage::handle);

    CHANNEL.registerMessage(id++,
        PacketCaptivate.class,
        PacketCaptivate::encode,
        PacketCaptivate::decode,
        PacketCaptivate::handle);

    CHANNEL.registerMessage(id++,
        PacketCropinate.class,
        PacketCropinate::encode,
        PacketCropinate::decode,
        PacketCropinate::handle);

    CHANNEL.registerMessage(id++,
        PacketCultivate.class,
        PacketCultivate::encode,
        PacketCultivate::decode,
        PacketCultivate::handle);

    CHANNEL.registerMessage(id++,
        PacketExcavate.class,
        PacketExcavate::encode,
        PacketExcavate::decode,
        PacketExcavate::handle);

    CHANNEL.registerMessage(id++,
        PacketIlluminate.class,
        PacketIlluminate::encode,
        PacketIlluminate::decode,
        PacketIlluminate::handle);

    CHANNEL.registerMessage(id++,
        PacketLumbinate.class,
        PacketLumbinate::encode,
        PacketLumbinate::decode,
        PacketLumbinate::handle);

    CHANNEL.registerMessage(id++,
        PacketPathanate.class,
        PacketPathanate::encode,
        PacketPathanate::decode,
        PacketPathanate::handle);

    CHANNEL.registerMessage(id++,
        PacketShaftanate.class,
        PacketShaftanate::encode,
        PacketShaftanate::decode,
        PacketShaftanate::handle);

    CHANNEL.registerMessage(id++,
        PacketSubstituteTool.class,
        PacketSubstituteTool::encode,
        PacketSubstituteTool::decode,
        PacketSubstituteTool::handle);

    CHANNEL.registerMessage(id++,
        PacketVeinate.class,
        PacketVeinate::encode,
        PacketVeinate::decode,
        PacketVeinate::handle);
  }

  /**
   * Sends a packet to the server.<br>
   * Must be called Client side.
   */
  @OnlyIn(Dist.CLIENT)
  public void sendToServer(Object msg) {
    CHANNEL.sendToServer(msg);
  }

  /**
   * Send a packet to a specific player.<br>
   * Must be called Server side.
   */
  public void sendTo(ServerPlayer player, Object msg) {
    if (!(player instanceof FakePlayer)) {
      CHANNEL.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }
  }
}
