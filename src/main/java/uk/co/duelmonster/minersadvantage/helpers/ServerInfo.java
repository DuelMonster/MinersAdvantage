package uk.co.duelmonster.minersadvantage.helpers;

import javax.annotation.Nullable;

import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.NetworkManager;
import net.minecraft.server.MinecraftServer;

public final class ServerInfo {
	private static boolean isOnServer = false;
	@Nullable
	private static String worldUid = null;

	private ServerInfo() {
	}

	public static boolean isOnServer() {
		return isOnServer;
	}

	public static void onConnectedToServer(boolean isOnServer) {
		ServerInfo.isOnServer = isOnServer;
		ServerInfo.worldUid = null;
	}

	public static String getWorldUid(@Nullable NetworkManager networkManager) {
		if (worldUid == null) {

			if (networkManager == null) {

				worldUid = "default"; // we get here when opening the in-game config before loading a world

			} else if (networkManager.isLocalChannel()) {

				MinecraftServer minecraftServer = ServerLifecycleHooks.getCurrentServer();

				if (minecraftServer != null)
					worldUid = minecraftServer.getFolderName();

			} else {

				ServerData serverData = Minecraft.getInstance().getCurrentServerData();

				if (serverData != null)
					worldUid = serverData.serverIP + ' ' + serverData.serverName;
			}

			if (worldUid == null)
				worldUid = "default";

			worldUid = "world" + worldUid.hashCode();
		}

		return worldUid;
	}

}