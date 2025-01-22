package com.yaetoti.localskinsystem.client.network.handler;

import com.yaetoti.localskinsystem.TexturesData;
import com.yaetoti.localskinsystem.client.utils.TextureHelper;
import com.yaetoti.localskinsystem.network.packet.c2s.custom.UploadTexturesC2SPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

public class JoinHandler implements ClientPlayConnectionEvents.Join {
  @Override
  public void onPlayReady(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
    TexturesData textures = TextureHelper.LoadSkinTextures();
    if (textures == null) {
      return;
    }

    ClientPlayNetworking.send(new UploadTexturesC2SPayload(textures.isSlimModel(), textures.skinData(), textures.capeData(), textures.elytraData()));
  }
}
