package com.yaetoti.localskinsystem.client.network.handler;

import com.yaetoti.localskinsystem.client.SkinCacheClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

public class DisconnectHandler implements ClientPlayConnectionEvents.Disconnect {
  @Override
  public void onPlayDisconnect(ClientPlayNetworkHandler handler, MinecraftClient client) {
    SkinCacheClient.Get().Reset();
  }
}
