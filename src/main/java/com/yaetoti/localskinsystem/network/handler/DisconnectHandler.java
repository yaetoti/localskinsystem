package com.yaetoti.localskinsystem.network.handler;

import com.yaetoti.localskinsystem.Mod;
import com.yaetoti.localskinsystem.SkinCacheServer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public class DisconnectHandler implements ServerPlayConnectionEvents.Disconnect {
  @Override
  public void onPlayDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
    Mod.LOGGER.info("Removing textures of disconnected player: " + handler.player.getGameProfile().getName());
    SkinCacheServer.Get().RemoveTextures(handler.player.getGameProfile().getName());
  }
}
